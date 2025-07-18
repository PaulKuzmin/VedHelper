package com.alternadv.vedhelper.utils

import com.alternadv.vedhelper.model.CalcInfo
import com.alternadv.vedhelper.model.CalcRateTypeModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class EmptyObjectAsArrayAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = Types.getRawType(type)

        // Поддерживаем только классы, которые могут приходить как пустой массив или объект
        if (
            rawType == CalcRateTypeModel::class.java ||
            rawType == CalcInfo::class.java ||
            Map::class.java.isAssignableFrom(rawType)
        ) {
            val delegate: JsonAdapter<Any> = moshi.nextAdapter(this, type, annotations)
            return EmptyObjectAsArrayAdapter(delegate)
        }

        return null
    }
}