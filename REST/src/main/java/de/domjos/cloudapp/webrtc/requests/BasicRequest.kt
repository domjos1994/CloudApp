package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.ignoreIoExceptions
import java.io.IOException
import java.util.concurrent.TimeUnit


open class BasicRequest(authentication: Authentication?, urlPart: String) {
    private val jsonType: MediaType = "application/json".toMediaType()
    @OptIn(ExperimentalSerializationApi::class)
    protected val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        ignoreIoExceptions {  }
    }
    protected val client: OkHttpClient
    private val url: String

    init {
        if(authentication!=null) {
            this.client = OkHttpClient.Builder()
                .addInterceptor(BasicAuthInterceptor(authentication.userName, authentication.password))
                .callTimeout(120L, TimeUnit.SECONDS)
                .readTimeout(120L, TimeUnit.SECONDS)
                .writeTimeout(300L, TimeUnit.SECONDS)
                .connectTimeout(120L, TimeUnit.SECONDS)
                .build()
            this.url = "${authentication.url}$urlPart"
        } else {
            this.client = OkHttpClient()
            this.url = ""
        }
    }

    protected fun buildRequest(endPoint: String, method: String, body: String?): Request? {
        if(this.url != "") {
            if(method.lowercase()=="get") {
                return Request.Builder()
                    .url("$url$endPoint")
                    .addHeader("OCS-APIRequest", "true")
                    .build()
            } else if(method.lowercase()=="post") {
                return if(body==null) {
                    Request.Builder()
                        .url("${this.url}$endPoint")
                        .addHeader("OCS-APIRequest", "true")
                        .build()
                } else {
                    return Request.Builder()
                        .url("${this.url}$endPoint")
                        .addHeader("OCS-APIRequest", "true")
                        .post(body.toRequestBody(jsonType))
                        .build()
                }
            } else if(method.lowercase()=="delete") {
                return Request.Builder()
                    .url("${this.url}$endPoint")
                    .addHeader("OCS-APIRequest", "true")
                    .delete(body?.toRequestBody(jsonType))
                    .build()
            } else if(method.lowercase()=="patch") {
                return Request.Builder()
                    .url("${this.url}$endPoint")
                    .addHeader("OCS-APIRequest", "true")
                    .patch(body?.toRequestBody(jsonType)!!)
                    .build()
            } else if(method.lowercase()=="put") {
                return Request.Builder()
                    .url("${this.url}$endPoint")
                    .addHeader("OCS-APIRequest", "true")
                    .put(body?.toRequestBody(jsonType)!!)
                    .build()
            }
        }
        return null
    }
}


class BasicAuthInterceptor(user: String, password: String) : Interceptor {
    private val credentials: String

    init {
        credentials = Credentials.basic(user, password)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val authenticatedRequest: Request = request.newBuilder()
            .header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }
}