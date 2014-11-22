package com.todoroo.astrid.data.views;

import com.todoroo.andlib.data.AbstractViewModel;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;
import com.todoroo.andlib.data.TodorooCursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLogReport extends AbstractViewModel {

    public enum GroupByTime {
        DAY,
        WEEK,
        MONTH
    }

    public enum GroupByType {
        TASK,
        LIST
    }

    //it is important that REPORT_TYPE_SUM is first when sorting lexicographically
    public static final String REPORT_TYPE_TASK = "TASK";
    public static final String REPORT_TYPE_LIST = "LIST";
    public static final String REPORT_TYPE_SUM = "AAA_SUM";


    public static final Property.StringProperty REPORT_TYPE = new Property.StringProperty((Table) null, "report_type");

    public static final Property.LongProperty OBJECT_ID = new Property.LongProperty((Table) null, "report_object_id");

    public static final Property.StringProperty NAME = new Property.StringProperty((Table) null, "report_name");

    public static final Property.LongProperty TIME_SUM = new Property.LongProperty((Table)null, "report_entry_time");

    public static final Property.StringProperty REPORT_ENTRY_START = new Property.StringProperty(null, "report_entry_start", Property.PROP_FLAG_DATE);
    public static final Property.StringProperty REPORT_ENTRY_END = new Property.StringProperty(null, "report_entry_end", Property.PROP_FLAG_DATE);

    public long getTimeSpent(){
        return getValue(TIME_SUM);
    }

    public Date getEntryStart(){
        return parseDate(getValue(REPORT_ENTRY_START));
    }

    private Date parseDate(String dateString){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public TimeLogReport(TodorooCursor<TimeLogReport> cursor) {
        readPropertiesFromCursor(cursor);
    }

    public Date getEntryEnd(){
        return parseDate(getValue(REPORT_ENTRY_END));
    }

    public String getName() {
        return getValue(NAME);
    }

    public Long getObjectId(){
        return getValue(OBJECT_ID);
    }

    public String getReportType(){
        return getValue(REPORT_TYPE);
    }

    @Override
    public String toString() {
        return "TimeLogReport{" +
                "entryEnd=" + getEntryEnd() +
                ", entryStart=" + getEntryStart() +
                ", name='" + getName() + '\'' +
                ", reportType='" + getReportType() + '\'' +
                ", timeSpent=" + getTimeSpent() +
                ", objectId=" + getObjectId() +
                '}';
    }
}
