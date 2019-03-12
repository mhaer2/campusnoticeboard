package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder> {

    private List<Announcement> announcements = new ArrayList<>();

    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementHolder announcementHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class AnnouncementHolder extends RecyclerView.ViewHolder{
        private TextView textViewPreview_dayCount;
        private TextView textViewPreview_title;
        private TextView textViewPreview_date;
        private TextView textViewPreview_message;

        public AnnouncementHolder(@NonNull View itemView) {
            super(itemView);
            textViewPreview_date = itemView.findViewById(R.id.preview_date);
            textViewPreview_dayCount = itemView.findViewById(R.id.preview_day_count);
            textViewPreview_message = itemView.findViewById(R.id.preview_message);
            textViewPreview_title = itemView.findViewById(R.id.preview_title);
        }
    }
}
