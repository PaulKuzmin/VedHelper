package com.alternadv.vedhelper.utils

import com.alternadv.vedhelper.model.CalcRateTypeModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class EmptyObjectAsArrayAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) == CalcRateTypeModel::class.java) {
            val delegate = moshi.nextAdapter<CalcRateTypeModel>(this, type, annotations)
            return EmptyObjectAsArrayAdapter(delegate)
        }
        return null
    }
}