package com.todoroo.astrid.data.views;

import com.todoroo.andlib.data.Property;
import com.todoroo.astrid.data.TagData;

/**
 * Created by Marcin on 2014-11-14.
 */
public class TimeLogByListReport extends AbstractTimeLogReport {

    public static final Property.LongProperty TAG_ID = TagData.ID;

    public static final Property.StringProperty TAG_NAME = TagData.NAME;

    public long getTagId(){
        return getValue(TAG_ID);
    }

    @Override
    public String getName() {
        return getValue(TAG_NAME);
    }

    public TagData getTagData(){
        //TODO
        return null;
    }
}
