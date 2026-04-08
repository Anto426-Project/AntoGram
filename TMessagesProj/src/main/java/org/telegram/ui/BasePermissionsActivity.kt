package org.telegram.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.RawRes
import androidx.fragment.app.FragmentActivity
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.ContactsController
import org.telegram.messenger.FileLog
import org.telegram.messenger.ImageLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.camera.CameraXController
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.AlertsCreator

open class BasePermissionsActivity : FragmentActivity() {

    companion object {
        const val REQUEST_CODE_GEOLOCATION = 2
        const val REQUEST_CODE_EXTERNAL_STORAGE = 4
        const val REQUEST_CODE_ATTACH_CONTACT = 5
        const val REQUEST_CODE_CALLS = 7
        const val REQUEST_CODE_OPEN_CAMERA = 20
        const val REQUEST_CODE_VIDEO_MESSAGE = 150
        const val REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR = 151
        const val REQUEST_CODE_SIGN_IN_WITH_GOOGLE = 200
        const val REQUEST_CODE_PAYMENT_FORM = 210
        const val REQUEST_CODE_MEDIA_GEO = 211
    }

    @JvmField
    protected var currentAccount = -1

    protected open fun checkPermissionsResult(
        requestCode: Int,
        permissions: Array<String>?,
        grantResults: IntArray?
    ): Boolean {
        val safeGrantResults = grantResults ?: IntArray(0)
        val safePermissions = permissions ?: emptyArray()
        val granted = safeGrantResults.isNotEmpty() && safeGrantResults[0] == PackageManager.PERMISSION_GRANTED

        if (requestCode == 104) {
            if (granted) {
                GroupCallActivity.groupCallInstance?.enableCamera()
            } else {
                showPermissionErrorAlert(
                    R.raw.permission_request_camera,
                    LocaleController.getString(R.string.VoipNeedCameraPermission)
                )
            }
        } else if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE || requestCode == REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR) {
            if (!granted) {
                showPermissionErrorAlert(
                    R.raw.permission_request_folder,
                    if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR) {
                        LocaleController.getString(R.string.PermissionNoStorageAvatar)
                    } else {
                        LocaleController.getString(R.string.PermissionStorageWithHint)
                    }
                )
            } else {
                ImageLoader.getInstance().checkMediaPaths()
            }
        } else if (requestCode == REQUEST_CODE_ATTACH_CONTACT) {
            if (!granted) {
                showPermissionErrorAlert(
                    R.raw.permission_request_contacts,
                    LocaleController.getString(R.string.PermissionNoContactsSharing)
                )
                return false
            } else {
                ContactsController.getInstance(currentAccount).forceImportContacts()
            }
        } else if (requestCode == 3 || requestCode == REQUEST_CODE_VIDEO_MESSAGE) {
            var audioGranted = true
            var cameraGranted = true
            val size = minOf(safePermissions.size, safeGrantResults.size)
            for (i in 0 until size) {
                if (Manifest.permission.RECORD_AUDIO == safePermissions[i]) {
                    audioGranted = safeGrantResults[i] == PackageManager.PERMISSION_GRANTED
                } else if (Manifest.permission.CAMERA == safePermissions[i]) {
                    cameraGranted = safeGrantResults[i] == PackageManager.PERMISSION_GRANTED
                }
            }
            if (requestCode == REQUEST_CODE_VIDEO_MESSAGE && (!audioGranted || !cameraGranted)) {
                showPermissionErrorAlert(
                    R.raw.permission_request_camera,
                    LocaleController.getString(R.string.PermissionNoCameraMicVideo)
                )
            } else if (!audioGranted) {
                showPermissionErrorAlert(
                    R.raw.permission_request_microphone,
                    LocaleController.getString(R.string.PermissionNoAudioWithHint)
                )
            } else if (!cameraGranted) {
                showPermissionErrorAlert(
                    R.raw.permission_request_camera,
                    LocaleController.getString(R.string.PermissionNoCameraWithHint)
                )
            } else {
                if (SharedConfig.inappCamera) {
                    CameraXController.warmUp(this)
                }
                return false
            }
        } else if (requestCode == 18 || requestCode == 19 || requestCode == REQUEST_CODE_OPEN_CAMERA || requestCode == 22) {
            if (!granted) {
                showPermissionErrorAlert(
                    R.raw.permission_request_camera,
                    LocaleController.getString(R.string.PermissionNoCameraWithHint)
                )
            } else if (SharedConfig.inappCamera) {
                CameraXController.warmUp(this)
            }
        } else if (requestCode == REQUEST_CODE_GEOLOCATION) {
            NotificationCenter.getGlobalInstance().postNotificationName(
                if (granted) NotificationCenter.locationPermissionGranted else NotificationCenter.locationPermissionDenied
            )
        } else if (requestCode == REQUEST_CODE_MEDIA_GEO) {
            NotificationCenter.getGlobalInstance().postNotificationName(
                if (granted) NotificationCenter.locationPermissionGranted else NotificationCenter.locationPermissionDenied,
                1
            )
        }
        return true
    }

    open fun createPermissionErrorAlert(@RawRes animationId: Int, message: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setTopAnimation(
                animationId,
                AlertsCreator.PERMISSIONS_REQUEST_TOP_ICON_SIZE,
                false,
                Theme.getColor(Theme.key_dialogTopBackground)
            )
            .setMessage(AndroidUtilities.replaceTags(message))
            .setPositiveButton(LocaleController.getString(R.string.PermissionOpenSettings)) { _, _ ->
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:${ApplicationLoader.applicationContext.packageName}")
                    startActivity(intent)
                } catch (e: Exception) {
                    FileLog.e(e)
                }
            }
            .setNegativeButton(LocaleController.getString(R.string.ContactsPermissionAlertNotNow), null)
            .create()
    }

    private fun showPermissionErrorAlert(@RawRes animationId: Int, message: String) {
        createPermissionErrorAlert(animationId, message).show()
    }
}
