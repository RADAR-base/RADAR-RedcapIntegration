package org.radarbase.redcap.util

interface FieldParser<out T, in R> {
    fun parseField(fieldValue: R): T

    fun canBeParsed(fieldValue: R): Boolean
}