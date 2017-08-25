package org.radarcns.redcap.util;

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
 * Information about the REDCap user used to query the REDCap API.
 */
public final class RedCapUser {

    private static String token = "728A6BED02E68ACB1C9E057812747A22";

    /**
     * Returns API Token used to identify the REDCap user against the REDCap instance.
     * @return {@link String} representing REDCap API Token
     */
    public static String getToken() {
        return token;
    }

}
