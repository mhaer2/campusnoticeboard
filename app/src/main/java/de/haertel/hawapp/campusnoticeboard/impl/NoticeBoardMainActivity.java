package de.haertel.hawapp.campusnoticeboard.impl;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenuData.MenuEntry;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenuData.MenuEntryViewModel;

import static java.lang.Math.toIntExact;

public class NoticeBoardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    ScrollView scrollView;
    ExpandableListAdapter mFacultyMenuAdapter;
    ExpandableListAdapter mGeneralMenuAdapter;
    ExpandableListView facultyMenuExpandableList;
    private DatabaseReference mDatabase;
    ArrayList<HashMap<String, String>> arrayList;

    ExpandableListView generalMenuExpandableList;
    List<ExpandedMenuModel> navigationMenuParentList;
    HashMap<ExpandedMenuModel, List<String>> navigationMenuChildList;

    public MenuEntryViewModel menuEntryViewModel;

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
        facultyMenuExpandableList = (ExpandableListView) findViewById(R.id.navigationmenufaculty);
        generalMenuExpandableList = (ExpandableListView) findViewById(R.id.navigationmenugeneral);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        mDatabase = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/navigation/noticeBoards/en-US/items");

        //navigationView.setNavigationItemSelectedListener(this);
        menuEntryViewModel = ViewModelProviders.of(this).get(MenuEntryViewModel.class);




        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        _initNavigationListListener();


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                menuEntryViewModel.deleteAllMenuEntries();
                _populateDatabase();
                menuEntryViewModel.getAllMenuEntries().observe(NoticeBoardMainActivity.this, new Observer<List<MenuEntry>>() {
                    @Override
                    public void onChanged(@Nullable final List<MenuEntry> menuEntries) {

                        _prepareNavigationMenu(getString(R.string.facultyTopic), menuEntries);
                        _prepareNavigationMenu(getString(R.string.generalTopic), menuEntries);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void _populateDatabase() {
        int id;
        int menuParentId;
        String title;

        for (HashMap<String, String> hashMap: Objects.requireNonNull(arrayList)) {
            id = -1;
            menuParentId = -1;
            title = null;
            Long temp;
            for (Map.Entry<String, String> entry : hashMap.entrySet() ) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if (key.equals(getString(R.string.flamelinkNavigationId))){
                    temp = (Long.parseLong(value));
                    id = temp.intValue();
                }
                if (key.equals(getString(R.string.flamelinkNavigationParentIndex))){
                    temp = (Long.parseLong(value));
                    menuParentId = temp.intValue();
                }
                if (key.equals(getString(R.string.flamelinkNavigationTitle))){
                    title = value;
                }
            }
            if (id != -1 || menuParentId != -1 || title != null) {
                MenuEntry newEntry = new MenuEntry(id, menuParentId, title);
                menuEntryViewModel.insert(newEntry);
            }
        }
    }


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
    private void _prepareNavigationMenu(String pTopic, List<MenuEntry> pMenuEntries){
        navigationMenuParentList = new ArrayList<ExpandedMenuModel>();
        navigationMenuChildList = new HashMap<ExpandedMenuModel, List<String>>();
        ExpandedMenuModel expandedMenuModel;
        List<String> childsToAdd;

        List<MenuEntry> parentEntries = new ArrayList<MenuEntry>();
        List<MenuEntry> childEntries = new ArrayList<MenuEntry>();


        int rootId = -1;
        for ( MenuEntry entry : pMenuEntries) {
            if (entry.getTitle().equals(pTopic)) {
                rootId = entry.getId();
            }
        }

        for ( MenuEntry entry : pMenuEntries) {
            // falls Entry ein Kindknoten des RootEntrys mit dem Topic pTopic ist
            if (entry.getMenuParentId() == rootId && rootId != -1){
                parentEntries.add(entry);
                // füge alle Kinder des ParentEntries der Liste der Kinder hinzu
                for ( MenuEntry childEntry : pMenuEntries) {
                    if (childEntry.getMenuParentId() == entry.getId()){
                        // falls ChildEntry noch nicht in Liste existiert, füge es hinzu
                        if(!childEntries.contains(childEntry)){
                            childEntries.add(childEntry);
                        }

                    }
                }
            }
        }


        for (MenuEntry menuEntry: parentEntries) {
            expandedMenuModel = new ExpandedMenuModel();
            childsToAdd = new ArrayList<String>();
            expandedMenuModel.setMenuName(menuEntry.getTitle());
            navigationMenuParentList.add(expandedMenuModel);

            for (MenuEntry childEntry: childEntries) {
                if (childEntry.getMenuParentId() == menuEntry.getId()){
                    childsToAdd.add(childEntry.getTitle());
                }
            }
            navigationMenuChildList.put(expandedMenuModel, childsToAdd);
        }
        if(pTopic.equals(getString(R.string.facultyTopic))){
            mFacultyMenuAdapter = new ExpandableListAdapter(this, navigationMenuParentList, navigationMenuChildList, facultyMenuExpandableList);
            facultyMenuExpandableList.setAdapter(mFacultyMenuAdapter);
        } else{
            if (pTopic.equals(getString(R.string.generalTopic))){
                mGeneralMenuAdapter = new ExpandableListAdapter(this, navigationMenuParentList, navigationMenuChildList, generalMenuExpandableList);
                generalMenuExpandableList.setAdapter(mGeneralMenuAdapter);
            }
        }
    }



    /**
     * Falls NavigationBar offen,
     * wird beim benutzen des Android-Back-Buttons nicht die Anwendung geschlossen,
     * sondern die NavigationBar wieder zugeklappt.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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

    private void _initNavigationListListener() {
        facultyMenuExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
//                Log.d(msg, expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString());
//
//                _prepareFacultyListData2();

                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        facultyMenuExpandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });
        generalMenuExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        generalMenuExpandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });
    }


}
