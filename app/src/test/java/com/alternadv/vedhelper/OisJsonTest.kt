package com.alternadv.vedhelper

import com.alternadv.vedhelper.model.OisResultModel
import com.alternadv.vedhelper.utils.EmptyObjectAsArrayAdapterFactory
import com.alternadv.vedhelper.utils.OisImageAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Ответ /widget/ois/list/{text}: image приходит объектом {src,width,height,placeholder},
 * пустой строкой (картинки нет) или старой строкой-путём. Раньше модель ждала String и весь поиск РОИС
 * падал на разборе — тесты фиксируют совместимость со всеми тремя формами.
 */
class OisJsonTest {

    private val adapter = Moshi.Builder()
        .add(EmptyObjectAsArrayAdapterFactory())
        .add(OisImageAdapterFactory())
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(OisResultModel::class.java)

    @Test
    fun imageObjectIsParsed() {
        val json = """
            {"ois_text":"apple","ois_list":[{"regnom":"05516/08658-001/ТЗ-270521",
              "g31_12":"MACBOOK","image":{"src":"/img-ois/05516/08658-001/%D0%A2%D0%97-270521",
              "width":89,"height":14,"placeholder":""}}]}
        """.trimIndent()
        val result = adapter.fromJson(json)!!
        assertEquals("/img-ois/05516/08658-001/%D0%A2%D0%97-270521", result.oisList!![0].image?.src)
        assertEquals(89, result.oisList!![0].image?.width)
    }

    @Test
    fun emptyImageStringMeansNoImage() {
        val json = """{"ois_text":"x","ois_list":[{"regnom":"1","image":""}]}"""
        assertNull(adapter.fromJson(json)!!.oisList!![0].image)
    }

    @Test
    fun legacyStringImageIsParsed() {
        val json = """{"ois_text":"x","ois_list":[{"regnom":"1","image":"/img/ois/1.jpg"}]}"""
        assertEquals("/img/ois/1.jpg", adapter.fromJson(json)!!.oisList!![0].image?.src)
    }

    @Test
    fun noImageFieldIsNull() {
        val json = """{"ois_text":"x","ois_list":[{"regnom":"1"}]}"""
        assertNull(adapter.fromJson(json)!!.oisList!![0].image)
    }
}
