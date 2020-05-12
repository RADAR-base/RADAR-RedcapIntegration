package org.radarcns.redcap.util;

public class AttributeFieldParser implements FieldParser<String, String> {

    @Override
    public String parseField(String fieldValue) {
        return fieldValue;
    }
}
