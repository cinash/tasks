package com.todoroo.astrid.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.astrid.data.views.TimeLogReport;

import org.tasks.R;

public class TimeLogReportAdapter extends CursorAdapter {


    public static final int VIEW_TYPE_SUM = 0;
    public static final int VIEW_TYPE_TASK = 1;

    public TimeLogReportAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        TodorooCursor<TimeLogReport> todorooCursor = (TodorooCursor<TimeLogReport>) cursor;
        String reportType = todorooCursor.get(TimeLogReport.REPORT_TYPE);
        if (reportType.equals(TimeLogReport.REPORT_TYPE_SUM)){
            return VIEW_TYPE_SUM;
        } else {
            return VIEW_TYPE_TASK;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (viewType == VIEW_TYPE_SUM){
            LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.timelog_report_row_sum, parent, false);
            SumViewHolder viewHolder = new SumViewHolder(view);
            view.setTag(viewHolder);
            return view;
        } else {
            LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.timelog_report_row_task, parent, false);
            TaskViewHolder viewHolder = new TaskViewHolder(view);
            view.setTag(viewHolder);
            return view;
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TodorooCursor<TimeLogReport> todorooCursor = (TodorooCursor<TimeLogReport>) cursor;
        TimeLogReport timeLogReport = new TimeLogReport(todorooCursor);
        if (timeLogReport.getReportType().equals(TimeLogReport.REPORT_TYPE_SUM)){
            SumViewHolder viewHolder = (SumViewHolder) view.getTag();
            viewHolder.dateTextView.setText(DateUtils.formatDateRange(context, timeLogReport.getEntryStart().getTime(), timeLogReport.getEntryEnd().getTime(), DateUtils.FORMAT_NO_MIDNIGHT));
            viewHolder.timeTextView.setText(DateUtils.formatElapsedTime(timeLogReport.getTimeSpent()));
        } else {
            TaskViewHolder viewHolder = (TaskViewHolder) view.getTag();
            viewHolder.timeTextView.setText(DateUtils.formatElapsedTime(timeLogReport.getTimeSpent()));
            viewHolder.nameTextView.setText(timeLogReport.getName());
        }
    }

    private class TaskViewHolder{
        public TextView nameTextView;
        public TextView timeTextView;

        public TaskViewHolder(LinearLayout view) {
            timeTextView = (TextView) view.findViewById(R.id.timelog_report_row_task_time);
            nameTextView = (TextView) view.findViewById(R.id.timelog_report_row_task_name);
        }
    }

    private class SumViewHolder{
        public TextView dateTextView;
        public TextView timeTextView;
        public TextView sumLabelTextView;

        private SumViewHolder(LinearLayout view) {
            dateTextView = (TextView) view.findViewById(R.id.timelog_report_date);
            timeTextView = (TextView) view.findViewById(R.id.timelog_report_row_sum_time);
            sumLabelTextView = (TextView) view.findViewById(R.id.timelog_report_row_sum_name);
        }
    }
}
