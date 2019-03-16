package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder> {

    private List<Announcement> announcements = new ArrayList<>();
    String pattern = "dd/MM/yyyy HH:mm";
    DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());

    /**
     * @param pParent   der Recylerview selbst.
     * @param pViewType
     * @return
     */
    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup pParent, int pViewType) {
        View itemView = LayoutInflater.from(pParent.getContext())
                .inflate(R.layout.announcement_item, pParent, false);

        return new AnnouncementHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementHolder pAnnouncementHolder, int pPosition) {
        Announcement currentAnnouncement = announcements.get(pPosition);

        Date announcementDate = currentAnnouncement.getDate();
        String dayCount = _getDayCount(announcementDate);

        pAnnouncementHolder.textViewPreview_title.setText(currentAnnouncement.getHeadline());
        pAnnouncementHolder.textViewPreview_message.setText(currentAnnouncement.getMessage());
        pAnnouncementHolder.textViewPreview_date.setText(dateFormat.format(currentAnnouncement.getDate()));
        pAnnouncementHolder.textViewPreview_dayCount.setText(dayCount);
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public void setAnnouncements(List<Announcement> pAnnouncements){

        List<Announcement> announcementList = pAnnouncements;
        if (announcementList.size() > 1) {
            Collections.sort(announcementList);
        }
        this.announcements = announcementList;
        notifyDataSetChanged();
    }

    private String _getDayCount(Date pAnnouncementDate){
        Date actualDate = new Date();
        long differenceMillis = actualDate.getTime() - pAnnouncementDate.getTime();
        int dayDifference = (int) (differenceMillis / (24 * 60 * 60 * 1000));
        if (dayDifference > 7){
            return ">7T";
        }
        String dayCount;
        switch (dayDifference) {
            case 0:
                dayCount = "NEW";
                break;
            case 1:
                dayCount = "1T";
                break;
            case 2:
                dayCount = "2T";
                break;
            case 3:
                dayCount = "3T";
                break;
            case 4:
                dayCount = "4T";
                break;
            case 5:
                dayCount = "5T";
                break;
            case 6:
                dayCount = "6T";
                break;
            case 7:
                dayCount = "7T";
                break;
            default:
                dayCount = "-1";
                break;
        }
        return dayCount;
    }


    class AnnouncementHolder extends RecyclerView.ViewHolder {
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
