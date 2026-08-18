package com.garrettbutchko.minimate.utilities

import co.touchlab.kermit.Logger

object AppLogger {
    fun error(tag: String, message: String) {
        Logger.withTag(tag).e { "❌ $message" }
    }

    fun error(tag: String, message: String, throwable: Throwable) {
        Logger.withTag(tag).e(throwable) { "❌ $message" }
    }

    fun warning(tag: String, message: String) {
        Logger.withTag(tag).w { "⚠️ $message" }
    }

    fun warning(tag: String, message: String, throwable: Throwable) {
        Logger.withTag(tag).w(throwable) { "⚠️ $message" }
    }

    fun success(tag: String, message: String) {
        Logger.withTag(tag).i { "✅ $message" }
    }

    fun info(tag: String, message: String) {
        Logger.withTag(tag).i { "ℹ️ $message" }
    }

    fun debug(tag: String, message: String) {
        Logger.withTag(tag).d { "🔧 $message" }
    }
}
