package org.radarcns.redcap.util

class AttributeFieldParser : FieldParser<String, String> {
    override fun parseField(fieldValue: String): String {
        return fieldValue
    }

    override fun canBeParsed(fieldValue: String): Boolean = fieldValue.isNotBlank()
}