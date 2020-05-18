package org.radarbase.redcap.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.radarbase.redcap.webapp.exception.IllegalRequestException
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

    val CONFIG: Configuration by lazy {
        try {
            loadApiConfig()
        } catch (exec: IOException) {
            LOGGER.error(exec.message, exec)
            throw ExceptionInInitializerError(exec)
        }
    }

    private val mapper by lazy {
        ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule())
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }


    /**
     * Loads the API configuration file. First of all, the `CONFIG_FOLDER` env variable is
     * checked to verify if points a valid config file. If not, the default location for AWS
     * and Docker image deployment are checked. In the last instance, the config file is
     * searched inside the default projects resources folder.
     */
    @Throws(IOException::class)
    private fun loadApiConfig(): Configuration {
        val paths = arrayOf(
            System.getenv(CONFIG_FOLDER) ?: ".",
            PATH_FILE
        )
        for (path in paths) {
            val config =
                loadApiConfig(path)
            if (config != null) {
                return config
            }
        }
        val path = Properties::class.java.classLoader.getResource(
            NAME_CONFIG_FILE
        )?.file
        val folders = paths.copyOfRange(
            if (System.getenv(CONFIG_FOLDER) == null) 1 else 0,
            paths.size
        )
        if (!checkFileExist(path) && !folders.any {
                checkFileExist(
                    it
                )
            }) {
            LOGGER.error(
                "Config file {} cannot be found at {} or in the resources folder.",
                NAME_CONFIG_FILE,
                folders,
                CONFIG_FOLDER
            )
            throw FileNotFoundException("$NAME_CONFIG_FILE cannot be found.")
        }
        LOGGER.info("Loading Config file located at : {}", path)
        return loadConfig(File(path!!))
    }

    @Throws(IOException::class)
    private fun loadApiConfig(path: String): Configuration? {
        val filePath = path + NAME_CONFIG_FILE
        if (checkFileExist(filePath)) {
            LOGGER.info("Loading Config file located at : {}", path)
            return loadConfig(
                File(
                    filePath
                )
            )
        }
        return null
    }

    fun loadConfig(file: File): Configuration = file.inputStream().use {
        mapper.readValue(it, Configuration::class.java)
    }

    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if `path` points a file, false otherwise
     */
    fun checkFileExist(path: String?): Boolean {
        return if (path == null) false else File(path).exists()
    }

    /**
     * Loads all configurations and converts them to [String]. If the conversion
     * fails, it means that the config files are wrong.
     * @return a [String] representing the loaded configurations
     */
    fun validate(): String = CONFIG.toString()

    fun isSupportedInstance(url: URL, projectId: Int): Boolean {
        val identifier = RedCapInfo(url, projectId)
        return CONFIG.projects.any { it.redCapInfo == identifier }
    }

    fun getRedCapInfo(url: URL, projectId: Int): RedCapInfo {
        val identifier = RedCapInfo(url, projectId)
        return CONFIG.projects.find { it.redCapInfo == identifier }?.redCapInfo
            ?: throw IllegalRequestException(
                "No project $projectId for instance $url"
            )
    }

    fun getMpInfo(url: URL, projectId: Int): ManagementPortalInfo {
        val identifier = RedCapInfo(url, projectId)
        return CONFIG.projects.find { it.redCapInfo == identifier }?.mpInfo
            ?: throw IllegalRequestException(
                "No project $projectId for instance $url"
            )
    }

    /**
     * Get the OAuth2 client id to access ManagementPortal.
     * @return the client id
     */
    val oauthClientId: String
        get() = CONFIG.mpConfig.oauthClientId

    /**
     * Get the OAuth2 client secret to access ManagementPortal.
     * @return the client secret
     */
    val oauthClientSecret: String
        get() = CONFIG.mpConfig.oauthClientSecret

    /**
     * Generates the token end point [URL] needed to refresh tokens against Management Portal.
     * @return [URL] useful to refresh tokens
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @get:Throws(MalformedURLException::class)
    val tokenEndPoint: URL
        get() = URL(validateMpUrl(), CONFIG.mpConfig.tokenEndpoint)

    /**
     * Generates the token end point [URL] needed to manage subjects on Management Portal.
     * @return [URL] useful create and update subjects
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @get:Throws(MalformedURLException::class)
    val subjectEndPoint: URL
        get() = URL(validateMpUrl(), CONFIG.mpConfig.subjectEndpoint)

    /**
     * Generates the Project end point [URL] needed to read projects on Management Portal.
     * @param mpInfo [ManagementPortalInfo] used to extract the Management Portal project
     * identifier
     * @return [URL] useful to read project information
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @Throws(MalformedURLException::class)
    fun getProjectEndPoint(mpInfo: ManagementPortalInfo): URL = URL(
        projectEndPoint, mpInfo.projectName
    )

    /**
     * Generates the base Project end point [URL] needed to read projects on Management Portal.
     *
     * @return [URL] useful to read project information
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @get:Throws(MalformedURLException::class)
    val projectEndPoint: URL
        get() = URL(validateMpUrl(), CONFIG.mpConfig.projectEndpoint)

    /**
     * Checks if the provided [URL] is using a secure connection or not.
     * @param url [URL] to check
     * @return `true` if the protocol is `HTTPS`, `false` otherwise
     */
    private fun isSecureConnection(url: URL): Boolean = url.protocol == HTTPS

    /**
     * Returns a [URL] pointing a Management Portal instance and Checks if it is using a
     * secure connection.
     * @return [URL] pointing the Management Portal instance specified on the config file
     */
    fun validateMpUrl(): URL {
        if (!isSecureConnection(
                CONFIG.mpConfig.managementPortalUrl
            )
        ) {
            LOGGER.warn(
                "The provided Management Portal instance is not using an encrypted"
                        + " connection."
            )
        }
        return CONFIG.mpConfig.managementPortalUrl
    }

    /**
     * Checks if the provided [URL] is using a secure connection and returns it.
     * @param url [URL] to has to be checked
     * @return the provided [URL]
     */
    fun validateRedcapUrl(url: URL): URL {
        if (!isSecureConnection(url)) {
            LOGGER.warn("The provided REDCap instance is not using an encrypted connection.")
        }
        return url
    }
}