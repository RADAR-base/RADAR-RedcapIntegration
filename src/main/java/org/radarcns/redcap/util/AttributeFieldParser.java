package org.radarcns.redcap.util;

public class AttributeFieldParser implements FieldParser<String> {

    @Override
    public String parseField(Object fieldValue) {
        return fieldValue.toString();
    }
}
