package com.alternadv.vedhelper.utils

import com.alternadv.vedhelper.model.OisImageModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.lang.reflect.Type
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Картинка ОИС приходит объектом {src,width,height,placeholder}, но бывает пустой строкой (нет картинки),
 * пустым массивом (старый формат) или строкой-путём (очень старый формат). Приводим всё к OisImageModel?
 */
class OisImageAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) != OisImageModel::class.java) return null
        val delegate = moshi.nextAdapter<OisImageModel>(this, type, annotations)
        return object : JsonAdapter<OisImageModel>() {
            override fun fromJson(reader: JsonReader): OisImageModel? = when (reader.peek()) {
                JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)
                JsonReader.Token.NULL -> reader.nextNull()
                JsonReader.Token.STRING -> reader.nextString().takeIf { it.isNotBlank() }?.let { OisImageModel(src = it) }
                JsonReader.Token.BEGIN_ARRAY -> {
                    reader.beginArray()
                    if (reader.hasNext()) throw JsonDataException("Non-empty array for image")
                    reader.endArray()
                    null
                }
                else -> throw JsonDataException("Unexpected image token: ${reader.peek()}")
            }

            override fun toJson(writer: JsonWriter, value: OisImageModel?) = delegate.toJson(writer, value)
        }
    }
}
