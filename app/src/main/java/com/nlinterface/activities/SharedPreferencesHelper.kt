package com.nlinterface.activities

import android.content.Context

object SharedPreferencesHelper {
    private const val SHARED_PREF_NAME = "SavedLocations"

    fun saveLocations(context: Context, locations: List<String>) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("locations", locations.toSet())
        editor.apply()
    }

    fun getSavedLocations(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("locations", emptySet())?.toList() ?: emptyList()
    }
}