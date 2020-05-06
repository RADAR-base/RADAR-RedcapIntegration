package org.radarcns.redcap.config

import org.radarcns.config.YamlConfigLoader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
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
 * Singleton class to manage configuration files.
 */
object Properties {
    /** Logger.  */
    private val LOGGER = LoggerFactory.getLogger(Properties::class.java)
    private const val HTTPS = "https"
    /** Path to the configuration file.  */
    private const val PATH_FILE = "/usr/local/etc/radar-redcap-int/"
    /** Placeholder alternative path for the config folder.  */
    private const val CONFIG_FOLDER = "REDCAP_INTEGRATION_CONFIG_FOLDER"
    /** API Config file name.  */
    private const val NAME_CONFIG_FILE = "radar.yml"
    /** Path where the config file is located.  */ //private static String validPath;
    private var CONFIG: Configuration? = null

    /**
     * Loads the API configuration file. First of all, the `CONFIG_FOLDER` env variable is
     * checked to verify if points a valid config file. If not, the default location for AWS
     * and Docker image deployment are checked. In the last instance, the config file is
     * searched inside the default projects resources folder.
     */
    @Throws(IOException::class)
    private fun loadApiConfig(): Configuration {
        val paths = arrayOf(
            System.getenv(CONFIG_FOLDER),
            PATH_FILE
        )
        var config: Configuration?
        for (i in paths.indices) {
            config = loadApiConfig(paths[i])
            if (config != null) {
                return config
            }
        }
        val path = Properties::class.java.classLoader.getResource(NAME_CONFIG_FILE)?.file
        //validPath = new File(path).getParent() + "/";
        if (path == null) {
            val folders = paths.copyOfRange(
                if (System.getenv(CONFIG_FOLDER) == null) 1 else 0,
                paths.size
            )
            LOGGER.error(
                "Config file {} cannot be found at {} or in the resources"
                        + "folder.",
                NAME_CONFIG_FILE,
                folders,
                CONFIG_FOLDER
            )
            throw FileNotFoundException("$NAME_CONFIG_FILE cannot be found.")
        }
        LOGGER.info("Loading Config file located at : {}", path)
        return YamlConfigLoader().load(File(path), Configuration::class.java)
    }

    @Throws(IOException::class)
    private fun loadApiConfig(path: String): Configuration? { //validPath = path;
        val filePath = path + NAME_CONFIG_FILE
        if (checkFileExist(filePath)) {
            LOGGER.info(
                "Loading Config file located at : {}",
                path
            )
            return YamlConfigLoader().load(
                File(
                    filePath
                ), Configuration::class.java
            )
        }
        //validPath = null;
        return null
    }

    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if `path` points a file, false otherwise
     */
    private fun checkFileExist(path: String?): Boolean {
        return if (path == null) false else File(path).exists()
    }

    /**
     * Loads all configurations and converts them to [String]. If the conversion
     * fails, it means that the config files are wrong.
     * @return a [String] representing the loaded configurations
     */
    fun validate(): String {
        return CONFIG.toString()
    }

    @JvmStatic
    fun isSupportedInstance(url: URL, projectId: Int): Boolean {
        val identifier = RedCapInfo(url, projectId)
        for (info in CONFIG!!.projects) {
            if (info.redCapInfo.equals(identifier)) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun getRedCapInfo(url: URL, projectId: Int): RedCapInfo {
        val identifier = RedCapInfo(url, projectId)
        for (info in CONFIG!!.projects) {
            if (info.redCapInfo.equals(identifier)) {
                return info.redCapInfo
            }
        }
        throw IllegalArgumentException(
            "No project " + projectId + " for instance "
                    + url.toString()
        )
    }

    @JvmStatic
    fun getMpInfo(url: URL, projectId: Int): ManagementPortalInfo {
        val identifier = RedCapInfo(url, projectId)
        for (info in CONFIG!!.projects) {
            if (info.redCapInfo.equals(identifier)) {
                return info.mpInfo
            }
        }
        throw IllegalArgumentException(
            "No project " + projectId + " for instance "
                    + url.toString()
        )
    }

    /**
     * Get the OAuth2 client id to access ManagementPortal.
     * @return the client id
     */
    @JvmStatic
    val oauthClientId: String
        get() = CONFIG!!.oauthClientId

    /**
     * Get the OAuth2 client secret to access ManagementPortal.
     * @return the client secret
     */
    @JvmStatic
    val oauthClientSecret: String
        get() = CONFIG!!.oauthClientSecret

    /**
     * Generates the token end point [URL] needed to refresh tokens against Management Portal.
     * @return [URL] useful to refresh tokens
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @JvmStatic
    @get:Throws(MalformedURLException::class)
    val tokenEndPoint: URL
        get() = URL(
            validateMpUrl(),
            CONFIG!!.tokenEndpoint
        )

    /**
     * Generates the token end point [URL] needed to manage subjects on Management Portal.
     * @return [URL] useful create and update subjects
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @get:Throws(MalformedURLException::class)
    val subjectEndPoint: URL
        get() = URL(
            validateMpUrl(),
            CONFIG!!.subjectEndpoint
        )

    /**
     * Generates the Project end point [URL] needed to read projects on Management Portal.
     * @param mpInfo [ManagementPortalInfo] used to extract the Management Portal project
     * identifier
     * @return [URL] useful to read project information
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @Throws(MalformedURLException::class)
    fun getProjectEndPoint(mpInfo: ManagementPortalInfo): URL {
        return URL(
            validateMpUrl(),
            CONFIG!!.projectEndpoint +
                    mpInfo.projectName
        )
    }

    /**
     * Generates the base Project end point [URL] needed to read projects on Management Portal.
     *
     * @return [URL] useful to read project information
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @JvmStatic
    @get:Throws(MalformedURLException::class)
    val projectEndPoint: URL
        get() = URL(
            validateMpUrl(),
            CONFIG!!.projectEndpoint
        )

    /**
     * Checks if the provided [URL] is using a secure connection or not.
     * @param url [URL] to check
     * @return `true` if the protocol is `HTTPS`, `false` otherwise
     */
    private fun isSecureConnection(url: URL): Boolean {
        return url.protocol == HTTPS
    }

    /**
     * Returns a [URL] pointing a Management Portal instance and Checks if it is using a
     * secure connection.
     * @return [URL] pointing the Management Portal instance specified on the config file
     */
    fun validateMpUrl(): URL {
        if (!isSecureConnection(CONFIG!!.managementPortalUrl)) {
            LOGGER.warn(
                "The provided Management Portal instance is not using an encrypted"
                        + " connection."
            )
        }
        return CONFIG!!.managementPortalUrl
    }

    /**
     * Checks if the provided [URL] is using a secure connection and returns it.
     * @param url [URL] to has to be checked
     * @return the provided [URL]
     */
    @JvmStatic
    fun validateRedcapUrl(url: URL): URL {
        if (!isSecureConnection(url)) {
            LOGGER.warn("The provided REDCap instance is not using an encrypted connection.")
        }
        return url
    }

    init {
        CONFIG = try {
            loadApiConfig()
        } catch (exec: IOException) {
            LOGGER.error(exec.message, exec)
            throw ExceptionInInitializerError(exec)
        }
    }
}