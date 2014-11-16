package com.todoroo.astrid.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.todoroo.andlib.data.AbstractModel;
import com.todoroo.andlib.data.Callback;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.sql.Aggregations;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.sql.SqlConstants;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.TaskTimeLog;
import com.todoroo.astrid.data.views.TimeLogReport;
import com.todoroo.astrid.tags.TaskToTagMetadata;

import org.tasks.helper.UUIDHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskTimeLogDao extends RemoteModelDao<TaskTimeLog> {

    private final Database database;

    @Inject
    public TaskTimeLogDao(Database database) {
        super(TaskTimeLog.class);
        setDatabase(database);
        this.database = database;
    }

    public static void migrateLoggedTime(SQLiteDatabase database){

        Property[] properties = {Task.ID, Task.ELAPSED_SECONDS, Task.COMPLETION_DATE, Task.CREATION_DATE, Task.UUID};
        List<String> strings = new ArrayList<String>();
        for (Property property : properties) {
            strings.add(property.name);
        }
        Cursor cursor = database.query(Task.TABLE.name, strings.toArray(new String[0]), null, null, null, null, null);
        TodorooCursor<Task> todorooCursor = new TodorooCursor<>(cursor, properties);

        try {
            while (todorooCursor.moveToNext()){
                Task task = new Task(todorooCursor);
                TaskTimeLog taskTimeLog = createTimeLogFromTask(task);
                if (taskTimeLog != null) {
                    database.insert(TaskTimeLog.TABLE.name, AbstractModel.ID_PROPERTY.name, taskTimeLog.getMergedValues());
                }
}
        } finally {
            cursor.close();
        }
    }

    public static TaskTimeLog createTimeLogFromTask(Task task) {
        Integer elapsedSeconds = task.getElapsedSeconds();
        if (elapsedSeconds == null || elapsedSeconds == 0){
            return null;
        }
        TaskTimeLog taskTimeLog = new TaskTimeLog();
        taskTimeLog.setTaskId(task.getId());
        taskTimeLog.setTaskUuid(task.getUuid());
        taskTimeLog.setTime(isNonZero(task.getCompletionDate()) ? task.getCompletionDate() : task.getCreationDate());
        taskTimeLog.setTimeSpent(elapsedSeconds);
        taskTimeLog.setUuid(UUIDHelper.newUUID());
        taskTimeLog.setID(TaskTimeLog.NO_ID);

        Integer estimatedSeconds = task.getEstimatedSeconds();
        if (isNonZero(estimatedSeconds)){
            int remainingSeconds = Math.max(0, estimatedSeconds - elapsedSeconds);
            task.setRemainingSeconds(remainingSeconds);
        }

        return taskTimeLog;
    }

    public static boolean isNonZero(Number completionDate) {
        return (completionDate != null && completionDate.intValue() != 0);
    }

    /**
     * Generates SQL clauses
     */
    public static class TaskTimeLogCriteria {

        /** @return Time Logs by id nadmiarowe! */
        public static Criterion byId(long id) {
            return TaskTimeLog.ID.eq(id);
        }

        /** @return Time Logs for selected task by TaskId */
        public static Criterion byTaskId(long TaskId) {
            return TaskTimeLog.TASK_ID.eq(TaskId);
        }

        /** @return Time Logs between Time1 and Time2 */
        public static Criterion betweenTimes(Date Time1, Date Time2) {
            return Criterion.and(TaskTimeLog.TIME.lte(Time2),
                    TaskTimeLog.TIME.gt(Time1));
        }

    }
    // --- delete

    /**
     * Delete the given item
     *
     * @return true if delete was successful
     */
    @Override
    public boolean delete(long id) {
        boolean result = super.delete(id);
        if(!result) {
            return false;
        }
        return true;
    }
    public int deleteWhere(long TaskId) {
        int result = deleteWhere(TaskTimeLogCriteria.byTaskId(TaskId));
        return result;
    }

    public void byTask(long taskId, Callback<TaskTimeLog> callback) {
        query(callback, Query.select(TaskTimeLog.PROPERTIES).where(TaskTimeLogCriteria.byTaskId(taskId)));
    }

    public <T extends TimeLogReport> TodorooCursor<T> getReport(TimeLogReport.GroupByType type, TimeLogReport.GroupByTime timeSpan){
        List<Field> select = new ArrayList<>();
        Property.LongFunctionProperty sumProperty = new Property.LongFunctionProperty(Aggregations.sum(TaskTimeLog.TIME_SPENT).toString(), TimeLogReport.TIME_SUM.getColumnName());
        List<Field> groupBy = new ArrayList<>();
        List<Join> joins = new ArrayList<>();
        Field startTime=null, endTime=null;
        Property.LongProperty objectId = null;
        Property.StringProperty name = null;
        String reportTypeString = null;
        Join taskJoin = Join.inner(Task.TABLE, TaskTimeLog.TASK_ID.eq(Task.ID));
        switch (type){
            case TASK:
                objectId = Task.ID;
                name = Task.TITLE;
                reportTypeString = TimeLogReport.REPORT_TYPE_TASK;
                joins.add(taskJoin);
                break;
            case LIST:
                objectId = TagData.ID;
                name = TagData.NAME;
                reportTypeString = TimeLogReport.REPORT_TYPE_LIST;
                joins.add(taskJoin);
                joins.add(Join.left(Metadata.TABLE, TaskToTagMetadata.TASK_UUID.eq(Task.UUID)));
                joins.add(Join.left(TagData.TABLE, TaskToTagMetadata.TAG_UUID.eq(TagData.UUID)));
                break;
        }

        objectId = objectId.as(TimeLogReport.OBJECT_ID.name);
        name = name.as(TimeLogReport.NAME.name);
        Property.StringProperty reportType = new Property.StringFunctionProperty("'" + reportTypeString + "'", TimeLogReport.REPORT_TYPE.name);

        switch (timeSpan){
            case DAY:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_DAY);
                endTime = Functions.date(false, startTime, "'+1 day'");
                break;
            case WEEK:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_NEXT_WEEK, "'-7 days'");
                endTime = Functions.date(false, startTime, "'+7 days'");
                break;
            case MONTH:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_MONTH);
                endTime = Functions.date(false, startTime, "'+31 days'", SqlConstants.DATEMODIFIER_START_OF_MONTH);
                break;
        }
        startTime = new Property.StringFunctionProperty(startTime.toString(), TimeLogReport.REPORT_ENTRY_START.getColumnName());
        endTime = new Property.StringFunctionProperty(endTime.toString(), TimeLogReport.REPORT_ENTRY_END.getColumnName());
        groupBy.add(startTime);
        groupBy.add(objectId);
        groupBy.add(name);

        select.add(reportType);
        select.add(endTime);
        select.add(sumProperty);
        select.addAll(groupBy);

        Query dataQuery = Query.select(select.toArray(new Field[select.size()]))
                .from(TaskTimeLog.TABLE)
                .join(joins.toArray(new Join[joins.size()]))
                .groupBy(groupBy.toArray(new Field[groupBy.size()]));

        Query sumQuery = Query.select(new Property.StringFunctionProperty("'" + TimeLogReport.REPORT_TYPE_SUM + "'", TimeLogReport.REPORT_TYPE.name),
                    endTime, sumProperty, startTime,
                    new Property.LongFunctionProperty("-1", TimeLogReport.OBJECT_ID.name),
                    new Property.StringFunctionProperty("''", TimeLogReport.NAME.name))
                .from(TaskTimeLog.TABLE)
                .groupBy(startTime);


        String combinedQuery = "select * " +
                "from (" + dataQuery.toString() + " " +
                    "union " + sumQuery.toString() + ")" +
                "order by " + startTime.toString() + " desc, " +
                TimeLogReport.REPORT_TYPE.name + ", " +
                TimeLogReport.NAME.name;

        Cursor cursor = database.rawQuery(combinedQuery);

        return new TodorooCursor<>(cursor, dataQuery.getFields());
    }
}
