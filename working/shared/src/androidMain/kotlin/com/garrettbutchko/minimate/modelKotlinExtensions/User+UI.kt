package com.garrettbutchko.minimate.modelKotlinExtensions

import androidx.compose.ui.graphics.Color
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.dataModels.userModels.UserDTO

val UserDTO.ballColor: Color?
    get() = ballColorDT.toColor()

val UserModel.ballColor: Color?
    get() = ballColorDT.toColor()
