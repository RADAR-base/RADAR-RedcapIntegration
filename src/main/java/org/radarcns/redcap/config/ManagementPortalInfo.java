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

/**
 * Configuration file entry to define Management Portal settings.
 */
public class ManagementPortalInfo {

    /**
     * Management Portal project identifier. As long as the Management Portal is centralised, this
     *      value is unique.
     */
    private final String projectName;

    protected ManagementPortalInfo(
            @JsonProperty("project_name") String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the unique project identifier of the Management Portal project.
     * @return {@link Integer} representing Management Portal unique event identifier
     */
    public String getProjectName() {
        return projectName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ManagementPortalInfo)) {
            return false;
        }

        ManagementPortalInfo that = (ManagementPortalInfo) obj;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(projectName, that.projectName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(
                17, 37)
            .append(projectName)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "ManagementPortalInfo {projectName = " + projectName + "}\n";
    }
}
