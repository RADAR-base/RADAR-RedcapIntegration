package org.radarcns.redcap.util;

public interface FieldParser<T> {

    T parseField(Object fieldValue);
}
