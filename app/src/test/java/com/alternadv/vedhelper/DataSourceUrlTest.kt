package com.alternadv.vedhelper

import com.alternadv.vedhelper.datasource.DataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Сборка URL запроса (DataSource.buildUrl).
 *
 * Что проверяем:
 *  • json=1 добавляется сам и ровно один раз;
 *  • пустые значения выбрасываются;
 *  • кириллица и спецсимволы кодируются;
 *  • базовый адрес берётся из сборки (debug → dev, release → прод).
 */
class DataSourceUrlTest {

    @Test
    fun `json=1 добавляется к запросу без параметров`() {
        val url = DataSource.buildUrl("widget/calcauto/vehicles", emptyMap())

        assertEquals("${DataSource.BASE_URL}/widget/calcauto/vehicles?json=1", url)
        assertTrue(url.endsWith("?json=1"))
    }

    @Test
    fun `json=1 добавляется к запросу с параметрами`() {
        val url = DataSource.buildUrl("widget/calcauto/params/car", mapOf("vehicle" to "car"))

        assertEquals("${DataSource.BASE_URL}/widget/calcauto/params/car?vehicle=car&json=1", url)
    }

    @Test
    fun `пустые значения не попадают в url`() {
        val url = DataSource.buildUrl(
            "widget/calcauto/result/car",
            mapOf("cost" to "15000", "engine" to "", "capacity" to "")
        )

        assertTrue("cost есть", url.contains("cost=15000"))
        assertFalse("пустой engine выброшен", url.contains("engine="))
        assertFalse("пустая capacity выброшена", url.contains("capacity="))
    }

    @Test
    fun `порядок параметров сохраняется`() {
        val url = DataSource.buildUrl(
            "widget/calcauto/result/car",
            linkedMapOf("vehicle" to "car", "cost" to "15000", "currency" to "978")
        )

        assertTrue(url.endsWith("vehicle=car&cost=15000&currency=978&json=1"))
    }

    @Test
    fun `кириллица в пути кодируется`() {
        val url = DataSource.buildUrl("widget/calculator/examples/авто", emptyMap())

        assertFalse("в url не должно быть сырой кириллицы", url.contains("авто"))
        assertTrue("ожидаем percent-encoding", url.contains("%D0%B0%D0%B2%D1%82%D0%BE"))
    }

    @Test
    fun `пробел в пути кодируется как %20, а не плюс`() {
        // раньше вызывающий код кодировал сам через URLEncoder и получал «стиральная+машина»:
        // в пути плюс означает плюс, а не пробел — сервер искал не то
        val url = DataSource.buildUrl("widget/calculator/examples/стиральная машина", emptyMap())

        assertTrue("пробел должен быть %20", url.contains("%20"))
        assertFalse("плюса в пути быть не должно", url.contains("+"))
    }

    @Test
    fun `сегменты пути не кодируются дважды`() {
        val url = DataSource.buildUrl("widget/calcauto/params/car", emptyMap())

        assertTrue(url.contains("widget/calcauto/params/car"))
        assertFalse("не должно появиться %25 (двойное кодирование %)", url.contains("%25"))
    }

    @Test
    fun `лишний слеш в начале пути не ломает url`() {
        val url = DataSource.buildUrl("/widget/ois/list/iphone", emptyMap())

        assertFalse("двойной слеш после хоста", url.substringAfter("://").contains("//"))
    }

    @Test
    fun `дробные значения передаются с точкой`() {
        val url = DataSource.buildUrl("widget/calcauto/result/car", mapOf("cost" to "1.6"))

        assertTrue(url.contains("cost=1.6"))
    }

    @Test
    fun `базовый адрес без хвостового слеша не даёт двойного слеша`() {
        val url = DataSource.buildUrl("widget/calcauto/vehicles", emptyMap())

        assertFalse("двойной слеш после хоста", url.replace("https://", "").replace("http://", "").contains("//"))
        assertFalse(DataSource.BASE_URL.endsWith("/"))
    }

    @Test
    fun `в debug-сборке базовый адрес указывает на dev`() {
        // unit-тесты гоняются в debug-варианте; release проверяется сборкой AAB
        assertTrue(
            "ожидали dev-адрес, получили ${DataSource.BASE_URL}",
            DataSource.BASE_URL == "http://10.0.2.2:8180" || DataSource.BASE_URL.startsWith("http://")
        )
    }
}
