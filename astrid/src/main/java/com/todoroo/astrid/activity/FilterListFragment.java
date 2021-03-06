/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.todoroo.astrid.adapter.FilterAdapter;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.FilterListItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tasks.R;
import org.tasks.filters.FilterCounter;
import org.tasks.injection.InjectingListFragment;
import org.tasks.injection.Injector;

import javax.inject.Inject;

/**
 * Activity that displays a user's task lists and allows users
 * to filter their task list.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class FilterListFragment extends InjectingListFragment {

    private static final Logger log = LoggerFactory.getLogger(FilterListFragment.class);

    public static final String TAG_FILTERLIST_FRAGMENT = "filterlist_fragment"; //$NON-NLS-1$

    public static final String TOKEN_LAST_SELECTED = "lastSelected"; //$NON-NLS-1$

    // -- extra codes
    //public static final String SHOW_BACK_BUTTON = "show_back"; //$NON-NLS-1$

    // --- menu codes

    private static final int CONTEXT_MENU_SHORTCUT = R.string.FLA_context_shortcut;
    private static final int CONTEXT_MENU_INTENT = Menu.FIRST + 4;

    public static final int REQUEST_CUSTOM_INTENT = 10;
    public static final int REQUEST_NEW_LIST = 4;

    // --- instance variables

    protected FilterAdapter adapter = null;

    private final RefreshReceiver refreshReceiver = new RefreshReceiver();

    private OnFilterItemClickedListener mListener;

    @Inject FilterCounter filterCounter;
    @Inject Injector injector;

    /* ======================================================================
     * ======================================================= initialization
     * ====================================================================== */

    /** Container Activity must implement this interface and we ensure
     * that it does during the onAttach() callback
     */
    public interface OnFilterItemClickedListener {
        public boolean onFilterItemClicked(FilterListItem item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Check that the container activity has implemented the callback interface
        try {
            mListener = (OnFilterItemClickedListener) activity;
            adapter = new FilterAdapter(injector, filterCounter, getActivity(), null, R.layout.filter_adapter_row, false, false);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFilterItemClickedListener"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see com.todoroo.astrid.fragment.ExpandableListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Activity activity = getActivity();
        int layout = R.layout.filter_list_activity;
        return activity.getLayoutInflater().inflate(layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        getActivity().setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);

        setUpList();
    }

    /* ======================================================================
     * ============================================================ lifecycle
     * ====================================================================== */

    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null) {
            adapter.registerRecevier();
        }

        // also load sync actions
        Activity activity = getActivity();

        if (activity instanceof TaskListActivity) {
            ((TaskListActivity) activity).setupPopoverWithFilterList(this);
        }

        activity.registerReceiver(refreshReceiver,
                new IntentFilter(AstridApiConstants.BROADCAST_EVENT_REFRESH));

    }

    @Override
    public void onPause() {
        super.onPause();
        if(adapter != null) {
            adapter.unregisterRecevier();
        }
        try {
            getActivity().unregisterReceiver(refreshReceiver);
        } catch (IllegalArgumentException e) {
            // Might not have fully initialized
            log.error(e.getMessage(), e);
        }
    }

    /* ======================================================================
     * ===================================================== populating lists
     * ====================================================================== */

    /** Sets up the coach list adapter */
    protected void setUpList() {
        adapter.setListView(getListView());
        setListAdapter(adapter);

        // Can't do context menus when list is in popup menu for some reason--workaround
        if (((AstridActivity) getActivity()).fragmentLayout == AstridActivity.LAYOUT_SINGLE) {
            getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    // Do stuff
                    final Filter filter = adapter.getItem(position);
                    final String[] labels = filter.contextMenuLabels;
                    final Intent[] intents = filter.contextMenuIntents;
                    ArrayAdapter<String> intentAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
                    intentAdapter.add(getString(R.string.FLA_context_shortcut));
                    for (String l : labels) {
                        intentAdapter.add(l);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(filter.title);
                    builder.setAdapter(intentAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                showCreateShortcutDialog(getActivity(), ShortcutActivity.createIntent(filter), filter);
                            } else {
                                getActivity().startActivityForResult(intents[which - 1], REQUEST_CUSTOM_INTENT);
                            }
                        }
                    });

                    Dialog d = builder.create();
                    d.setOwnerActivity(getActivity());
                    d.show();
                    return true;
                }

            });
        } else {
            registerForContextMenu(getListView());
        }
    }


    /* ======================================================================
     * ============================================================== actions
     * ====================================================================== */


    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Filter item = adapter.getItem(position);
        mListener.onFilterItemClicked(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

        Filter item = adapter.getItem(info.position);

        android.view.MenuItem menuItem;

        if(item instanceof Filter) {
            menuItem = menu.add(0, CONTEXT_MENU_SHORTCUT, 0, R.string.FLA_context_shortcut);
            menuItem.setIntent(ShortcutActivity.createIntent(item));
        }

        for(int i = 0; i < item.contextMenuLabels.length; i++) {
            if(item.contextMenuIntents.length <= i) {
                break;
            }
            menuItem = menu.add(0, CONTEXT_MENU_INTENT, 0, item.contextMenuLabels[i]);
            menuItem.setIntent(item.contextMenuIntents[i]);
        }

        if(menu.size() > 0) {
            menu.setHeaderTitle(item.listingTitle);
        }
    }

    /**
     * Creates a shortcut on the user's home screen
     */
    private static void createShortcut(Activity activity, Intent shortcutIntent, String label) {
        if(label.length() == 0) {
            return;
        }

        Bitmap bitmap = superImposeListIcon(activity);

        Intent createShortcutIntent = new Intent();
        createShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        createShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
        createShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        createShortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT"); //$NON-NLS-1$

        activity.sendBroadcast(createShortcutIntent);
        Toast.makeText(activity,
                activity.getString(R.string.FLA_toast_onCreateShortcut, label), Toast.LENGTH_LONG).show();
    }

    public static Bitmap superImposeListIcon(Activity activity) {
        return ((BitmapDrawable)activity.getResources().getDrawable(R.drawable.icon)).getBitmap();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        // called when context menu appears
        return onOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // handle my own menus
        switch (item.getItemId()) {
            case CONTEXT_MENU_SHORTCUT: {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
                final Intent shortcutIntent = item.getIntent();
                FilterListItem filter = ((FilterAdapter.ViewHolder)info.targetView.getTag()).item;
                if(filter instanceof Filter) {
                    showCreateShortcutDialog(getActivity(), shortcutIntent, (Filter) filter);
                }

                return true;
            }
            case CONTEXT_MENU_INTENT: {
                Intent intent = item.getIntent();
                getActivity().startActivityForResult(intent, REQUEST_CUSTOM_INTENT);
                return true;
            }
            default: {
                TaskListFragment tasklist = (TaskListFragment) getActivity().getSupportFragmentManager().findFragmentByTag(TaskListFragment.TAG_TASKLIST_FRAGMENT);
                if (tasklist != null && tasklist.isInLayout()) {
                    return tasklist.onOptionsItemSelected(item);
                }
            }
        }
        return false;
    }

    public static void showCreateShortcutDialog(final Activity activity, final Intent shortcutIntent,
            final Filter filter) {
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setPadding(10, 0, 10, 0);
        final EditText editText = new EditText(activity);
        if(filter.listingTitle == null) {
            filter.listingTitle = ""; //$NON-NLS-1$
        }
        editText.setText(filter.listingTitle.
                replaceAll("\\(\\d+\\)$", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
        frameLayout.addView(editText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        final Runnable createShortcut = new Runnable() {
            @Override
            public void run() {
                String label = editText.getText().toString();
                createShortcut(activity, shortcutIntent, label);
            }
        };
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_NULL) {
                    createShortcut.run();
                    return true;
                }
                return false;
            }
        });

        new AlertDialog.Builder(activity)
        .setTitle(R.string.FLA_shortcut_dialog_title)
        .setMessage(R.string.FLA_shortcut_dialog)
        .setView(frameLayout)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createShortcut.run();
            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show().setOwnerActivity(activity);
    }

    public void clear() {
        adapter.clear();
    }

    public void refresh() {
        adapter.clear();
        adapter.getLists();
    }

    /**
     * Receiver which receives refresh intents
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    protected class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || !AstridApiConstants.BROADCAST_EVENT_REFRESH.equals(intent.getAction())) {
                return;
            }

            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }
    }
}
