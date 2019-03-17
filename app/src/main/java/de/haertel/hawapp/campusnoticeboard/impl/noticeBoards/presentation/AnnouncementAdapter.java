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
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

/**
 * Adapter der zum befüllen des Recyclerviews auf den schwarzen Brettern verwendet wird.
 */
public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder> {

    private OnItemClickListener listener;
    private List<Announcement> announcements = new ArrayList<>();

    /**
     * Methode die einen ViewHolder für die Bekanntmachungen liefert.
     *
     * @param pParent   der Recylerview selbst.
     * @param pViewType der ViewType
     * @return das Holder-Objekt
     */
    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup pParent, int pViewType) {
        View itemView = LayoutInflater.from(pParent.getContext())
                .inflate(R.layout.announcement_item, pParent, false);

        return new AnnouncementHolder(itemView);
    }

    /**
     * Methode die getriggert wird, wenn der ViewHolder gebunden wird.
     *
     * @param pAnnouncementHolder der Holder.
     * @param pPosition           die  Position in der Liste.
     */
    @Override
    public void onBindViewHolder(@NonNull AnnouncementHolder pAnnouncementHolder, int pPosition) {
        Announcement currentAnnouncement = announcements.get(pPosition);

        pAnnouncementHolder.textViewPreview_title.setText(currentAnnouncement.getHeadline());
        pAnnouncementHolder.textViewPreview_message.setText(currentAnnouncement.getMessage());
        pAnnouncementHolder.textViewPreview_date.setText(LastInsert.getDateFormat().format(currentAnnouncement.getDate()));
        pAnnouncementHolder.textViewPreview_dayCount.setText(currentAnnouncement.getDayCountSincePosted());
    }

    /**
     * Liefert die Anzahl der Elemente in der Liste.
     *
     * @return die Anzahl
     */
    @Override
    public int getItemCount() {
        return announcements.size();
    }

    /**
     * setzt eine Liste an Bekanntmachungen als die im ViewHolder angezeigte Liste.
     * Die Liste wird davor noch alphabetisch sortiert.
     *
     * @param pAnnouncements
     */
    public void setAnnouncements(List<Announcement> pAnnouncements) {

        List<Announcement> announcementList = pAnnouncements;
        if (announcementList.size() > 1) {
            Collections.sort(announcementList);
        }
        this.announcements = announcementList;
        notifyDataSetChanged();
    }

    /**
     * Liefert eine Bekanntmachung an einer bestimmten Position
     *
     * @param pPosition die Position
     * @return das Announcement
     */
    public Announcement getAnnouncementAt(int pPosition) {
        return announcements.get(pPosition);
    }

    /**
     * Die eigene Implementierung einer ViewHolder Klasse für einen RecyclerView.
     */
    class AnnouncementHolder extends RecyclerView.ViewHolder {
        private TextView textViewPreview_dayCount;
        private TextView textViewPreview_title;
        private TextView textViewPreview_date;
        private TextView textViewPreview_message;

        /**
         * Der Konstruktor.
         *
         * @param itemView die View für die der Holder Daten bereit hält.
         */
        public AnnouncementHolder(@NonNull View itemView) {
            super(itemView);
            textViewPreview_date = itemView.findViewById(R.id.preview_date);
            textViewPreview_dayCount = itemView.findViewById(R.id.preview_day_count);
            textViewPreview_message = itemView.findViewById(R.id.preview_message);
            textViewPreview_title = itemView.findViewById(R.id.preview_title);


            itemView.setOnClickListener(new View.OnClickListener() {
                /**
                 * Setzen eines Listener, der auf anklicken der einzellnen Items hört.
                 * @param v die View
                 */
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(announcements.get(position));

                    }
                }
            });
        }
    }

    /**
     * Festlegen, dass ein ClickListener für Announcements stets die onItemClick Methode überschreiben muss.
     */
    public interface OnItemClickListener {
        void onItemClick(Announcement announcement);
    }

    /**
     * Zuweisen des Listeners.
     *
     * @param pListener der einzuhängende Listener
     */
    public void setOnItemClickListener(OnItemClickListener pListener) {
        this.listener = pListener;
    }
}
