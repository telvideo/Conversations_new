package eu.siacs.conversations.ui.fragment.settings;

import static eu.siacs.conversations.utils.Compatibility.hasStoragePermission;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.base.Strings;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.R;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.services.ExportBackupService;
import eu.siacs.conversations.services.ExportBackupJobService;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    private static final String CREATE_BACKUP = "create_backup";
    private static final String AUTOMATIC_BACKUPS = "automatic_backups";

    private static final int JOB_AUTOMATIC_BACKUP = 1;

    private final ActivityResultLauncher<String> requestStorageForBackupLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startBackup();
                        } else {
                            Toast.makeText(
                                            requireActivity(),
                                            getString(
                                                    R.string.no_storage_permission,
                                                    getString(R.string.app_name)),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        final var about = findPreference("about");
        final var connection = findPreference("connection");
        final var createBackup = findPreference(CREATE_BACKUP);
        final var automaticBackups = findPreference(AUTOMATIC_BACKUPS);
        if (about == null || connection == null || createBackup == null || automaticBackups == null) {
            throw new IllegalStateException(
                    "The preference resource file is missing some preferences");
        }
        createBackup.setSummary(
                getString(
                        R.string.pref_create_backup_summary,
                        FileBackend.getBackupDirectory(requireContext()).getAbsolutePath()));
        createBackup.setOnPreferenceClickListener(this::onCreateBackupPreferenceClicked);
        automaticBackups.setOnPreferenceChangeListener(this::onAutomaticBackupsPreferenceChanged);
        about.setTitle(getString(R.string.title_activity_about_x, BuildConfig.APP_NAME));
        about.setSummary(
                String.format(
                        "%s %s %s @ %s · %s · %s",
                        BuildConfig.APP_NAME,
                        BuildConfig.VERSION_NAME,
                        im.conversations.webrtc.BuildConfig.WEBRTC_VERSION,
                        Strings.nullToEmpty(Build.MANUFACTURER),
                        Strings.nullToEmpty(Build.DEVICE),
                        Strings.nullToEmpty(Build.VERSION.RELEASE)));
        if (ConnectionSettingsFragment.hideChannelDiscovery()) {
            connection.setSummary(R.string.pref_connection_summary);
        }
    }

    private boolean onCreateBackupPreferenceClicked(final Preference preference) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestStorageForBackupLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                startBackup();
            }
        } else {
            startBackup();
        }
        return true;
    }

    private void startBackup() {
        ContextCompat.startForegroundService(
                requireContext(), new Intent(requireContext(), ExportBackupService.class));
        final MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(requireActivity());
        builder.setMessage(R.string.backup_started_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    private boolean onAutomaticBackupsPreferenceChanged(Preference preference, Object value) {
        boolean enabled = ((boolean) value) && hasStoragePermission(requireContext());
        syncAutomaticBackupsJob(enabled);
        return enabled;
    }

    private void syncAutomaticBackupsJob(boolean enabled) {
        Activity activity = requireActivity();
        JobScheduler scheduler = (JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo existing = null;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_AUTOMATIC_BACKUP) {
                existing = jobInfo;
                break;
            }
        }
        if (enabled && existing == null) {
            JobInfo.Builder builder = new JobInfo.Builder(JOB_AUTOMATIC_BACKUP,
                    new ComponentName(activity, ExportBackupJobService.class));
            builder.setPeriodic(1000 * 60 * 60 * 24);
            builder.setPersisted(true);
            scheduler.schedule(builder.build());
            displayToast(activity, getString(R.string.toast_automatic_backups_enabled));
        } else if (!enabled && existing != null) {
            scheduler.cancel(JOB_AUTOMATIC_BACKUP);
        }
    }

    private void displayToast(Activity activity, String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, msg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.title_activity_settings);
    }
}
