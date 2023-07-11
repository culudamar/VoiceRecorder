package com.recorder.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import com.core.common.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderService : Service() {

    private lateinit var recorder: MediaRecorder
    private lateinit var storage: Storage
    private val job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(this)
        else
            MediaRecorder()
        storage = Storage()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "record" -> {
                startRecording(this)
                Timber.e("record")
            }
            "stop" -> {
                stopRecordingAudio(onStopRecording = {

                })
                Timber.e("stop")
            }
            "pause" -> {
                pauseRecording()
                Timber.e("pause")
            }
            "resume" -> {
                resumeRecording()
                Timber.e("resume")
            }
        }

        return START_STICKY
    }

    private fun pauseRecording() {
        serviceScope.launch {
            recorder.apply {
                pause()
            }
        }
    }

    private fun resumeRecording() {
        serviceScope.launch {
            recorder.apply {
                resume()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
        Timber.e("recorder service destroyed")
    }

    private fun startRecording(context: Context) {
        serviceScope.launch {
            val path = storage.getPath(context)
            val fileName = generateFileName()
            val file = File(path, fileName)
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.path)
                try {
                    prepare()
                } catch (e: Exception) {
                Timber.e("recorder on android(S) can`t be prepared")
                }
                start()
            }
        }
    }

    private fun stopRecordingAudio(onStopRecording: () -> Unit) {
        serviceScope.launch {
            recorder.apply {
                stop()
                reset()
                onStopRecording()
                Timber.e("stopped recording")
            }
        }
    }

    private fun releaseResources() {
        serviceScope.launch {
            recorder.apply {
                release()
            }
            job.cancel()
        }
    }

    private fun generateFileName(
        pattern: String = "yyMMdd_HHmmss",
        fileExt: String = ".m4a",
        local: Locale = Locale.getDefault(),
    ): String {
        val sdf = SimpleDateFormat(pattern, local)
        val date = sdf.format(Date())
        return "$date$fileExt"
    }
}