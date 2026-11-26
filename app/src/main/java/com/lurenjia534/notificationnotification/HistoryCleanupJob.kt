package com.lurenjia534.notificationnotification

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

internal class HistoryCleanupJob : JobService() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var cleanup: Job? = null

    override fun onStartJob(params: JobParameters): Boolean {
        cleanup = scope.launch {
            withContext(Dispatchers.IO) {
                NotificationHistory(applicationContext).clearExpired()
            }
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        cleanup?.cancel()
        return true
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val JOB_ID = 1

        fun updateSchedule(context: Context, enabled: Boolean) {
            val scheduler = context.getSystemService(JobScheduler::class.java)
            if (!enabled) {
                scheduler.cancel(JOB_ID)
            } else if (scheduler.getPendingJob(JOB_ID) == null) {
                val job = JobInfo.Builder(JOB_ID, ComponentName(context, HistoryCleanupJob::class.java))
                    .setPeriodic(TimeUnit.DAYS.toMillis(1))
                    .setPersisted(true)
                    .build()
                scheduler.schedule(job)
            }
        }
    }
}
