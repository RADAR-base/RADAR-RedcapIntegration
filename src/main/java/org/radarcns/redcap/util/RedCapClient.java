package org.radarcns.redcap.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import okhttp3.*;
import org.json.JSONArray;
import org.radarcns.redcap.config.RedCapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@SuppressWarnings("unchecked")
public class RedCapClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedCapClient.class);
    private static final String API_ROOT = "/redcap/api/";
    private static final String TOKEN_LABEL = "token";
    private static final String DATA_LABEL = "data";
    private static final String FIELDS_LABEL = "fields";
    private static final String RECORDS_LABEL = "records";
    private static final int REDCAP_RESULT_INDEX = 0;


    private final RedCapInfo redCapInfo;

    private final OkHttpClient httpClient = new OkHttpClient();

    public RedCapClient(RedCapInfo redCapInfo) {
        this.redCapInfo = redCapInfo;
    }

    /**
     * Returns the REDCap API URL starting from the information contained in the
     *      {@link RedCapTrigger}.
     * @return {@link URL} to request REDCap API
     * @throws MalformedURLException in case the URL is malformed
     */
    public URL getApiUrl() throws MalformedURLException {
        return new URL(redCapInfo.getUrl().getProtocol(), redCapInfo.getUrl().getHost(),
                redCapInfo.getUrl().getPort(), API_ROOT);
    }

    public Request createRequest(Map<String, String> params) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String,String> entry : params.entrySet())
            builder.add(entry.getKey(), entry.getValue());

        builder.add(TOKEN_LABEL, redCapInfo.getToken());
        RequestBody body = builder.build();

        return new Request.Builder()
                .url(getApiUrl())
                .post(body)
                .build();
    }

    public boolean updateForm(Set<RedCapInput> formData, Integer recordId) {
        Map<String, String> parameters = getFormUpdateParameters(formData);
        try {
            Request request = createRequest(parameters);
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                LOGGER.info("Successful update for record {}", recordId);
            }
            return response.isSuccessful();
        }
        catch(IOException exc){
            throw new IllegalStateException("Error updating RedCap form", exc);
        }
    }

    protected Map<String, String> getFormUpdateParameters(Set<RedCapInput> formData) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(DATA_LABEL, convertRedCapInputSetToString(formData));
        parameters.put("content", "record");
        parameters.put("format", "json");
        parameters.put("type", "eav");
        parameters.put("overwriteBehavior", "overwrite");
        parameters.put("returnContent", "count");
        parameters.put("returnFormat", "json");
        return parameters;
    }

    private String convertRedCapInputSetToString(Set<RedCapInput> data){
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException exc) {
            throw new IllegalArgumentException(exc);
        }
    }

    public Map<String, String> fetchFormDataForId(List<String> fields, Integer recordId) {
        ArrayList<String> records = new ArrayList<String>();
        records.add(String.valueOf(recordId));
        Map<String, String> parameters = getFormFetchParameters(fields, records);
        try {
            Request request = createRequest(parameters);
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                LOGGER.info("Successful fetch for record {}", recordId);
            }
            String result = response.body().string();
            String data = new JSONArray(result).get(REDCAP_RESULT_INDEX).toString();
            return new ObjectMapper().readValue(data, HashMap.class);
        }
        catch(IOException exc){
            throw new IllegalStateException("Error fetching RedCap form", exc);
        }
    }


    protected Map<String, String> getFormFetchParameters(List<String> fields, List<String> records) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("content", "record");
        parameters.put("format", "json");
        parameters.put("type", "flat");
        parameters.put("rawOrLabel", "label");

        Map<String, String> fieldsEncoded = encodeListParams(fields, FIELDS_LABEL);
        Map<String, String> recordIdsEncoded = encodeListParams(records, RECORDS_LABEL);
        parameters.putAll(fieldsEncoded);
        parameters.putAll(recordIdsEncoded);

        return parameters;
    }

    private Map<String, String> encodeListParams(List<String> data, String label){
        Map<String, String> encoded = new HashMap<>();
        int index = 0;
        while (index < data.size()){
            encoded.put(label + "[" + index + "]", data.get(index));
            index ++;
        }
        return encoded;
    }
}
