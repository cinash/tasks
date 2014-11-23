package com.todoroo.andlib.data;

import android.content.ContentValues;

import com.todoroo.andlib.sql.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marcin on 2014-11-12.
 */
public class AbstractViewModel {

    /** Values from database */
    protected ContentValues values = null;

    private static final Logger log = LoggerFactory.getLogger(AbstractViewModel.class);

    private static final AbstractModel.ContentValuesSavingVisitor saver = new AbstractModel.ContentValuesSavingVisitor();

    /**
     * Reads all properties from the supplied cursor and store
     */
    protected void readPropertiesFromCursor(TodorooCursor<?> cursor) {
        if (values == null) {
            values = new ContentValues();
        }

        for (Property<?> property : cursor.getProperties()) {
            try {
                saver.save(property, values, cursor.get(property));
            } catch (IllegalArgumentException e) {
                // underlying cursor may have changed, suppress
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Reads the given property. Make sure this model has this property!
     */
    public synchronized <TYPE> TYPE getValue(Property<TYPE> property) {
        Object value;
        String columnName = property.getColumnName();
        if(values != null && values.containsKey(columnName)) {
            value = values.get(columnName);
        } else {
            throw new UnsupportedOperationException(
                    "Model Error: Did not read property " + property.name); //$NON-NLS-1$
        }

        // resolve properties that were retrieved with a different type than accessed
        try {
            if(value instanceof String && property instanceof Property.LongProperty) {
                return (TYPE) Long.valueOf((String) value);
            } else if(value instanceof String && property instanceof Property.IntegerProperty) {
                return (TYPE) Integer.valueOf((String) value);
            } else if(value instanceof Integer && property instanceof Property.LongProperty) {
                return (TYPE) Long.valueOf(((Number) value).longValue());
            }
            return (TYPE) value;
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
            throw new UnsupportedOperationException(
                    "Model Error: Did not read property " + property.name); //$NON-NLS-1$
        }
    }

    public static class ViewQuery<T extends AbstractViewModel> {
        private Query query;
        private Property<?>[] fields;

        public ViewQuery(Query query, Property<?>[] fields) {
            this.query = query;
            this.fields = fields;
        }

        public Query getQuery() {
            return query;
        }

        public Property<?>[] getFields() {
            return fields;
        }

    }
}
