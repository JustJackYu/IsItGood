package com.juhyeonyu.isitgood.utils

import com.juhyeonyu.isitgood.BuildConfig

object Constants {
    // Per build type: debug → local emulator backend, release → deployed backend.
    // Configured in app/build.gradle.kts (buildConfigField).
    val BASE_URL = BuildConfig.BASE_URL
}
