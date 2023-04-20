package eu.siacs.conversations.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.axolotl.SQLiteAxolotlStore;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.persistance.DatabaseBackend;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.utils.BackupFileHeader;
import eu.siacs.conversations.utils.Compatibility;

public class ExportBackupTask {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static final String KEYTYPE = "AES";
    public static final String CIPHERMODE = "AES/GCM/NoPadding";
    public static final String PROVIDER = "BC";

    public static final String MIME_TYPE = "application/vnd.conversations.backup";

    static final int NOTIFICATION_ID = 19;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private Service mService;
    private DatabaseBackend mDatabaseBackend;
    private List<Account> mAccounts;
    private NotificationManager notificationManager;

    private static void accountExport(
            final SQLiteDatabase db, final String uuid, final JsonWriter writer)
            throws IOException {
        final Cursor accountCursor =
                db.query(
                        Account.TABLENAME,
                        null,
                        Account.UUID + "=?",
                        new String[] {uuid},
                        null,
                        null,
                        null);
        while (accountCursor != null && accountCursor.moveToNext()) {
            writer.beginObject();
            writer.name("table");
            writer.value(Account.TABLENAME);
            writer.name("values");
            writer.beginObject();
            for (int i = 0; i < accountCursor.getColumnCount(); ++i) {
                final String name = accountCursor.getColumnName(i);
                writer.name(name);
                final String value = accountCursor.getString(i);
                if (value == null || Account.ROSTERVERSION.equals(accountCursor.getColumnName(i))) {
                    writer.nullValue();
                } else if (Account.OPTIONS.equals(accountCursor.getColumnName(i))
                        && value.matches("\\d+")) {
                    int intValue = Integer.parseInt(value);
                    intValue |= 1 << Account.OPTION_DISABLED;
                    writer.value(intValue);
                } else {
                    writer.value(value);
                }
            }
            writer.endObject();
            writer.endObject();
        }
        if (accountCursor != null) {
            accountCursor.close();
        }
    }

