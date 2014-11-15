package com.todoroo.astrid.data.views;

import com.todoroo.andlib.data.AbstractViewModel;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marcin on 2014-11-12.
 */
public abstract class AbstractTimeLogReport extends AbstractViewModel {

    public enum GroupByTime {
        DAY,
        WEEK,
        MONTH
    }

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

    public Date getEntryEnd(){
        return parseDate(getValue(REPORT_ENTRY_END));
    }

    public abstract String getName();

}
