package com.nlinterface.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.nlinterface.R
import java.util.Locale


open class GlobalParameters protected constructor() {

    var keepScreenOn: KeepScreenOn = KeepScreenOn.NO
    var themeChoice: ThemeChoice = ThemeChoice.SYSTEM_DEFAULT
    var barcodeServiceMode: BarcodeServiceMode = BarcodeServiceMode.OFF

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

    enum class BarcodeServiceMode {
        ON,
        OFF
    }

    var navState: NavState = NavState.YES
    var labelsState: LabelsState = LabelsState.NO
    var cooState: CooState = CooState.NO
    var iaaState: IaaState = IaaState.YES
    var snvState: SnvState = SnvState.NO

    enum class NavState {
        YES,
        NO
    }
    enum class LabelsState {
        YES,
        NO
    }
    enum class CooState {
        YES,
        NO
    }
    enum class IaaState {
        YES,
        NO
    }
    enum class SnvState {
        YES,
        NO
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
        instance!!.keepScreenOn = KeepScreenOn.valueOf(prefKeepScreenOn!!)

        val prefTheme = sharedPref.getString(
            context.resources.getString(R.string.settings_theme_key),
            ThemeChoice.SYSTEM_DEFAULT.toString()
        )
        instance!!.themeChoice = ThemeChoice.valueOf(prefTheme!!)

        val prefBarcodeServiceMode = sharedPref.getString(
            context.resources.getString(R.string.barcode_service_mode_key),
            BarcodeServiceMode.OFF.toString()
        )
        instance!!.barcodeServiceMode = BarcodeServiceMode.valueOf(prefBarcodeServiceMode!!)

        val prefNameAndVolume = sharedPref.getString(
            context.resources.getString(R.string.settings_name_and_volume_key),
            NavState.YES.toString()
        )
        instance!!.navState = NavState.valueOf(prefNameAndVolume!!)

        val prefLabels = sharedPref.getString(
            context.resources.getString(R.string.settings_labels_key),
            LabelsState.NO.toString()
        )
        instance!!.labelsState = LabelsState.valueOf(prefLabels!!)

        val prefCountryOfOrigin = sharedPref.getString(
            context.resources.getString(R.string.settings_country_of_origin_key),
            CooState.NO.toString()
        )
        instance!!.cooState = CooState.valueOf(prefCountryOfOrigin!!)

        val prefIngredients = sharedPref.getString(
            context.resources.getString(R.string.settings_ingredients_and_allergies_key),
            IaaState.YES.toString()
        )
        instance!!.iaaState = IaaState.valueOf(prefIngredients!!)

        val prefNutritionalValues = sharedPref.getString(
                context.resources.getString(R.string.settings_short_nutritional_values_key),
                SnvState.NO.toString()
        )
        instance!!.snvState = SnvState.valueOf(prefNutritionalValues!!)
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