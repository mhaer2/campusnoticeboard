package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * Das ViewModel für die Bekanntmachungen.
 * Über diese ViewModel können alle festgelegten Datenbankzugriffe ausgeführt werden.
 */
public class AnnouncementViewModel extends AndroidViewModel {
    private AnnouncementRepository repository;

    /**
     * Konstruktor des ViewModels
     *
     * @param pApplication das Applikations-Objekt
     */
    public AnnouncementViewModel(@NonNull Application pApplication) {
        super(pApplication);
        repository = new AnnouncementRepository(pApplication);
    }

    /**
     * Ruft die Methode im Repository auf, die den Insert vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    public void insert(Announcement pAnnouncement) {
        repository.insert(pAnnouncement);
    }

    /**
     * Ruft die Methode im Repository auf, die das Update vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    public void update(Announcement pAnnouncement) {
        repository.update(pAnnouncement);
    }

    /**
     * Ruft die Methode im Repository auf, die das Delete vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    public void delete(Announcement pAnnouncement) {
        repository.delete(pAnnouncement);
    }

    /**
     * Ruft die Methode im Repository auf, die alle Bekanntmachungen löscht vornimmt.
     */
    public void deleteAllNotes() {
        repository.deleteAllNotes();
    }

    /**
     * Ruft die Methode im Repository auf, die alle Bekanntmachungen vor einem bestimmten Datum löscht.
     *
     * @param pDeleteBefore das Datum vor dem gelöscht werden soll.
     */
    public void deleteOlderAnnouncements(Date pDeleteBefore) {
        repository.deleteOlderAnnouncements(pDeleteBefore);
    }

    /**
     * Ruft die Methode im Repository auf, die alle Einträge eines Topics bereitstellt.
     *
     * @param pTopic das Topic
     * @return eine Liste mit Bekanntmachungen als LiveData.
     */
    @NonNull
    public LiveData<List<Announcement>> getAllAnnouncementsForTopic(String pTopic) {
        return repository.getAllAnnouncementsForTopic(pTopic);
    }
}
