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

/**
 * Configuration file entry to define Project settings. Each item provides an instance of
 *      {@link RedCapInfo} and an instance of {@link ManagementPortalInfo} defining respectively
 *      REDCap and Management Portal configurations.
 */
public class ProjectInfo {

    /** REDCap project information. */
    @JsonProperty("redcap_info")
    private RedCapInfo redCapInfo;

    /** Management Portal project information. */
    @JsonProperty("mp_info")
    private ManagementPortalInfo mpInfo;

    public ProjectInfo() {
        // For desrialization
    }

    protected ProjectInfo(
            RedCapInfo redCapInfo,
            ManagementPortalInfo mpInfo) {
        this.redCapInfo = redCapInfo;
        this.mpInfo = mpInfo;
    }

    public RedCapInfo getRedCapInfo() {
        return redCapInfo;
    }

    public ManagementPortalInfo getMpInfo() {
        return mpInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ProjectInfo)) {
            return false;
        }

        ProjectInfo that = (ProjectInfo) obj;

        return new EqualsBuilder()
            .append(redCapInfo, that.redCapInfo)
            .append(mpInfo, that.mpInfo)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(redCapInfo)
            .append(mpInfo)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "ProjectInfo {" + "\n"
            + "redcapInfo = " + redCapInfo.toString() + "\n"
            + "mpInfo = " + mpInfo.toString() + '}' + "\n";
    }
}
