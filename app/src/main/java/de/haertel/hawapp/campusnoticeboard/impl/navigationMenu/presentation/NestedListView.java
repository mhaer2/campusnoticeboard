package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.presentation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.view.View.OnTouchListener;
import android.widget.ListAdapter;

/**
 * Eigene Implementierung des ExpandableListViews,
 * der es erlaubt,
 * die ListView innerhalb einer Scrollbar zu verwenden.
 */
public class NestedListView extends ExpandableListView implements OnTouchListener, AbsListView.OnScrollListener {

    private int listViewTouchAction;
    private static final int MAXIMUM_LIST_ITEMS_VIEWABLE = 1000;

    /**
     * Konstruktor für die eigene ExpandableListView.
     *
     * @param context der Kontext.
     * @param attrs   Attribute, die dem Super Konstruktor weitergegeben werden.
     */
    public NestedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        listViewTouchAction = -1;
        setOnScrollListener(this);
        setOnTouchListener(this);
    }

    /**
     * Wird bei einem Klick aufgerufen
     *
     * @param v     die View
     * @param event Das Touchevent
     * @return immer false
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getAdapter() != null && getAdapter().getCount() > MAXIMUM_LIST_ITEMS_VIEWABLE) {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE) {
                scrollBy(0, 1);
            }
        }
        return false;
    }

    /**
     * Methode ohne Funktion, da nicht implementiert
     *
     * @param view        der View
     * @param scrollState der Status der Scrollleiste.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * Bestimmt was beim Scrollen passieren soll.
     *
     * @param view             die View in der gescrollt wird.
     * @param firstVisibleItem erstes Item das sichtbar ist
     * @param visibleItemCount anzahl der zu sehenden Items.
     * @param totalItemCount   Anzhal aller Items.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (getAdapter() != null && getAdapter().getCount() > MAXIMUM_LIST_ITEMS_VIEWABLE) {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE) {
                scrollBy(0, -1);
            }
        }
    }


    /**
     * Methode, die die Höhe und Breite der View berechnet,
     * je nachdem wie viele Kinder aufgeklappt sind und wie viele Kinder diese entsprechend haben
     *
     * @param widthMeasureSpec  die Benötigte Breite
     * @param heightMeasureSpec die benötigte Höhe.
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newHeight = 0;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            ListAdapter listAdapter = getAdapter();
            if (listAdapter != null && !listAdapter.isEmpty()) {
                int listPosition = 0;
                for (listPosition = 0; listPosition < listAdapter.getCount()
                        && listPosition < MAXIMUM_LIST_ITEMS_VIEWABLE; listPosition++) {
                    View listItem = listAdapter.getView(listPosition, null, this);
                    //now it will not throw a NPE if listItem is a ViewGroup instance
                    if (listItem instanceof ViewGroup) {
                        listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    }
                    listItem.measure(widthMeasureSpec, heightMeasureSpec);
                    newHeight += listItem.getMeasuredHeight();
                }
                newHeight += getDividerHeight() * listPosition;
            }
            if ((heightMode == MeasureSpec.AT_MOST) && (newHeight > heightSize)) {
                newHeight = heightSize;
            }
        } else {
            newHeight = getMeasuredHeight();
        }
        // setzen der neuen Dimensionen
        setMeasuredDimension(getMeasuredWidth(), newHeight);
    }
}
