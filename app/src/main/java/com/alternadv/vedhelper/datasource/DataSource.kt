package com.alternadv.vedhelper.datasource

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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DataSource {
    const val BASE_URL = "https://alternadv.com"

    private val client = OkHttpClient()
    val moshi = Moshi.Builder()
        .add(FlexibleStringAdapter)
        .add(FlexibleBooleanAdapter)
        .add(EmptyObjectAsArrayAdapterFactory())
        .add(KotlinJsonAdapterFactory())
        .build()!!

    inline fun <reified T> parameterizedType(): Type =
        Types.newParameterizedType(ResponseModel::class.java, T::class.java)

    suspend inline fun <reified T> get(url: String): ResponseModel<T>? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val responseStr = executeAsync(request)
        val adapter = moshi.adapter<ResponseModel<T>>(parameterizedType<T>())
        return adapter.fromJson(responseStr)
    }

    suspend inline fun <reified T> getRaw(url: String): T? {
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

    fun buildUrl(path: String, queryParams: Map<String, String>): String {
        val query = queryParams
            .filterValues { it.isNotEmpty() }
            .map { (key, value) -> "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}" }
            .joinToString("&")

        return "${BASE_URL}/$path${if (query.isNotEmpty()) "?$query&json=1" else "?json=1"}"
    }

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
                            cont.resume(it.body.string() ?: "")
                        }
                    }
                }
            })
        }
}