package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;


import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.Date;
import java.util.List;


/**
 * Repository, das als Mediator fungiert. So kann das View Model auf ein Repository zugreifen,
 * und das Repository leitet die Anfrage an verschiedene Datenquellen weiter.
 */
public class AnnouncementRepository {
    private AnnouncementDao announcementDao;

    /**
     * Konstruktor des Repositories.
     *
     * @param pApplication das Applikation-Objekt
     */
    AnnouncementRepository(Application pApplication) {
        AnnouncementDatabase database = AnnouncementDatabase.getInstance(pApplication);
        announcementDao = database.announcementDao();
    }

    /**
     * Ruft einen Asynchronen Task auf, der den Insert vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    public void insert(Announcement pAnnouncement) {
        new InsertAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    /**
     * Ruft einen Asynchronen Task auf, der das Update vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    void update(Announcement pAnnouncement) {
        new UpdateAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    /**
     * Ruft einen Asynchronen Task auf, der das Delte vornimmt.
     *
     * @param pAnnouncement die Bekanntmachung
     */
    public void delete(Announcement pAnnouncement) {
        new DeleteAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    /**
     * Ruft einen Asynchronen Task auf der alle Datensätze löscht vornimmt.
     */
    void deleteAllNotes() {
        new DeleteAllAnnouncementAsyncTask(announcementDao).execute();
    }

    /**
     * Ruft einen Asynchronen Task auf der alle Datensätze, die vor einen bestimmten Datum liegen löscht.
     *
     * @param pDeleteBefore das Datum vor dem gelöscht werden soll.
     */
    void deleteOlderAnnouncements(Date pDeleteBefore) {
        new DeleteOlderAnnouncementAsyncTask(announcementDao).execute(pDeleteBefore);
    }

    /**
     * Ruft einen Asynchronen Task auf, der eine Liste an Datensätzen liefert.
     *
     * @param pTopic das Topic für das die Datensätze geliefert werden sollen.
     * @return die Bekanntmachungen als Live Daten.
     */
    LiveData<List<Announcement>> getAllAnnouncementsForTopic(String pTopic) {

        return announcementDao.getAnnouncementsForTopic(pTopic);
    }

    /**
     * Der Asynchrone Task, der die Bekanntmachung einpflegt.
     */
    private static class InsertAnnouncementAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementDao announcementDao;

        private InsertAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementDao.insert(pAnnouncements[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der die Bekanntmachung updated.
     */
    private static class UpdateAnnouncementAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementDao announcementDao;

        private UpdateAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementDao.update(pAnnouncements[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der die Bekanntmachung löscht.
     */
    private static class DeleteAnnouncementAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementDao announcementDao;

        private DeleteAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementDao.delete(pAnnouncements[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der alle Bekanntmachung löscht.
     */
    private static class DeleteAllAnnouncementAsyncTask extends AsyncTask<Void, Void, Void> {
        private AnnouncementDao announcementDao;

        private DeleteAllAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            announcementDao.deleteAllAnnouncements();
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der alle Bekanntmachungen vor einen bestimmten Datum löscht.
     */
    private static class DeleteOlderAnnouncementAsyncTask extends AsyncTask<Date, Void, Void> {
        private AnnouncementDao announcementDao;

        private DeleteOlderAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Date... pDate) {
            announcementDao.deleteOlderAnnouncements(pDate[0]);
            return null;
        }
    }
}
