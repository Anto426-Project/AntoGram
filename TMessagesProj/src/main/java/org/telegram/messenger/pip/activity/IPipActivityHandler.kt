package org.telegram.messenger.pip.activity

import android.app.PictureInPictureParams
import android.content.res.Configuration

interface IPipActivityHandler {
    fun onPictureInPictureRequested()
    fun onUserLeaveHint()
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, ignoredNewConfig: Configuration)
    fun onDestroy()
    fun onConfigurationChanged(ignoredNewConfig: Configuration)
    fun setPictureInPictureParams(params: PictureInPictureParams?)
}
