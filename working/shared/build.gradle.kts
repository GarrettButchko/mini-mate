import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("co.touchlab.skie") version "0.10.11"
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    skie {
        features {
            defaultArgumentsInExternalLibraries.set(true)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {

        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.kermit)
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // SKIE Annotations
            implementation(libs.configuration.annotations)

            // Room SQLite
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Firebase Multiplatform
            implementation(libs.firebase.common)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.database)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            api(project.dependencies.platform(libs.koin.bom))
            api("io.insert-koin:koin-core")
        }

        androidMain.dependencies {

            implementation(libs.androidx.material.icons.extended)
            implementation(libs.coil.compose)
            implementation(libs.places)
            implementation(libs.maps.compose)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.auth)
            implementation(libs.google.id)
            implementation(libs.bytebeats.compose.charts)
            api("io.insert-koin:koin-android")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.io.insert.koin.koin.test)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.garrettbutchko.minimate.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    "kspCommonMainMetadata"(libs.koin.compiler)
    
    // Applying Room compiler to all targets explicitly
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
