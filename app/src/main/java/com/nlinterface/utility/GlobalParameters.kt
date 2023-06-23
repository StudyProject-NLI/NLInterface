package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import android.widget.TextView
import android.widget.Toast
import com.nlinterface.activities.SettingsActivity
import java.util.Locale


open class GlobalParameters protected constructor() {
    var language: Language = Language.EN
    var visualImpairment: VisualImpairment = VisualImpairment.BLIND
    var colorChoice: ColorChoice = ColorChoice.DEFAULT
    var layoutSwitch : Boolean = false
    var voiceCommandTrigger: VoiceCommandTrigger = VoiceCommandTrigger.BUTTON

    lateinit var locale: Locale

    // order of the items needs to be the same as in the respective dropdown menus
    enum class Language {
        EN,
        DE
    }

    enum class VisualImpairment {
        BLIND,
        PARTIALLY,
        COLOR
    }

    enum class ColorChoice {
        DEFAULT,
        RED_GREEN,
        HIGH_CONTRAST
    }

    enum class VoiceCommandTrigger {
        BUTTON,
        VOICE
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


}