package com.alternadv.vedhelper.datasource

import android.util.Log
import com.alternadv.vedhelper.BuildConfig
import com.alternadv.vedhelper.model.ResponseModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.alternadv.vedhelper.utils.EmptyObjectAsArrayAdapterFactory
import com.alternadv.vedhelper.utils.FlexibleBooleanAdapter
import com.alternadv.vedhelper.utils.FlexibleStringAdapter
import java.io.IOException
import java.lang.reflect.Type
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DataSource {
    /**
     * Адрес API берётся из сборки: debug → локальный dev, release → прод.
     * Задаётся в app/build.gradle.kts (buildConfigField API_BASE_URL).
     */
    val BASE_URL: String = BuildConfig.API_BASE_URL.trimEnd('/')

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()
    val moshi = Moshi.Builder()
        .add(FlexibleStringAdapter)
        .add(FlexibleBooleanAdapter)
        .add(EmptyObjectAsArrayAdapterFactory())
        .add(KotlinJsonAdapterFactory())
        .build()!!

    inline fun <reified T> parameterizedType(): Type =
        Types.newParameterizedType(ResponseModel::class.java, T::class.java)

    suspend inline fun <reified T> get(url: String): ResponseModel<T>? {

        Log.d("DataSource", "GET: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val responseStr = executeAsync(request)
        val adapter = moshi.adapter<ResponseModel<T>>(parameterizedType<T>())
        return adapter.fromJson(responseStr)
    }

    suspend inline fun <reified T> getRaw(url: String): T? {

        Log.d("DataSource", "GET: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val responseStr = executeAsync(request)
        val adapter = moshi.adapter(T::class.java)
        return adapter.fromJson(responseStr)
    }

    suspend inline fun <reified T> post(url: String, payload: Any): ResponseModel<T>? {
        val jsonAdapter = moshi.adapter(Any::class.java)
        val json = jsonAdapter.toJson(payload)

        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val responseStr = executeAsync(request)
        val adapter = moshi.adapter<ResponseModel<T>>(parameterizedType<T>())
        return adapter.fromJson(responseStr)
    }

    /**
     * Собирает URL: кодирует сегменты пути и параметры, добавляет json=1.
     *
     * ВАЖНО: вызывающий код передаёт путь и значения СЫРЫМИ (без URLEncoder). Сегменты пути
     * кодируются здесь, причём пробел становится %20, а не «+» (плюс в пути означает плюс,
     * а не пробел — раньше поиск «стиральная машина» уходил как «стиральная+машина»).
     */
    fun buildUrl(path: String, queryParams: Map<String, String>): String {
        val encodedPath = path.trimStart('/')
            .split('/')
            .joinToString("/") { encodeSegment(it) }

        val query = queryParams
            .filterValues { it.isNotEmpty() }
            .map { (key, value) -> "${encodeSegment(key)}=${encodeSegment(value)}" }
            .joinToString("&")

        return "${BASE_URL}/$encodedPath${if (query.isNotEmpty()) "?$query&json=1" else "?json=1"}"
    }

    private fun encodeSegment(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    suspend fun executeAsync(request: Request): String =
        suspendCancellableCoroutine { cont ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isCancelled) return
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            cont.resumeWithException(IOException("HTTP error: ${it.code}"))
                        } else {
                            cont.resume(it.body.string())
                        }
                    }
                }
            })
        }
}