package org.radarbase.redcap.managementportal

import java.util.*


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

data class Source(
    var id: Long? = null,
    var sourceTypeId: Long? = null,
    var sourceTypeProducer: String? = null,
    var sourceTypeModel: String? = null,
    var sourceTypeCatalogVersion: String? = null,
    var expectedSourceName: String? = null,
    var sourceId: UUID? = null,
    var sourceName: String? = null,
    var isAssigned: Boolean? = null,
    var attributes: MutableMap<String, String> = HashMap()
)