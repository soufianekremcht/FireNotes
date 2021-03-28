package com.soufianekre.firenotes.helper

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import es.dmoral.toasty.Toasty
import timber.log.Timber


object PermissionHelper {


    fun  getStoragePermission(context :Context){
        val onDeniedlistener  =
            DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(context)
                .withTitle("Storage permission")
                .withMessage("Storage permissions are needed to Import & Export Notes")
                .withButtonText(android.R.string.ok)
                .build()
        var listener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()){
                    Toasty.success(context,"Storage Permissions Granted",Toasty.LENGTH_SHORT).show()
                }else{
                    Toasty.error(context,"Storage Permissions Granted",Toasty.LENGTH_SHORT).show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest?>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }

        var compositeListener = CompositeMultiplePermissionsListener(listener,onDeniedlistener)


        Dexter.withContext(context).withPermissions(Manifest.permission_group.STORAGE)
            .withListener(compositeListener)
            .withErrorListener{
                Timber.e(it.name)
            }
            .check()
    }

    fun checkStoragePermissions() : Boolean{
        return false
    }
}