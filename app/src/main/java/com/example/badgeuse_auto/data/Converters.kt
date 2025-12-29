package com.example.badgeuse_auto.data

import androidx.room.TypeConverter

class Converters {

    // 🔹 ThemeMode
    @TypeConverter
    fun fromThemeMode(mode: ThemeMode): String = mode.name

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode =
        ThemeMode.valueOf(value)
}
