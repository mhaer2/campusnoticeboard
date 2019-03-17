package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;

/**
 * Implementierung eines Callbacks,
 * dass die Funktion SwipeToDelete sowohl nach links als auch nach rechts bereitstellt.
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private AnnouncementAdapter mAdapter;
    private Activity activity;
    private AnnouncementViewModel announcementViewModel;

    private Drawable icon;
    private final ColorDrawable background;


    /**
     * Der Konstruktor.
     *
     * @param pActivity              die aufrufende Activity
     * @param pAnnouncementViewModel das ViewModel mit den Announcements
     * @param pAdapter               der AnnouncementAdapter
     */
    public SwipeToDeleteCallback(Activity pActivity, AnnouncementViewModel pAnnouncementViewModel, AnnouncementAdapter pAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        announcementViewModel = pAnnouncementViewModel;
        mAdapter = pAdapter;
        activity = pActivity;
        icon = activity.getApplicationContext().getDrawable(R.drawable.ic_delete);
        background = new ColorDrawable(Color.RED);
    }

    /**
     * Normalerweise benutzt für up and down swipes, welche hier nicht benötigt werden.
     *
     * @param recyclerView der RecyclerView
     * @param target       die Zielrichtung
     * @param viewHolder   der Viewholder
     * @return immer false
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        return false;
    }

    /**
     * Legt fest, was beim Swipe ausgeführt werden soll.
     *
     * @param viewHolder der ViewHolder der Liste
     * @param direction  die Richtung in welche gewischt wird.
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        announcementViewModel.delete(mAdapter.getAnnouncementAt(viewHolder.getAdapterPosition()));
    }


    /**
     * Setzt einen Hintergrund mit animation für das Swipen.
     * Ein delete-Icon wird links oder rechts eingeblendet, je nach dem in welche Richtung geswiped wird.
     *
     * @param c                 The canvas which RecyclerView is drawing its children
     * @param recyclerView      The RecyclerView to which ItemTouchHelper is attached to
     * @param viewHolder        The ViewHolder which is being interacted by the User or it was interacted and simply animating to its original position
     * @param dX                the amount of horizontal displacement caused by user's action
     * @param dY                The amount of vertical displacement caused by user's action
     * @param actionState       The type of interaction on the View. Is either ACTION_STATE_DRAG or ACTION_STATE_SWIPE.
     * @param isCurrentlyActive True if this view is currently being controlled by the user or false it is simply animating back to its original state
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 8;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}