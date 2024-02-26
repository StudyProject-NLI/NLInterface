package com.nlinterface.utility

/**
 * Specifies the Locale. Required for TTS and STT language settings.
 */
enum class LocaleType {
    DE,
    EN,
    OTHER
}

/**
 * Specifies the activity. One value for each activity, corresponding to the activity name
 */
enum class ActivityType {
    MAIN,
    GROCERYLIST,
    PLACEDETAILS,
    CLASSIFICATION,
    SETTINGS
}

/**
 * Specifies the input type of a STT interaction.
 */
enum class STTInputType {
    COMMAND,
    ANSWER
}

