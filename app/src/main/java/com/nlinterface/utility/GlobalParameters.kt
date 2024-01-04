package com.nlinterface.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.nlinterface.R
import java.util.Locale


open class GlobalParameters protected constructor() {

    var keepScreenOn: KeepScreenOn = KeepScreenOn.NO
    var themeChoice: ThemeChoice = ThemeChoice.SYSTEM_DEFAULT

    lateinit var locale: Locale

    // order of the items needs to be the same as in the respective dropdown menus

    enum class KeepScreenOn {
        NO,
        YES
    }

    enum class ThemeChoice {
        SYSTEM_DEFAULT,
        LIGHT,
        DARK
    }

    // make it a Singleton
    companion object {
        private var mInstance: GlobalParameters? = null

        @get:Synchronized
        val instance: GlobalParameters?
            get() {
                if (null == mInstance) {
                    mInstance = GlobalParameters()
                }
                return mInstance
            }
    }

    fun loadPreferences(context: Context) {
        // get saved data from SharedPreferences or create SharedPreferences
        // and set the respective GlobalParameters to the saved data
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            context.resources.getString(R.string.settings_preferences_key),
            Context.MODE_PRIVATE
        ) ?: return

        val prefKeepScreenOn = sharedPref.getString(
            context.resources.getString(R.string.settings_keep_screen_on_key),
            KeepScreenOn.NO.toString()
        )
        instance!!.keepScreenOn = GlobalParameters.KeepScreenOn.valueOf(prefKeepScreenOn!!)

        val prefTheme = sharedPref.getString(
            context.resources.getString(R.string.settings_theme_key),
            ThemeChoice.SYSTEM_DEFAULT.toString()
        )
        instance!!.themeChoice = GlobalParameters.ThemeChoice.valueOf(prefTheme!!)
    }

    fun updateTheme() {
        when (instance!!.themeChoice) {
            ThemeChoice.SYSTEM_DEFAULT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )

            ThemeChoice.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            ThemeChoice.DARK -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }


}