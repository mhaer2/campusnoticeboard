package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.presentation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.R;

/**
 * Adapter der zum befüllen der ListViews des Navigationsbaumes verwendet wird.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<ExpandedMenuModel> mListDataHeader; // header titles

    private HashMap<ExpandedMenuModel, List<String>> mListDataChild;
    private ExpandableListView expandList;

    /**
     * Der Konstruktor
     *
     * @param context        der Kontext
     * @param listDataHeader die Liste mit den Header-Menüeinträgen
     * @param listChildData  die Liste mit den Kind-Menüeinträgen
     * @param mView          die ListView
     */
    public ExpandableListAdapter(Context context, List<ExpandedMenuModel> listDataHeader, HashMap<ExpandedMenuModel, List<String>> listChildData, ExpandableListView mView) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
        this.expandList = mView;
    }

    /**
     * @return die Anzahl der Gruppen.
     */
    @Override
    public int getGroupCount() {
        int i = mListDataHeader.size();
        return this.mListDataHeader.size();
    }

    /**
     * @param groupPosition die Position der Gruppe.
     * @return die Anzahl der  Kinder zu einer gegebenen Gruppe
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        int childCount;

        childCount = Objects.requireNonNull(this.mListDataChild.get(this.mListDataHeader.get(groupPosition)))
                .size();

        return childCount;
    }

    /**
     * Liefert die Gruppe für eine gegebene Position.
     *
     * @param groupPosition die Gruppenposition
     * @return das Gruppen Objekt
     */
    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    /**
     * Liefert ein Child für gegebene Positionen.
     *
     * @param groupPosition die Grupppenposition.
     * @param childPosition die Kindposition.
     * @return Das Child-Objekt
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(this.mListDataChild.get(this.mListDataHeader.get(groupPosition)))
                .get(childPosition);
    }

    /**
     * Liefert die GruppenId
     *
     * @param groupPosition die Position des Elements.
     * @return die  ID.
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Liefert die ChildId
     *
     * @param groupPosition die Position des Elements.
     * @param childPosition die Childposition.
     * @return die  ID.
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * @return immer false.
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Liefert die Ansicht der Gruppe.
     *
     * @param groupPosition die Gruppenposition.
     * @param isExpanded    true falls Gruppe expanded
     * @param convertView   die convertView, also alte View
     * @param parent        Der Parent als ViewGroup
     * @return Die View der Gruppe für die angegebenen Parameter.
     */
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandedMenuModel headerTitle = (ExpandedMenuModel) getGroup(groupPosition);
        View view = convertView;
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.navigation_expandablelist_menu, null);
        }
        TextView lblListHeader = view
                .findViewById(R.id.expandablelistMenu);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle.getMenuName());
        return view;
    }

    /**
     * Liefert die View eines Childs.
     *
     * @param groupPosition die Position der Gruppe.
     * @param childPosition die KindPosition.
     * @param isLastChild   ob es das letzte Kind ist.
     * @param convertView   die convertView, also alte View
     * @param parent        der Parent
     * @return Die View des Childs für die angegebenen Parameter.
     */
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navigation_expandablelist_submenu, null);
        }

        TextView txtListChild = convertView
                .findViewById(R.id.expandablelistSubMenu);

        txtListChild.setText(childText);

        return convertView;
    }

    /**
     * Liefert ob Kind selektierbar.
     *
     * @param groupPosition gruppenposition des Childs
     * @param childPosition Childposition in der Gruppe
     * @return true falls seletktierbar.
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}