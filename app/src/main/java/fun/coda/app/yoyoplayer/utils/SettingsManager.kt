package `fun`.coda.app.yoyoplayer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit { putString(KEY_BASE_URL, value) }

    fun resetBaseUrl() {
        baseUrl = DEFAULT_BASE_URL
    }

    companion object {
        private const val PREFS_NAME = "yoyo_settings"
        private const val KEY_BASE_URL = "base_url"
        const val DEFAULT_BASE_URL = "https://gitee.com/nowszhao/yoyo/raw/master/test.json"
    }
} 