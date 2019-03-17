package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;


/**
 * Diese Klasse dient als Handler für die Menüeinträge des Navigationsbaumes.
 */
public class NavigationMenuDataHandler {
    private final String MENU_ENTRY_STORE_KEY = "menuEntryPreference";
    private final String MY_PREFERENCES = "MyPreferences";
    private static final String NAVIGATION_MENU_REFERENCE_FIREBASE = "flamelink/environments/production/navigation/noticeBoards/en-US/items";

    ExpandableListAdapter mFacultyMenuAdapter;
    ExpandableListAdapter mGeneralMenuAdapter;
    ExpandableListView facultyMenuExpandableList;
    private DatabaseReference mDatabase;
    ArrayList<HashMap<String, String>> arrayList;

    ExpandableListView generalMenuExpandableList;
    List<ExpandedMenuModel> navigationMenuParentList;
    HashMap<ExpandedMenuModel, List<String>> navigationMenuChildList;

    public MenuEntryViewModel menuEntryViewModel;
    private Activity noticeBoardActivity;

    /**
     * Der Konstruktor.
     *
     * @param pActivity die Activity, in der der Handler erstellt wurde.
     */
    public NavigationMenuDataHandler(Activity pActivity) {
        noticeBoardActivity = pActivity;
    }

    /**
     * Methode, die den Navigationsbaum mit Daten befüllt und updated falls Netzwerk verfügbar ist und sich am Menübaum etwas geändert hat.
     * Frägt von Firebase die Daten ab und Cached diese.
     */
    public void handleNavigationMenuData() {
        facultyMenuExpandableList = (ExpandableListView) noticeBoardActivity.findViewById(R.id.navigationmenufaculty);
        generalMenuExpandableList = (ExpandableListView) noticeBoardActivity.findViewById(R.id.navigationmenugeneral);

        mDatabase = FirebaseDatabase.getInstance()
                .getReference(NAVIGATION_MENU_REFERENCE_FIREBASE);

        menuEntryViewModel = ViewModelProviders.of((FragmentActivity) noticeBoardActivity).get(MenuEntryViewModel.class);

        if (!_isNetworkAvailable()) {
            List<MenuEntry> storedMenuEntries = _fetchMenuEntries();
            if (storedMenuEntries != null) {
                _prepareNavigationMenu(noticeBoardActivity.getString(R.string.facultyTopic), storedMenuEntries);
                _prepareNavigationMenu(noticeBoardActivity.getString(R.string.generalTopic), storedMenuEntries);
            }
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            /**
             * Bei Änderung der Daten in der Firebase Datenbank wird der Navigationsbaum neu befüllt
             * @param dataSnapshot snapshot von den Daten der angegebnen Referenz.
             */
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (_isNetworkAvailable()) {
                    arrayList = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                    menuEntryViewModel.deleteAllMenuEntries();
                    _populateDatabase();
                    menuEntryViewModel.getAllMenuEntries().observe((LifecycleOwner) noticeBoardActivity, new Observer<List<MenuEntry>>() {
                        /**
                         * Falls sich Daten in der Firebase DB geändert haben wird diese Methode getriggert.
                         * @param menuEntries die neuen Menueinträge.
                         */
                        @Override
                        public void onChanged(@Nullable final List<MenuEntry> menuEntries) {
                            if (menuEntries != null && _isNetworkAvailable()) {
                                _storeMenuEntries(menuEntries);
                                _prepareNavigationMenu(noticeBoardActivity.getString(R.string.facultyTopic), menuEntries);
                                _prepareNavigationMenu(noticeBoardActivity.getString(R.string.generalTopic), menuEntries);
                            }
                        }
                    });
                }
            }

            /**
             * nicht implementiert, daher keine Funktion.
             * @param databaseError Fehlermeldung
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //no implementation
            }
        });
    }

    /**
     * Speichert die Menüeinträge als schared preference, damit der Menübaum auch ohne Netzwerk aufgebaut werden kann.
     *
     * @param pMenuEntries die Menüeinträge.
     * @return true falls erfolgreich.
     */
    @SuppressLint("ApplySharedPref")
    private boolean _storeMenuEntries(List<MenuEntry> pMenuEntries) {
        boolean returnValue;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = noticeBoardActivity.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = gson.toJson(pMenuEntries);
        editor = sharedPreferences.edit();
        editor.remove(MENU_ENTRY_STORE_KEY).commit();
        editor.putString(MENU_ENTRY_STORE_KEY, json);
        returnValue = editor.commit();

        return returnValue;
    }

    /**
     * Liefert die in den Shared Preferences gespeicherten Menüeinträge.
     *
     * @return die Liste der Menüeinträge.
     */
    private List<MenuEntry> _fetchMenuEntries() {
        SharedPreferences sharedPreferences;
        sharedPreferences = noticeBoardActivity.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            Gson gson = new Gson();
            String response = sharedPreferences.getString(MENU_ENTRY_STORE_KEY, "");
            ArrayList<MenuEntry> menuEntryList = gson.fromJson(response,
                    new TypeToken<List<MenuEntry>>() {
                    }.getType());
            return menuEntryList;
        } else {
            return null;
        }
    }

