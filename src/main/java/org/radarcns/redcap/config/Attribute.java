package org.radarcns.redcap.config;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Attribute {

    /** REDCap field name. */
    @JsonProperty("field_name")
    private String fieldName;

    public Attribute() {
        // For desrialization
    }

    protected Attribute(
            String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Attribute)) {
            return false;
        }

        Attribute that = (Attribute) obj;

        return new EqualsBuilder()
            .append(fieldName, that.fieldName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(fieldName)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "ProjectInfo {" + "\n"
            + "fieldName = " + fieldName.toString() + '}' + "\n";
    }
}
