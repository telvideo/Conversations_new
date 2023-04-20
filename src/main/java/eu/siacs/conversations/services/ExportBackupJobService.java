package eu.siacs.conversations.services;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class ExportBackupJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return new ExportBackupTask(this).run((success, files) -> {
            jobFinished(jobParameters, false);
        });
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

}
