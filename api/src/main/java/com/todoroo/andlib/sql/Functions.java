/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.andlib.sql;

import android.text.TextUtils;

import com.google.common.collect.ObjectArrays;
import com.todoroo.andlib.data.Property.StringProperty;




public final class Functions {



    public static String caseStatement(Criterion when, Object ifTrue, Object ifFalse) {
        return "(CASE WHEN " + when.toString() + " THEN " + value(ifTrue) + " ELSE " + value(ifFalse) + " END)";
    }

    private static String value(Object value) {
        return value.toString();
    }

    public static Field upper(Field title) {
        return new Field("UPPER(" + title.toString() + ")");
    }

    /**
     * @return SQL now (in milliseconds)
     */
    public static Field now() {
        return new Field("(strftime('%s','now')*1000)");
    }

    public static Field cast(Field field, String newType) {
        return new Field("CAST(" + field.toString() + " AS " +
                newType + ")");
    }

    public static Field length(StringProperty field) {
        return new Field("LENGTH(" + field.toString() + ")");
    }

    public static Field date(Field dateField, String... dateModifiers){
        return date(true, dateField, dateModifiers);
    }

    public static Field date(boolean applyToLongField, Field dateField, String... dateModifiers){
        String[] standardModifiers;
        if (applyToLongField) {
            standardModifiers = new String[]{SqlConstants.DATEMODIFIER_UNIXEPOCH, SqlConstants.DATEMODIFIER_LOCALTIME};
        } else {
            standardModifiers = new String[]{};
        }
        return new Field("date(" +dateField.toString()
                + (applyToLongField ? "/1000" : "")
                + SqlConstants.COMMA
                + TextUtils.join(SqlConstants.COMMA, ObjectArrays.concat(standardModifiers,dateModifiers, String.class)) + ")");
    }

    public static Field divide(Field dividend, Field divisor){
        return new Field("((" + dividend.toString() +")/(" + divisor.toString() + "))");
    }

    public static Field subtract(Field minuend, Field subtrahend){
        return new Field("((" + minuend.toString() +")-(" + subtrahend.toString() + "))");
    }
}
