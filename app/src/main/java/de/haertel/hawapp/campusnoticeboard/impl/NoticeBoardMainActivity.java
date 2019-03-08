package de.haertel.hawapp.campusnoticeboard.impl;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.haertel.hawapp.campusnoticeboard.R;

public class NoticeBoardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    ScrollView scrollView;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListAdapter mMenuAdapterGeneral;
    ExpandableListView expandableList;

    ExpandableListView expandableListGeneral;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //create HamburgerButton to toggle the NavigationDrawer and add it to the Toolbar
        ActionBarDrawerToggle toggleButton = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggleButton);
        toggleButton.syncState();

        scrollView = (ScrollView) findViewById(R.id.scrollViewDrawer);
        expandableList = (ExpandableListView) findViewById(R.id.navigationmenufaculty);
        expandableListGeneral = (ExpandableListView) findViewById(R.id.navigationmenugeneral);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        //_updateNavMenuDrawer();

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        prepareListData();
        mMenuAdapterGeneral= new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableListGeneral);
        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);
        expandableListGeneral.setAdapter(mMenuAdapterGeneral);


        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });
        expandableList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) expandableList.getLayoutParams();
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;//(expandableList. * expandableList.getHeight());
                expandableList.setLayoutParams(param);
                expandableList.requestLayout();
                expandableList.refreshDrawableState();
                scrollView.refreshDrawableState();
            }
        });
        expandableList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) expandableList.getLayoutParams();
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                expandableList.setLayoutParams(param);
                expandableList.requestLayout();
                expandableList.refreshDrawableState();
                scrollView.refreshDrawableState();
            }
        });
        expandableListGeneral.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        expandableListGeneral.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });
        expandableListGeneral.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) expandableListGeneral.getLayoutParams();
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;//    (expandableListGeneral.getChildCount() * expandableListGeneral.getHeight());
                expandableListGeneral.setLayoutParams(param);
                expandableListGeneral.requestLayout();
                expandableListGeneral.refreshDrawableState();
                scrollView.refreshDrawableState();
            }
        });

        expandableListGeneral.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) expandableListGeneral.getLayoutParams();
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                expandableListGeneral.setLayoutParams(param);
                expandableListGeneral.requestLayout();
                expandableListGeneral.refreshDrawableState();
                scrollView.refreshDrawableState();
            }
        });
    }

//    public void setListViewHeightBasedOnChildren(ExpandableListView listView) {
//        ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null) {
//            // pre-condition
//            return;
//        }
//
//        int totalHeight = 0;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(0, 0);
//            totalHeight += listItem.getMeasuredHeight();
//        }
//
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
//        listView.requestLayout();
//    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawer.closeDrawers();
                        return true;
                    }
                });
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("heading1");
        // Adding data header
        listDataHeader.add(item1);

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("heading2");
        listDataHeader.add(item2);

        ExpandedMenuModel item3 = new ExpandedMenuModel();
        item3.setIconName("heading3");
        listDataHeader.add(item3);

        // Adding child data
        List<String> heading1 = new ArrayList<String>();
        heading1.add("Submenu of item 1");

        List<String> heading2 = new ArrayList<String>();
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");

        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");

        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");

        listDataChild.put(listDataHeader.get(0), heading1);// Header, Child data
        listDataChild.put(listDataHeader.get(1), heading2);

    }

    private void _updateNavMenuDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        Menu fakul = menu.addSubMenu("Fakul");
        Menu inf = menu.addSubMenu("IF");
        Menu bwl = fakul.addSubMenu("BWL");
        inf.add("Informatik, B.Sc.");
        inf.add("Informatik, M.Sc.");
        inf.add("WIF, B.Sc.");

        navView.invalidate();
    }

    /**
     * Falls NavigationBar offen,
     * wird beim benutzen des Android-Back-Buttons nicht die Anwendung geschlossen,
     * sondern die NavigationBar wieder zugeklappt.
     */
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notice_board_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