    /**
     * Methode, die die Datenbank mit den Menüeinträgen befüllt, falls neue Einträge hinzukommen.
     */
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
                if (key.equals(noticeBoardActivity.getString(R.string.flamelinkNavigationId))) {
                    temp = (Long.parseLong(value));
                    id = temp.intValue();
                }
                if (key.equals(noticeBoardActivity.getString(R.string.flamelinkNavigationParentIndex))) {
                    temp = (Long.parseLong(value));
                    menuParentId = temp.intValue();
                }
                if (key.equals(noticeBoardActivity.getString(R.string.flamelinkNavigationTitle))) {
                    title = value;
                }
            }
            if (id != -1 || menuParentId != -1 || title != null) {
                MenuEntry newEntry = new MenuEntry(id, menuParentId, title);
                menuEntryViewModel.insert(newEntry);
            }
        }
    }

    /**
     * Bereitet das NavigationsMenü vor, indem es die ListViews mti Daten befüllt.
     *
     * @param pTopic       das Topic welches hier befüllt werden soll (z.B. Fakultäten)
     * @param pMenuEntries alle Menüeinträge
     */
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
            // falls Entry ein Kindknoten des RootEntrys mit dem AnnouncementTopic pTopic ist
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
        if (pTopic.equals(noticeBoardActivity.getString(R.string.facultyTopic))) {
            mFacultyMenuAdapter = new ExpandableListAdapter(noticeBoardActivity, navigationMenuParentList, navigationMenuChildList, facultyMenuExpandableList);
            facultyMenuExpandableList.setAdapter(mFacultyMenuAdapter);
        } else {
            if (pTopic.equals(noticeBoardActivity.getString(R.string.generalTopic))) {
                mGeneralMenuAdapter = new ExpandableListAdapter(noticeBoardActivity, navigationMenuParentList, navigationMenuChildList, generalMenuExpandableList);
                generalMenuExpandableList.setAdapter(mGeneralMenuAdapter);
            }
        }
        // Sortieren der Liste nach Alphabet
        Collections.sort(navigationMenuParentList, new Comparator<ExpandedMenuModel>() {
            @Override
            public int compare(ExpandedMenuModel model1, ExpandedMenuModel model2) {
                return model1.getMenuName().toLowerCase().compareTo(model2.getMenuName().toLowerCase());
            }
        });
        for (List<String> list : navigationMenuChildList.values()) {
            // Sortieren der Liste nach Alphabet
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String string1, String string2) {
                    return string1.toLowerCase().compareTo(string2.toLowerCase());
                }
            });
        }

    }

    /**
     * Initialisieren der OnClick Listener für die Menüeinträge.
     */
    public void initNavigationListListener() {
        facultyMenuExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            /**
             * OnChildKlick der Fakultäten-Gruppen
             * @param expandableListView die ListView
             * @param view die aktelle View
             * @param groupPosition die GruppenPosition (Informatik, Maschinenbau...)
             * @param childPosition die Kindposition des Schwarzen Brettes
             * @param id die id
             * @return true wenn gedrückt
             */
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                AnnouncementTopic.setTopic(expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString());
                noticeBoardActivity.setTitle(AnnouncementTopic.getTopic());
                return false;
            }
        });

        generalMenuExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            /**
             * OnChildKlick der Allgemein-Gruppen
             * @param expandableListView die ListView
             * @param view die aktelle View
             * @param groupPosition die GruppenPosition (Informatik, Maschinenbau...)
             * @param childPosition die Kindposition des Schwarzen Brettes
             * @param id die id
             * @return true wenn gedrückt
             */
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                AnnouncementTopic.setTopic(expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString());
                noticeBoardActivity.setTitle(AnnouncementTopic.getTopic());
                return false;
            }
        });
    }

    /**
     * Checkt auf Netzwer Verfügbarkeit
     *
     * @return true falls verfügbar.
     */
    private boolean _isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) noticeBoardActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
