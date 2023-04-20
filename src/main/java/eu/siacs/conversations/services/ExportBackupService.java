package eu.siacs.conversations.services;

import static eu.siacs.conversations.utils.Compatibility.s;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.siacs.conversations.R;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.utils.Compatibility;

public class ExportBackupService extends Service {

    private NotificationManager notificationManager;

    private static List<Intent> getPossibleFileOpenIntents(
            final Context context, final String path) {

        // http://www.openintents.org/action/android-intent-action-view/file-directory
        // do not use 'vnd.android.document/directory' since this will trigger system file manager
        final Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if (Compatibility.runsAndTargetsTwentyFour(context)) {
            openIntent.setType("resource/folder");
        } else {
            openIntent.setDataAndType(Uri.parse("file://" + path), "resource/folder");
        }
        openIntent.putExtra("org.openintents.extra.ABSOLUTE_PATH", path);

        final Intent amazeIntent = new Intent(Intent.ACTION_VIEW);
        amazeIntent.setDataAndType(Uri.parse("com.amaze.filemanager:" + path), "resource/folder");

        // will open a file manager at root and user can navigate themselves
        final Intent systemFallBack = new Intent(Intent.ACTION_VIEW);
        systemFallBack.addCategory(Intent.CATEGORY_DEFAULT);
        systemFallBack.setData(
                Uri.parse("content://com.android.externalstorage.documents/root/primary"));

        return Arrays.asList(openIntent, amazeIntent, systemFallBack);
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean running = new ExportBackupTask(this).run((success, files) -> {
            stopForeground(true);
            if (success) {
                notifySuccess(files);
            }
            stopSelf();
        });
        return running ? START_STICKY : START_NOT_STICKY;
    }

    private void notifySuccess(final List<File> files) {
        final String path = FileBackend.getBackupDirectory(this).getAbsolutePath();

        PendingIntent openFolderIntent = null;

        for (final Intent intent : getPossibleFileOpenIntents(this, path)) {
            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                openFolderIntent =
                        PendingIntent.getActivity(
                                this,
                                189,
                                intent,
                                s()
                                        ? PendingIntent.FLAG_IMMUTABLE
                                                | PendingIntent.FLAG_UPDATE_CURRENT
                                        : PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            }
        }

        PendingIntent shareFilesIntent = null;
        if (files.size() > 0) {
            final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<>();
            for (File file : files) {
                uris.add(FileBackend.getUriForFile(this, file));
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(ExportBackupTask.MIME_TYPE);
            final Intent chooser =
                    Intent.createChooser(intent, getString(R.string.share_backup_files));
            shareFilesIntent =
                    PendingIntent.getActivity(
                            this,
                            190,
                            chooser,
                            s()
                                    ? PendingIntent.FLAG_IMMUTABLE
                                            | PendingIntent.FLAG_UPDATE_CURRENT
                                    : PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getBaseContext(), "backup");
        mBuilder.setContentTitle(getString(R.string.notification_backup_created_title))
                .setContentText(getString(R.string.notification_backup_created_subtitle, path))
                .setStyle(
                        new NotificationCompat.BigTextStyle()
                                .bigText(
                                        getString(
                                                R.string.notification_backup_created_subtitle,
                                                FileBackend.getBackupDirectory(this)
                                                        .getAbsolutePath())))
                .setAutoCancel(true)
                .setContentIntent(openFolderIntent)
                .setSmallIcon(R.drawable.ic_archive_24dp);

        if (shareFilesIntent != null) {
            mBuilder.addAction(
                    R.drawable.ic_share_24dp,
                    getString(R.string.share_backup_files),
                    shareFilesIntent);
        }

        notificationManager.notify(ExportBackupTask.NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