    private static void simpleExport(
            final SQLiteDatabase db,
            final String table,
            final String column,
            final String uuid,
            final JsonWriter writer)
            throws IOException {
        final Cursor cursor =
                db.query(table, null, column + "=?", new String[] {uuid}, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            writer.beginObject();
            writer.name("table");
            writer.value(table);
            writer.name("values");
            writer.beginObject();
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                final String name = cursor.getColumnName(i);
                writer.name(name);
                final String value = cursor.getString(i);
                writer.value(value);
            }
            writer.endObject();
            writer.endObject();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static byte[] getKey(final String password, final byte[] salt)
            throws InvalidKeySpecException {
        final SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return factory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, 1024, 128))
                .getEncoded();
    }

    public ExportBackupTask(Service service) {
        mService = service;
        mDatabaseBackend = DatabaseBackend.getInstance(service.getBaseContext());
        mAccounts = mDatabaseBackend.getAccounts();
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public boolean run(Callback callback) {
        if (RUNNING.compareAndSet(false, true)) {
            new Thread(() -> {
                boolean success;
                List<File> files;
                try {
                    files = export();
                    success = true;
                } catch (final Exception e) {
                    Log.d(Config.LOGTAG, "unable to create backup", e);
                    success = false;
                    files = Collections.emptyList();
                }
                RUNNING.set(false);
                callback.done(success, files);
            }).start();
            return true;
        } else {
            Log.d(Config.LOGTAG, "ExportBackupTask. ignoring start command because already running");
        }
        return false;
    }

    private void messageExport(
            final SQLiteDatabase db,
            final String uuid,
            final JsonWriter writer,
            final Progress progress)
            throws IOException {
        Cursor cursor =
                db.rawQuery(
                        "select messages.* from messages join conversations on conversations.uuid=messages.conversationUuid where conversations.accountUuid=?",
                        new String[] {uuid});
        int size = cursor != null ? cursor.getCount() : 0;
        Log.d(Config.LOGTAG, "exporting " + size + " messages for account " + uuid);
        int i = 0;
        int p = 0;
        while (cursor != null && cursor.moveToNext()) {
            writer.beginObject();
            writer.name("table");
            writer.value(Message.TABLENAME);
            writer.name("values");
            writer.beginObject();
            for (int j = 0; j < cursor.getColumnCount(); ++j) {
                final String name = cursor.getColumnName(j);
                writer.name(name);
                final String value = cursor.getString(j);
                writer.value(value);
            }
            writer.endObject();
            writer.endObject();
            final int percentage = i * 100 / size;
            if (p < percentage) {
                p = percentage;
                notificationManager.notify(NOTIFICATION_ID, progress.build(p));
            }
            i++;
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private List<File> export() throws Exception {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mService.getBaseContext(), "backup");
        mBuilder.setContentTitle(mService.getString(R.string.notification_create_backup_title))
                .setSmallIcon(R.drawable.ic_archive_24dp)
                .setProgress(1, 0, false);
        mService.startForeground(NOTIFICATION_ID, mBuilder.build());
        int count = 0;
        final int max = this.mAccounts.size();
        final SecureRandom secureRandom = new SecureRandom();
        final List<File> files = new ArrayList<>();
        Log.d(Config.LOGTAG, "starting backup for " + max + " accounts");
        for (final Account account : this.mAccounts) {
            final String password = account.getPassword();
            if (Strings.nullToEmpty(password).trim().isEmpty()) {
                Log.d(
                        Config.LOGTAG,
                        String.format(
                                "skipping backup for %s because password is empty. unable to encrypt",
                                account.getJid().asBareJid()));
                continue;
            }
            Log.d(
                    Config.LOGTAG,
                    String.format(
                            "exporting data for account %s (%s)",
                            account.getJid().asBareJid(), account.getUuid()));
            final byte[] IV = new byte[12];
            final byte[] salt = new byte[16];
            secureRandom.nextBytes(IV);
            secureRandom.nextBytes(salt);
            final BackupFileHeader backupFileHeader =
                    new BackupFileHeader(
                            mService.getString(R.string.app_name),
                            account.getJid(),
                            System.currentTimeMillis(),
                            IV,
                            salt);
            final Progress progress = new Progress(mBuilder, max, count);
            final String filename =
                    String.format(
                            "%s.%s.ceb",
                            account.getJid().asBareJid().toEscapedString(),
                            DATE_FORMAT.format(new Date()));
            final File file =
                    new File(
                            FileBackend.getBackupDirectory(mService), filename);
            files.add(file);
            final File directory = file.getParentFile();
            if (directory != null && directory.mkdirs()) {
                Log.d(Config.LOGTAG, "created backup directory " + directory.getAbsolutePath());
            }
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            backupFileHeader.write(dataOutputStream);
            dataOutputStream.flush();

            final Cipher cipher =
                    Compatibility.twentyEight()
                            ? Cipher.getInstance(CIPHERMODE)
                            : Cipher.getInstance(CIPHERMODE, PROVIDER);
            final byte[] key = getKey(password, salt);
            SecretKeySpec keySpec = new SecretKeySpec(key, KEYTYPE);
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            CipherOutputStream cipherOutputStream =
                    new CipherOutputStream(fileOutputStream, cipher);

            final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(cipherOutputStream);
            final JsonWriter jsonWriter =
                    new JsonWriter(
                            new OutputStreamWriter(gzipOutputStream, StandardCharsets.UTF_8));
            jsonWriter.beginArray();
            final SQLiteDatabase db = this.mDatabaseBackend.getReadableDatabase();
            final String uuid = account.getUuid();
            accountExport(db, uuid, jsonWriter);
            simpleExport(db, Conversation.TABLENAME, Conversation.ACCOUNT, uuid, jsonWriter);
            messageExport(db, uuid, jsonWriter, progress);
            for (final String table :
                    Arrays.asList(
                            SQLiteAxolotlStore.PREKEY_TABLENAME,
                            SQLiteAxolotlStore.SIGNED_PREKEY_TABLENAME,
                            SQLiteAxolotlStore.SESSION_TABLENAME,
                            SQLiteAxolotlStore.IDENTITIES_TABLENAME)) {
                simpleExport(db, table, SQLiteAxolotlStore.ACCOUNT, uuid, jsonWriter);
            }
            jsonWriter.endArray();
            jsonWriter.flush();
            jsonWriter.close();
            mediaScannerScanFile(file);
            Log.d(Config.LOGTAG, "written backup to " + file.getAbsoluteFile());
            count++;
        }
        return files;
    }

    private void mediaScannerScanFile(final File file) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        mService.sendBroadcast(intent);
    }

    private static class Progress {
        private final NotificationCompat.Builder builder;
        private final int max;
        private final int count;

        private Progress(NotificationCompat.Builder builder, int max, int count) {
            this.builder = builder;
            this.max = max;
            this.count = count;
        }

        private Notification build(int percentage) {
            builder.setProgress(max * 100, count * 100 + percentage, false);
            return builder.build();
        }
    }

    @FunctionalInterface
    public interface Callback {
        void done(boolean success, List<File> files);
    }

}
