package com.alternadv.vedhelper.utils

import com.squareup.moshi.*

object FlexibleStringAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> reader.nextString()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
            JsonReader.Token.BEGIN_OBJECT,
            JsonReader.Token.BEGIN_ARRAY -> throw JsonDataException("Expected STRING but was ${reader.peek()}")
            else -> throw JsonDataException("Unexpected token ${reader.peek()}")
        }
    }

    @ToJson
    fun toJson(value: String?): String? = value
}
