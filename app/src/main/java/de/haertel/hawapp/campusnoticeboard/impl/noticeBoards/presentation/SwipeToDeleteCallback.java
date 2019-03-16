package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;


public  class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private AnnouncementAdapter mAdapter;
    private Activity activity;
    private AnnouncementViewModel announcementViewModel;

    private Drawable icon;
    private final ColorDrawable background;


    public SwipeToDeleteCallback(Activity pActivity, AnnouncementViewModel pAnnouncementViewModel, AnnouncementAdapter pAdapter) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        announcementViewModel = pAnnouncementViewModel;
        mAdapter = pAdapter;
        activity = pActivity;
        icon = activity.getApplicationContext().getDrawable(R.drawable.ic_delete);
        background = new ColorDrawable(Color.RED);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // used for up and down movements
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        announcementViewModel.delete(mAdapter.getAnnouncementAt(viewHolder.getAdapterPosition()));
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 8; //so background is behind the rounded corners of itemView

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft+ icon.getIntrinsicWidth();
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