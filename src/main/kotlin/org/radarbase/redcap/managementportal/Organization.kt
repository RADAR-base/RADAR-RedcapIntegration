package org.radarbase.redcap.managementportal

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

sealed class Organization {
    data class StringOrganization(val value: String) : Organization()
    data class ObjectOrganization(val value: OrganizationObject) : Organization()
}

data class OrganizationObject(
    val id: Int,
    val name: String,
    val description: String
) {}

// Custom deserializer
class OrganizationDeserializer : JsonDeserializer<Organization>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Organization {
        val node = p.codec.readTree<JsonNode>(p)
        return when {
            node.isValueNode -> Organization.StringOrganization(node.asText())
            else -> {
                val org = p.codec.treeToValue(node, OrganizationObject::class.java)
                Organization.ObjectOrganization(org)
            }
        }
    }
}