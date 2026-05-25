package com.garrettbutchko.minimate.viewModelsAndroidAddOns

import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel
import dev.gitlive.firebase.storage.Data

fun CourseSettingsViewModel.uploadLogoImageAndroid(bytes: ByteArray, course: Course, onCourseUpdated: (Course) -> Unit) {
    uploadLogoImage(Data(bytes), course, onCourseUpdated)
}

fun CourseSettingsViewModel.uploadAdImageAndroid(bytes: ByteArray, course: Course, onCourseUpdated: (Course) -> Unit) {
    uploadAdImage(Data(bytes), course, onCourseUpdated)
}
