package org.radarbase.redcap.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.radarbase.redcap.config.RedCapInfo
import org.radarbase.redcap.webapp.exception.RedcapOperationException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

/*
 * Copyright 2017 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Skeleton for creating a REDCap update handler.
 */
open class RedCapClient(private val redCapInfo: RedCapInfo) {
    private val httpClient = OkHttpClient()

    /**
     * Returns the REDCap API URL starting from the information contained in the
     * [RedCapTrigger].
     * @return [URL] to request REDCap API
     * @throws MalformedURLException in case the URL is malformed
     */
    @get:Throws(MalformedURLException::class)
    val apiUrl: URL
        get() = URL(redCapInfo.url.protocol, redCapInfo.url.host, redCapInfo.url.port,
            API_ROOT
        )

    @Throws(IOException::class)
    fun createRequest(params: Map<String, String>, token: String): Request {
        val body = FormBody.Builder().apply {
            params.forEach { entry -> add(entry.key, entry.value) }
            add(TOKEN_LABEL, token)
        }.build()

        return Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
    }

    open fun updateForm(formData: Set<RedCapInput>, recordId: Int): Boolean {
        val parameters = getFormUpdateParameters(formData)
        return try {
            val request = createRequest(parameters, redCapInfo.token!!)
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    LOGGER.info("Successful update for record {}", recordId)
                } else {
                    LOGGER.warn(getErrorMsg(response))
                }
                response.isSuccessful
            }
        } catch (exc: Exception) {
            throw RedcapOperationException(
                "Error updating RedCap form",
                exc
            )
        }
    }

    private fun getFormUpdateParameters(formData: Set<RedCapInput>) = mapOf(
        Pair(DATA_LABEL, convertRedCapInputSetToString(formData)),
        Pair("content", "record"),
        Pair("format", "json"),
        Pair("type", "eav"),
        Pair("overwriteBehavior", "overwrite"),
        Pair("returnContent", "count"),
        Pair("returnFormat", "json")
    )

    private fun convertRedCapInputSetToString(data: Set<RedCapInput>) =
        try {
            mapper.writeValueAsString(data)
        } catch (exc: JsonProcessingException) {
            throw RedcapOperationException(
                "Error while serializing RedCapInput",
                exc
            )
        }


    open fun fetchFormDataForId(
        fields: List<String>,
        recordId: Int
    ): MutableMap<String, String> {
        val records = listOf(recordId.toString())
        val parameters = getFormFetchParameters(fields, records)
        return try {
            val request = createRequest(parameters, redCapInfo.token!!)
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    LOGGER.info("Successful fetch for record {}", recordId)
                    val result = response.body()!!.string()
                    val data = JSONArray(result)[REDCAP_RESULT_INDEX].toString()
                    mapper.readValue(data, object : TypeReference<HashMap<String, String>>() {})
                } else {
                    LOGGER.warn(getErrorMsg(response))
                    mutableMapOf()
                }
            }
        } catch (exc: IOException) {
            throw RedcapOperationException(
                "Error fetching RedCap form",
                exc
            )
        } catch (exc: JSONException) {
            LOGGER.warn("The JSON response from Redcap could not be deserialized.", exc)
            mutableMapOf()
        }
    }

    private fun getErrorMsg(response: Response): String {
        var msg = "Request to Redcap was unsuccessful. Code: ${response.code()}, " +
                "Msg: ${response.message()}"
        val body = response.body();
        if(body != null) {
            msg += ", Reason: ${body.string()}"
        }
        return msg
    }

    private fun getFormFetchParameters(
        fields: List<String>,
        records: List<String>
    ): Map<String, String> {
        val parameters: MutableMap<String, String> = mutableMapOf()
        parameters["content"] = "record"
        parameters["format"] = "json"
        parameters["type"] = "flat"
        parameters["rawOrLabel"] = "label"
        parameters.putAll(encodeListParams(fields, FIELDS_LABEL))
        parameters.putAll(encodeListParams(records, RECORDS_LABEL))
        return parameters
    }

    private fun encodeListParams(data: List<String>, label: String) =
        mutableMapOf<String, String>().apply {
            data.forEachIndexed { index, value ->
                this["$label[$index]"] = value
            }
        }

    companion object {
        private val mapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(KotlinModule(nullIsSameAsDefault = true))
        }

        private val LOGGER = LoggerFactory.getLogger(RedCapClient::class.java)
        private const val API_ROOT = "/redcap/api/"
        private const val TOKEN_LABEL = "token"
        private const val DATA_LABEL = "data"
        private const val FIELDS_LABEL = "fields"
        private const val RECORDS_LABEL = "records"
        private const val REDCAP_RESULT_INDEX = 0
    }

}