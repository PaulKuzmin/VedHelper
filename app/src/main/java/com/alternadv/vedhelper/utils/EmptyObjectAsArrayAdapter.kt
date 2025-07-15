package com.alternadv.vedhelper.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.JsonDataException

class EmptyObjectAsArrayAdapter<T>(
    private val delegate: JsonAdapter<T>
) : JsonAdapter<T>() {

    override fun toJson(writer: JsonWriter, value: T?) {
        delegate.toJson(writer, value)
    }

    override fun fromJson(reader: JsonReader): T? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                return if (reader.hasNext()) {
                    throw JsonDataException("Non-empty array received where object or null was expected")
                } else {
                    reader.endArray()
                    null
                }
            }

            JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)

            JsonReader.Token.NULL -> reader.nextNull()

            else -> throw JsonDataException("Expected object or empty array for null, but was ${reader.peek()}")
        }
    }
}