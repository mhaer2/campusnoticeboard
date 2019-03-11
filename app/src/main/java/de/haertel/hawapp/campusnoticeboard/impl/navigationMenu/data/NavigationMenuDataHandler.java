package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.presentation.ExpandableListAdapter;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.presentation.ExpandedMenuModel;

public class NavigationMenuDataHandler {
    ExpandableListAdapter mFacultyMenuAdapter;
    ExpandableListAdapter mGeneralMenuAdapter;
    ExpandableListView facultyMenuExpandableList;
    private DatabaseReference mDatabase;
    ArrayList<HashMap<String, String>> arrayList;

    ExpandableListView generalMenuExpandableList;
    List<ExpandedMenuModel> navigationMenuParentList;
    HashMap<ExpandedMenuModel, List<String>> navigationMenuChildList;

    public MenuEntryViewModel menuEntryViewModel;

    private Activity mainActivity;

    public NavigationMenuDataHandler(Activity pMainActivity) {
        mainActivity = pMainActivity;
    }

    public void handleNavigationMenuData() {
        facultyMenuExpandableList = (ExpandableListView) mainActivity.findViewById(R.id.navigationmenufaculty);
        generalMenuExpandableList = (ExpandableListView) mainActivity.findViewById(R.id.navigationmenugeneral);

        mDatabase = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/navigation/noticeBoards/en-US/items");

        //navigationView.setNavigationItemSelectedListener(this);
        menuEntryViewModel = ViewModelProviders.of((FragmentActivity) mainActivity).get(MenuEntryViewModel.class);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                menuEntryViewModel.deleteAllMenuEntries();
                _populateDatabase();
                menuEntryViewModel.getAllMenuEntries().observe((LifecycleOwner) mainActivity, new Observer<List<MenuEntry>>() {
                    @Override
                    public void onChanged(@Nullable final List<MenuEntry> menuEntries) {
                        if (menuEntries != null){
                            _prepareNavigationMenu(mainActivity.getString(R.string.facultyTopic), menuEntries);
                            _prepareNavigationMenu(mainActivity.getString(R.string.generalTopic), menuEntries);
                        }
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

        for (HashMap<String, String> hashMap : Objects.requireNonNull(arrayList)) {
            id = -1;
            menuParentId = -1;
            title = null;
            Long temp;
            for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if (key.equals(mainActivity.getString(R.string.flamelinkNavigationId))) {
                    temp = (Long.parseLong(value));
                    id = temp.intValue();
                }
                if (key.equals(mainActivity.getString(R.string.flamelinkNavigationParentIndex))) {
                    temp = (Long.parseLong(value));
                    menuParentId = temp.intValue();
                }
                if (key.equals(mainActivity.getString(R.string.flamelinkNavigationTitle))) {
                    title = value;
                }
            }
            if (id != -1 || menuParentId != -1 || title != null) {
                MenuEntry newEntry = new MenuEntry(id, menuParentId, title);
                menuEntryViewModel.insert(newEntry);
            }
        }
    }

    private void _prepareNavigationMenu(String pTopic, List<MenuEntry> pMenuEntries) {
        navigationMenuParentList = new ArrayList<ExpandedMenuModel>();
        navigationMenuChildList = new HashMap<ExpandedMenuModel, List<String>>();
        ExpandedMenuModel expandedMenuModel;
        List<String> childsToAdd;

        List<MenuEntry> parentEntries = new ArrayList<MenuEntry>();
        List<MenuEntry> childEntries = new ArrayList<MenuEntry>();


        int rootId = -1;
        for (MenuEntry entry : pMenuEntries) {
            if (entry.getTitle().equals(pTopic)) {
                rootId = entry.getId();
            }
        }

        for (MenuEntry entry : pMenuEntries) {
            // falls Entry ein Kindknoten des RootEntrys mit dem Topic pTopic ist
            if (entry.getMenuParentId() == rootId && rootId != -1) {
                parentEntries.add(entry);
                // füge alle Kinder des ParentEntries der Liste der Kinder hinzu
                for (MenuEntry childEntry : pMenuEntries) {
                    if (childEntry.getMenuParentId() == entry.getId()) {
                        // falls ChildEntry noch nicht in Liste existiert, füge es hinzu
                        if (!childEntries.contains(childEntry)) {
                            childEntries.add(childEntry);
                        }

                    }
                }
            }
        }


        for (MenuEntry menuEntry : parentEntries) {
            expandedMenuModel = new ExpandedMenuModel();
            childsToAdd = new ArrayList<String>();
            expandedMenuModel.setMenuName(menuEntry.getTitle());
            navigationMenuParentList.add(expandedMenuModel);

            for (MenuEntry childEntry : childEntries) {
                if (childEntry.getMenuParentId() == menuEntry.getId()) {
                    childsToAdd.add(childEntry.getTitle());
                }
            }
            navigationMenuChildList.put(expandedMenuModel, childsToAdd);
        }
        if (pTopic.equals(mainActivity.getString(R.string.facultyTopic))) {
            mFacultyMenuAdapter = new ExpandableListAdapter(mainActivity, navigationMenuParentList, navigationMenuChildList, facultyMenuExpandableList);
            facultyMenuExpandableList.setAdapter(mFacultyMenuAdapter);
        } else {
            if (pTopic.equals(mainActivity.getString(R.string.generalTopic))) {
                mGeneralMenuAdapter = new ExpandableListAdapter(mainActivity, navigationMenuParentList, navigationMenuChildList, generalMenuExpandableList);
                generalMenuExpandableList.setAdapter(mGeneralMenuAdapter);
            }
        }

        Collections.sort(navigationMenuParentList, new Comparator<ExpandedMenuModel>() {
            @Override
            public int compare(ExpandedMenuModel model1, ExpandedMenuModel model2) {
                return model1.getMenuName().toLowerCase().compareTo(model2.getMenuName().toLowerCase());
            }
        });
        System.out.println();
        for (List<String> list : navigationMenuChildList.values()) {
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String string1, String string2) {
                    return string1.toLowerCase().compareTo(string2.toLowerCase());
                }
            });
        }
        System.out.println();

    }

    public void initNavigationListListener() {
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
