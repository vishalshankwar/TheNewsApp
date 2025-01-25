package com.example.thenewsapp.dp

import androidx.room.TypeConverter
import com.example.thenewsapp.models.Source
import kotlin.contracts.Returns

class Converters {
    @TypeConverter
    fun fromSource(source: Source):String{
        return source.name
    }
    @TypeConverter
    fun toSource(name: String):Source{
        return Source(name,name)
    }
}