package org.radarcns.redcap.util;

public interface FieldParser<T, R> {

    T parseField(R fieldValue);
}
