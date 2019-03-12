package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

public class AnnouncementViewModel extends AndroidViewModel {
    private AnnouncementRepository repository;

    public AnnouncementViewModel(@NonNull Application pApplication){
        super(pApplication);
        repository = new AnnouncementRepository(pApplication);
    }

    public void insert(Announcement pAnnouncement) {
        repository.insert(pAnnouncement);
    }

    public void update(Announcement pAnnouncement) {
        repository.update(pAnnouncement);
    }

    public void delete(Announcement pAnnouncement) {
        repository.delete(pAnnouncement);
    }

    public void deleteAllNotes() {
        repository.deleteAllNotes();
    }

    public void deleteOlderAnnouncements(Date pDeleteBefore){
        repository.deleteOlderAnnouncements(pDeleteBefore);
    }

    @NonNull
    public LiveData<List<Announcement>> getAllAnnouncementsForTopic(String pTopic){
        return repository.getAllAnnouncementsForTopic(pTopic);
    }
}
