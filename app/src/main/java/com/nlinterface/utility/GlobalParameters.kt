package com.nlinterface.utility

import android.widget.TextView

open class GlobalParameters protected constructor() {
    var language: Language = Language.EN
    var visualImpairment: VisualImpairment = VisualImpairment.BLIND
    var colorChoice: ColorChoice = ColorChoice.DEFAULT
    var layoutSwitch : Boolean = false
    var voiceCommandTrigger: VoiceCommandTrigger = VoiceCommandTrigger.BUTTON

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

    fun changeTextLanguage(texts: Array<TextView?>, newStrings: Array<String?>)
    {
        texts.forEachIndexed { i, text ->
            text!!.text = newStrings[i].toString()
        }
    }
}