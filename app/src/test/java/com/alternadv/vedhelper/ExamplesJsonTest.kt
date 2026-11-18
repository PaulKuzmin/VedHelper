package com.alternadv.vedhelper

import com.alternadv.vedhelper.datasource.DataSource
import com.alternadv.vedhelper.model.ExamplesModel
import com.alternadv.vedhelper.model.ResponseModel
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Разбор ответа widget/calculator/examples/{text}. Фикстура — реальный ответ dev 18.11.2026
 * на «обувь» (первые 3 позиции). Поля `rel` в ответе нет ни на dev, ни на проде: пока модель
 * требовала его, разбор падал и экран «Примеры» на любой запрос писал «ничего не найдено».
 */
class ExamplesJsonTest {

    private fun parse(body: String): ExamplesModel {
        val type = Types.newParameterizedType(ResponseModel::class.java, ExamplesModel::class.java)
        return requireNotNull(DataSource.moshi.adapter<ResponseModel<ExamplesModel>>(type).fromJson(body)).data
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource("fixtures/$name")!!.readText()

    @Test
    fun `ответ поиска примеров без поля rel разбирается`() {
        val model = parse(fixture("examples-obuv.json"))

        assertEquals(3, model.data.size)
        assertEquals("6402993900", model.data.first().code)
        assertTrue(model.data.all { it.name.contains("ОБУВ") })
        assertNull(model.data.first().rel)
    }

    @Test
    fun `поле rel, если сервер его вернёт, тоже читается`() {
        val body = """{"success":true,"data":{"searched_str":"6403","data":[
            {"id":"0","code":"6403120000","name":"ЛЫЖНЫЕ БОТИНКИ","rel":"x"}]}}"""
        assertEquals("x", parse(body).data.single().rel)
    }
}
