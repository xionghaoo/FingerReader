package com.ubtrobot.fingerreader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_CAMERA_PERMISSION = 1

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraTask()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSION)
    private fun cameraTask() {
        if (hasCameraPermission()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.instance())
                .commit()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "App需要相机权限，请授予",
                    RC_CAMERA_PERMISSION,
                    Manifest.permission.CAMERA
            )
        }
    }

    private fun hasCameraPermission() : Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }
}