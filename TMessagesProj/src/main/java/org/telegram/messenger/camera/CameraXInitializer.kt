package org.telegram.messenger.camera

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import org.telegram.messenger.FileLog
import java.util.concurrent.atomic.AtomicBoolean

object CameraXInitializer {

    private val warming = AtomicBoolean(false)

    @JvmStatic
    fun warmUp(context: Context?) {
        if (context == null || !warming.compareAndSet(false, true)) {
            return
        }

        try {
            val appContext = context.applicationContext
            val future = ProcessCameraProvider.getInstance(appContext)
            future.addListener(
                {
                    try {
                        future.get()
                    } catch (t: Throwable) {
                        FileLog.e(t)
                    } finally {
                        warming.set(false)
                    }
                },
                ContextCompat.getMainExecutor(appContext)
            )
        } catch (t: Throwable) {
            warming.set(false)
            FileLog.e(t)
        }
    }
}
