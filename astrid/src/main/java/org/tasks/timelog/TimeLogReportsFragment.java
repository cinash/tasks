package org.tasks.timelog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.todoroo.astrid.adapter.TimeLogReportAdapter;
import com.todoroo.astrid.dao.TaskTimeLogDao;

import org.tasks.R;
import org.tasks.injection.InjectingListFragment;
import org.tasks.preferences.ActivityPreferences;

import javax.inject.Inject;

public class TimeLogReportsFragment extends InjectingListFragment {

    private static final TimeLogReport.GroupByTime[] GROUP_BY_TIMES = new TimeLogReport.GroupByTime[]{
            TimeLogReport.GroupByTime.DAY, TimeLogReport.GroupByTime.WEEK, TimeLogReport.GroupByTime.MONTH
    };

    private static final TimeLogReport.GroupByType[] GROUP_BY_TYPES = new TimeLogReport.GroupByType[]{
            TimeLogReport.GroupByType.TASK, TimeLogReport.GroupByType.LIST
    };

    @Inject
    TaskTimeLogDao taskTimeLogDao;
    @Inject
    ActivityPreferences preferences;

    private TimeLogReport.GroupByType groupByType = TimeLogReport.GroupByType.TASK;
    private TimeLogReport.GroupByTime groupByTime = TimeLogReport.GroupByTime.DAY;

    private Spinner groupByTypeSpinner;
    private Spinner groupByTimeSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        preferences.applyTheme();
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.timelog_report_fragment, container, false);
        groupByTimeSpinner = (Spinner) view.findViewById(R.id.groupByDateSpinner);
        groupByTypeSpinner = (Spinner) view.findViewById(R.id.groupByTypeSpinner);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //TODO
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateListAdapter();

        groupByTimeSpinner.setOnItemSelectedListener(new OnTimeSelectedListener());

        groupByTypeSpinner.setOnItemSelectedListener(new OnTypeSelectedListener());
    }

    public void updateListAdapter() {
        TimeLogReportAdapter timeLogReportAdapter = new TimeLogReportAdapter(getActivity(), taskTimeLogDao.getReport(groupByType, groupByTime, getActivity()), false);
        setListAdapter(timeLogReportAdapter);
    }

    private class OnTimeSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (GROUP_BY_TIMES[position] != groupByTime){
                groupByTime = GROUP_BY_TIMES[position];
                updateListAdapter();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //do nothing, as it is not possible
        }
    }

    private class OnTypeSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (GROUP_BY_TYPES[position] != groupByType){
                groupByType = GROUP_BY_TYPES[position];
                updateListAdapter();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //do nothing, as it is not possible
        }
    }
}