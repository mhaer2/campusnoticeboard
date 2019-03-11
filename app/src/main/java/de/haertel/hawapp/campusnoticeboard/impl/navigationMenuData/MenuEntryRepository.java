package de.haertel.hawapp.campusnoticeboard.impl.navigationMenuData;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MenuEntryRepository {
    private MenuEntryDatabase database;
    private MenuEntryDao menuEntryDao;
    private LiveData<List<MenuEntry>> allMenuEntries;
//    private LiveData<List<MenuEntry>> allRootMenuEntries;
//    private LiveData<List<MenuEntry>> allChildMenuEntries;

    public MenuEntryRepository(Application pApplication){
        database = MenuEntryDatabase.getInstance(pApplication);
        menuEntryDao = database.menuEntryDao();
        allMenuEntries = menuEntryDao.getAllMenuEntries();
//        allRootMenuEntries = menuEntryDao.getAllRootMenuEntries();
//        allChildMenuEntries = menuEntryDao.getAllChildMenuEntries();

    }

    public void insert(MenuEntry pMenuEntry){
        new InsertMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }
    public void update(MenuEntry pMenuEntry){
        new UpdateMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }
    public void delete(MenuEntry pMenuEntry){
        new DeleteMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }
    public void deleteAllMenuEntries(){
        new DeleteAllMenuEntryAsyncTask(menuEntryDao).execute();
    }
    public LiveData<List<MenuEntry>> getAllMenuEntries(){
        //new GetAllMenuEntriesAsyncTask(menuEntryDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //return menuEntryDao.getAllMenuEntries();
        return allMenuEntries;
    }
//    public LiveData<List<MenuEntry>> getAllRootMenuEntries(){
//        return menuEntryDao.getAllRootMenuEntries();
//    }
//    public LiveData<List<MenuEntry>> getAllNonRootParentEntries(String pTopic){
//        return menuEntryDao.getAllNonRootParentEntries(pTopic);
//    }
//    public LiveData<List<MenuEntry>> getAllChildMenuEntries(){
//        return menuEntryDao.getAllChildMenuEntries();
//    }


    private static class InsertMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private InsertMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao){
            menuEntryDao = pMenuEntryDao;
        }
        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.insert(pMenuEntries[0]);
            return null;
        }
    }
    private static class UpdateMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private UpdateMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao){
            menuEntryDao = pMenuEntryDao;
        }
        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.update(pMenuEntries[0]);
            return null;
        }
    }
    private static class DeleteMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private DeleteMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao){
            menuEntryDao = pMenuEntryDao;
        }
        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.delete(pMenuEntries[0]);
            return null;
        }
    }
    private static class DeleteAllMenuEntryAsyncTask extends AsyncTask<Void, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private DeleteAllMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao){
            menuEntryDao = pMenuEntryDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            menuEntryDao.deleteAllMenuEntries();
            return null;
        }
    }
//    private static class GetAllMenuEntriesAsyncTask extends AsyncTask<Void, Void, List<MenuEntry>> {
//        private MenuEntryDao menuEntryDao;
//
//        private GetAllMenuEntriesAsyncTask(MenuEntryDao pMenuEntryDao){
//            menuEntryDao = pMenuEntryDao;
//        }
//        @Override
//        protected List<MenuEntry> doInBackground(Void... voids) {
//
//            return menuEntryDao.getAllMenuEntries();
//        }
//    }
//    private static class GetAllRootMenuEntriesAsyncTask extends AsyncTask<Void, Void, List<MenuEntry>> {
//        private MenuEntryDao menuEntryDao;
//
//        private GetAllRootMenuEntriesAsyncTask(MenuEntryDao pMenuEntryDao){
//            menuEntryDao = pMenuEntryDao;
//        }
//        @Override
//        protected List<MenuEntry> doInBackground(Void... voids) {
//
//            return menuEntryDao.getAllRootMenuEntries();
//        }
//    }
//    private static class GetAllNonRootMenuEntriesAsyncTask extends AsyncTask<String, Void, List<MenuEntry>> {
//        private MenuEntryDao menuEntryDao;
//
//        private GetAllNonRootMenuEntriesAsyncTask(MenuEntryDao pMenuEntryDao){
//            menuEntryDao = pMenuEntryDao;
//        }
//        @Override
//        protected List<MenuEntry> doInBackground(String... strings) {
//
//            return menuEntryDao.getAllNonRootParentEntries(strings[0]);
//        }
//    }
//    private static class GetAllChildMenuEntriesAsyncTask extends AsyncTask<Void, Void, List<MenuEntry>> {
//        private MenuEntryDao menuEntryDao;
//
//        private GetAllChildMenuEntriesAsyncTask(MenuEntryDao pMenuEntryDao){
//            menuEntryDao = pMenuEntryDao;
//        }
//        @Override
//        protected List<MenuEntry> doInBackground(Void... voids) {
//
//            return menuEntryDao.getAllChildMenuEntries();
//        }
//    }

}
