package org.tasks.timelog;

import android.content.Context;

import com.google.common.collect.Lists;
import com.todoroo.andlib.data.AbstractViewModel;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.sql.Aggregations;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.sql.SqlConstants;
import com.todoroo.andlib.sql.SqlTable;
import com.todoroo.andlib.sql.UnionQuery;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.TaskTimeLog;
import com.todoroo.astrid.tags.TaskToTagMetadata;

import org.tasks.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.tasks.timelog.TimeLogReport.GroupByTime;
import static org.tasks.timelog.TimeLogReport.GroupByType;

public class TimeLogReportCreator {
    public static AbstractViewModel.ViewQuery<TimeLogReport> createQuery(TimeLogReport.GroupByType type, TimeLogReport.GroupByTime timeSpan, Context context) {

        Field startTime = prepareStartTimeField(timeSpan);
        Property.LongProperty objectId = prepareObjectIdField(type);
        Property.StringProperty name = prepareNameField(type, context);

        Property.StringProperty reportType = prepareReportTypeField(type);
        Field endTime = prepareEndTimeField(timeSpan, startTime);
        Property.LongFunctionProperty sumProperty = prepareSumField();
        Property.LongProperty idProperty = new Property.LongFunctionProperty(Aggregations.min(TaskTimeLog.ID).toString(), "_id");

        //NOTE: we have to create that property after creation of endTime, because startTime has to be used there as full query not as alias
        startTime = new Property.StringFunctionProperty(startTime.toString(), TimeLogReport.REPORT_ENTRY_START.getColumnName());

        List<Join> joins = prepareJoins(type);

        List<Field> groupBy = Lists.newArrayList(startTime, objectId, name);

        List<Field> select = Lists.newArrayList(groupBy);
        select.addAll(Arrays.asList(reportType, endTime, sumProperty, idProperty));

        Query dataQuery = Query.select(select.toArray(new Field[select.size()]))
                .from(TaskTimeLog.TABLE)
                .join(joins.toArray(new Join[joins.size()]))
                .groupBy(groupBy.toArray(new Field[groupBy.size()]));

        Query timeSpanHeaderQuery = prepareTimeSpanHeaderQuery(startTime, endTime);


        UnionQuery union = UnionQuery.unionQuery(dataQuery, timeSpanHeaderQuery);
        Query combinedQuery = Query.select().from(SqlTable.table(union)).orderBy(Order.desc(startTime), Order.asc(TimeLogReport.REPORT_TYPE), Order.desc(sumProperty));

        return new AbstractViewModel.ViewQuery<>(combinedQuery, dataQuery.getFields());
    }

    private static Query prepareTimeSpanHeaderQuery(Field startTime, Field endTime) {
        Property.LongProperty idForSumProperty = new Property.LongFunctionProperty(Aggregations.min(TaskTimeLog.ID).toString() + " + 1000000000000000", "_id");
        return Query.select(startTime,
                new Property.LongFunctionProperty("-1", TimeLogReport.OBJECT_ID.name),
                new Property.StringFunctionProperty("''", TimeLogReport.NAME.name),
                new Property.StringFunctionProperty("'" + TimeLogReport.REPORT_TYPE_SUM + "'", TimeLogReport.REPORT_TYPE.name),
                endTime, prepareSumField(),idForSumProperty)
                .from(TaskTimeLog.TABLE)
                .groupBy(startTime);
    }

    private static Property.LongFunctionProperty prepareSumField() {
        return new Property.LongFunctionProperty(Aggregations.sum(TaskTimeLog.TIME_SPENT).toString(), TimeLogReport.TIME_SUM.getColumnName());
    }

    private static List<Join> prepareJoins(GroupByType type) {
        List<Join> joins = new ArrayList<>();
        Join taskJoin = Join.inner(Task.TABLE, TaskTimeLog.TASK_ID.eq(Task.ID));
        joins.add(taskJoin);
        if (type == GroupByType.LIST) {
            joins.add(Join.left(Metadata.TABLE, TaskToTagMetadata.TASK_UUID.eq(Task.UUID)));
            joins.add(Join.left(TagData.TABLE, TaskToTagMetadata.TAG_UUID.eq(TagData.UUID)));
        }
        return joins;
    }

    private static Property.StringProperty prepareReportTypeField(GroupByType type) {
        String reportTypeString = null;
        switch (type) {
            case TASK:
                reportTypeString = TimeLogReport.REPORT_TYPE_TASK;
                break;
            case LIST:
                reportTypeString = TimeLogReport.REPORT_TYPE_LIST;
                break;
        }
        return new Property.StringFunctionProperty("'" + reportTypeString + "'", TimeLogReport.REPORT_TYPE.name);
    }

    private static Property.StringProperty prepareNameField(GroupByType type, Context context) {
        Property.StringProperty name = null;
        switch (type) {
            case TASK:
                name = Task.TITLE;
                break;
            case LIST:
                name = new Property.StringFunctionProperty(Functions.ifnull(TagData.NAME, new Property.StringFunctionProperty("'" + context.getString(R.string.timeReport_noList) + "'", null)).toString(), null);
                break;
        }
        name = name.as(TimeLogReport.NAME.name);
        return name;
    }

    private static Property.LongProperty prepareObjectIdField(GroupByType type) {
        Property.LongProperty objectId = null;
        switch (type) {
            case TASK:
                objectId = Task.ID;
                break;
            case LIST:
                objectId = new Property.LongFunctionProperty(Functions.ifnull(TagData.ID, new Property.LongFunctionProperty("-1", null)).toString(), null);
                break;
        }
        objectId = objectId.as(TimeLogReport.OBJECT_ID.name);
        return objectId;
    }

    private static Field prepareEndTimeField(GroupByTime timeSpan, Field startTime) {
        Field endTime = null;
        switch (timeSpan) {
            case DAY:
                endTime = Functions.date(false, startTime, "'+1 day'");
                break;
            case WEEK:
                endTime = Functions.date(false, startTime, "'+7 days'");
                break;
            case MONTH:
                endTime = Functions.date(false, startTime, "'+31 days'", SqlConstants.DATEMODIFIER_START_OF_MONTH);
                break;
        }
        endTime = new Property.StringFunctionProperty(endTime.toString(), TimeLogReport.REPORT_ENTRY_END.getColumnName());
        return endTime;
    }

    private static Field prepareStartTimeField(GroupByTime timeSpan) {
        Field startTime = null;
        switch (timeSpan) {
            case DAY:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_DAY);
                break;
            case WEEK:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_NEXT_WEEK, "'-7 days'");
                break;
            case MONTH:
                startTime = Functions.date(TaskTimeLog.TIME, SqlConstants.DATEMODIFIER_START_OF_MONTH);
                break;
        }
        return startTime;
    }

}
