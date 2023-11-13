package com.nlinterface.utility

import android.util.Log

class VoiceCommandHelper {

    fun decodeVoiceCommand(string: String): ArrayList<String> {

        val input = cleanVoiceInput(string)

        return formatCommands(input)

    }

    private fun formatCommands(input: String): ArrayList<String> {

        val command: ArrayList<String> = if (getLocaleType() == LocaleType.DE) {
            formatCommandsDE(input)
        } else if (getLocaleType() == LocaleType.EN) {
            formatCommandsEN(input)
        } else {
            Log.println(Log.DEBUG, "formatCommands", "Locale = Locale.OTHER")
            formatCommandsEN(input)
        }

        return command

    }

    private fun formatCommandsEN(input: String): ArrayList<String> {

        val command = ArrayList<String>()

        if (input.contains(Regex("go to"))) {
            command.add("GOTO")
            if (input.contains(Regex("main menu"))) {
                command.add(ActivityType.MAIN.toString())
            } else if (input.contains(Regex("grocery list"))) {
                command.add(ActivityType.GROCERYLIST.toString())
            } else if (input.contains(Regex("place details"))) {
                command.add(ActivityType.PLACEDETAILS.toString())
            } else if (input.contains(Regex("settings"))) {
                command.add(ActivityType.SETTINGS.toString())
            } else {
                command.add("")
            }
        } else {
            command.add("")
        }

        command.add("")

        return command
    }

        private fun formatCommandsDE(input: String): ArrayList<String> {

            val command = ArrayList<String>()

            if (input.contains(Regex("gehe zu"))) {
                command.add("GOTO")
                if (input.contains(Regex("hauptmenü"))) {
                    command.add(ActivityType.MAIN.toString())
                } else if (input.contains(Regex("einkaufsliste"))) {
                    command.add(ActivityType.GROCERYLIST.toString())
                } else if (input.contains(Regex("ort details"))) {
                    command.add(ActivityType.PLACEDETAILS.toString())
                } else if (input.contains(Regex("einstellungen"))) {
                    command.add(ActivityType.SETTINGS.toString())
                } else {
                    command.add("")
                }
            } else {
                command.add("")
            }

            command.add("")

            return command

        }

    private fun cleanVoiceInput(input: String): String {

        val punctuationRegex = Regex("[^\\w\\süäöß]")

        var cleanInput = input.lowercase()
        cleanInput = cleanInput.replace(punctuationRegex, "")

        cleanInput = if (getLocaleType() == LocaleType.EN) {
            cleanVoiceInputEN(cleanInput)
        } else if (getLocaleType() == LocaleType.DE) {
            cleanVoiceInputDE(cleanInput)
        } else {
            Log.println(Log.DEBUG, "cleanVoiceInput", "Locale = Locale.OTHER")
            cleanVoiceInputEN(cleanInput)
        }

        return cleanInput

    }
    private fun cleanVoiceInputEN(input: String): String {

        val word2Digit = mapOf(
            "a" to 1,
            "an" to 1,
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6
        )

        return input.replace(word2Digit)

    }

    private fun cleanVoiceInputDE(input: String): String {

        val word2Digit = mapOf(
            "ein" to 1,
            "eine" to 1,
            "einen" to 1,
            "zwei" to 2,
            "drei" to 3,
            "view" to 4,
            "fünf" to 5,
            "sechs" to 6)

        return input.replace(word2Digit)

    }

    private fun String.replace(map: Map<String, Int>): String {
        var result = this
        map.forEach { (k, v) -> result = result.replace(k, v) }
        return result
    }

    private fun String.replace(k: String, v: Int): String {
        val result = this
        result.replace(k, v.toString())
        return result
    }







}