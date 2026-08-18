@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.garrettbutchko.minimate.viewModelsiOSAddOns

import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel
import com.garrettbutchko.minimate.utilities.AppLogger
import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

fun CourseSettingsViewModel.uploadLogoImageIOS(newImage: UIImage?, course: Course?, onCourseUpdated: (Course) -> Unit) {

    if (course == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Missing Course")
        return
    }


    if (newImage == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Photo upload failed: Missing image")
        return
    }

    val jpegData = UIImageJPEGRepresentation(newImage, 0.8)
    if (jpegData == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Photo upload failed: Could not process image")
        return
    }

    val data = Data(jpegData)
    uploadLogoImage(data, course, onCourseUpdated)
}

fun CourseSettingsViewModel.uploadAdImageIOS(newImage: UIImage?, course: Course?, onCourseUpdated: (Course) -> Unit) {
    if (course == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Missing Course")
        return
    }

    if (newImage == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Photo upload failed: Missing image")
        return
    }

    val jpegData = UIImageJPEGRepresentation(newImage, 0.8)
    if (jpegData == null) {
        AppLogger.error("CourseSettingsViewModelIOS", "Photo upload failed: Could not process image")
        return
    }

    val data = Data(jpegData)
    uploadAdImage(data, course, onCourseUpdated)
}
