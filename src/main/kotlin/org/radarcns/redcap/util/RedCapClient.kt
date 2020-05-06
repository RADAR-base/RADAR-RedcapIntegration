package org.radarcns.redcap.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.radarcns.redcap.config.RedCapInfo
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
        get() = URL(redCapInfo.url.protocol, redCapInfo.url.host, redCapInfo.url.port, API_ROOT)

    @Throws(IOException::class)
    fun createRequest(params: Map<String, String>): Request {
        val body = FormBody.Builder().apply {
            params.forEach { entry -> add(entry.key, entry.value) }
            add(TOKEN_LABEL, redCapInfo.token!!)
        }.build()
        return Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
    }

    fun updateForm(formData: Set<RedCapInput>, recordId: Int): Boolean {
        val parameters =
            getFormUpdateParameters(formData)
        return try {
            val request = createRequest(parameters)
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                LOGGER.info("Successful update for record {}", recordId)
            }
            response.isSuccessful
        } catch (exc: IOException) {
            throw IllegalStateException("Error updating RedCap form", exc)
        }
    }

    private fun getFormUpdateParameters(formData: Set<RedCapInput>): Map<String, String> {
        val parameters: MutableMap<String, String> =
            HashMap()
        parameters[DATA_LABEL] = convertRedCapInputSetToString(formData)
        parameters["content"] = "record"
        parameters["format"] = "json"
        parameters["type"] = "eav"
        parameters["overwriteBehavior"] = "overwrite"
        parameters["returnContent"] = "count"
        parameters["returnFormat"] = "json"
        return parameters
    }

    private fun convertRedCapInputSetToString(data: Set<RedCapInput>): String {
        return try {
            ObjectMapper().writeValueAsString(data)
        } catch (exc: JsonProcessingException) {
            throw IllegalArgumentException(exc)
        }
    }

    fun fetchFormDataForId(
        fields: List<String>,
        recordId: Int
    ): Map<String, String> {
        val records = ArrayList<String>().also { it.add(recordId.toString()) }
        val parameters =
            getFormFetchParameters(fields, records)
        return try {
            val request = createRequest(parameters)
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                LOGGER.info("Successful fetch for record {}", recordId)
            }
            val result = response.body()!!.string()
            val data = JSONArray(result)[REDCAP_RESULT_INDEX].toString()
            ObjectMapper().readValue(data, object : TypeReference<HashMap<String, String>>() {})
        } catch (exc: IOException) {
            throw IllegalStateException("Error fetching RedCap form", exc)
        }
    }

    protected fun getFormFetchParameters(
        fields: List<String>,
        records: List<String>
    ): Map<String, String> {
        val parameters: MutableMap<String, String> =
            HashMap()
        parameters["content"] = "record"
        parameters["format"] = "json"
        parameters["type"] = "flat"
        parameters["rawOrLabel"] = "label"
        val fieldsEncoded = encodeListParams(fields, FIELDS_LABEL)
        val recordIdsEncoded = encodeListParams(records, RECORDS_LABEL)
        parameters.putAll(fieldsEncoded)
        parameters.putAll(recordIdsEncoded)
        return parameters
    }

    private fun encodeListParams(
        data: List<String>,
        label: String
    ): Map<String, String> {
        val encoded: MutableMap<String, String> =
            HashMap()
        var index = 0
        while (index < data.size) {
            encoded["$label[$index]"] = data[index]
            index++
        }
        return encoded
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RedCapClient::class.java)
        private const val API_ROOT = "/redcap/api/"
        private const val TOKEN_LABEL = "token"
        private const val DATA_LABEL = "data"
        private const val FIELDS_LABEL = "fields"
        private const val RECORDS_LABEL = "records"
        private const val REDCAP_RESULT_INDEX = 0
    }

}