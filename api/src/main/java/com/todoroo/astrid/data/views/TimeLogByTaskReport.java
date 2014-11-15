package com.todoroo.astrid.data.views;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.astrid.data.Task;

public class TimeLogByTaskReport extends AbstractTimeLogReport {

    public static final Property.LongProperty TASK_ID = Task.ID;

    public static final Property.StringProperty TASK_TITLE = Task.TITLE;

    public long getTaskId(){
        return getValue(TASK_ID);
    }

    public TimeLogByTaskReport(TodorooCursor<TimeLogByTaskReport> cursor) {
        readPropertiesFromCursor(cursor);
    }

    @Override
    public String getName() {
        return getValue(TASK_TITLE);
    }

    public Task getTask(){
        //TODO
        return null;
    }
}
