package com.alternadv.vedhelper.utils

import com.squareup.moshi.*

object FlexibleBooleanAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Boolean? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.STRING -> {
                val str = reader.nextString().trim()
                when (str.lowercase()) {
                    "" -> null
                    "true", "1" -> true
                    "false", "0" -> false
                    else -> throw JsonDataException("Invalid boolean string value: '$str'")
                }
            }
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> {
                when (reader.nextInt()) {
                    1 -> true
                    0 -> false
                    else -> throw JsonDataException("Invalid boolean numeric value")
                }
            }
            else -> throw JsonDataException("Unexpected token type: ${reader.peek()}")
        }
    }

    @ToJson
    fun toJson(value: Boolean?): Any? = value
}
