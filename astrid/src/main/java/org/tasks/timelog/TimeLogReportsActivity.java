package org.tasks.timelog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tasks.R;
import org.tasks.injection.InjectingActionBarActivity;
import org.tasks.preferences.ActivityPreferences;

import javax.inject.Inject;


public class TimeLogReportsActivity extends InjectingActionBarActivity {

    private static final Logger log = LoggerFactory.getLogger(TimeLogReportsActivity.class);
    public static final String BASIC_TIME_REPORT = "BASIC_TIME_REPORT";

    @Inject
    ActivityPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timelog_report_activity);
        preferences.applyTheme();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setupFragment(BASIC_TIME_REPORT, R.id.timelog_report_fragment_container, TimeLogReportsFragment.class);
    }

    protected Fragment setupFragment(String tag, int container, Class<? extends Fragment> cls) {
        final FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if(fragment == null) {
            try {
                fragment = cls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
                return null;
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (container == 0) {
                ft.add(fragment, tag);
            }
            else {
                ft.replace(container, fragment, tag);
            }
            ft.commit();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fm.executePendingTransactions();
                }
            });
        }
        return fragment;
    }
}
