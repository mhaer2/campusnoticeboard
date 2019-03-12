package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;


import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

public class AnnouncementRepository {
    private AnnouncementDao announcementDao;

//    @NonNull
//    private LiveData<List<Announcement>> allAnnouncementsForTopic = new MutableLiveData<List<Announcement>>();

    public AnnouncementRepository(Application pApplication){
        AnnouncementDatabase database = AnnouncementDatabase.getInstance(pApplication);
        announcementDao = database.announcementDao();
    }

    public void insert(Announcement pAnnouncement) {
        new InsertAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    public void update(Announcement pAnnouncement) {
        new UpdateAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    public void delete(Announcement pAnnouncement) {
        new DeleteAnnouncementAsyncTask(announcementDao).execute(pAnnouncement);
    }

    public void deleteAllNotes() {
        new DeleteAllAnnouncementAsyncTask(announcementDao).execute();
    }

    public void deleteOlderAnnouncements(Date pDeleteBefore){
        new DeleteOlderAnnouncementAsyncTask(announcementDao).execute(pDeleteBefore);
    }

    //@NonNull
    public LiveData<List<Announcement>> getAllAnnouncementsForTopic(String pTopic){

        return announcementDao.getAnnouncementsForTopic(pTopic);
    }


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
