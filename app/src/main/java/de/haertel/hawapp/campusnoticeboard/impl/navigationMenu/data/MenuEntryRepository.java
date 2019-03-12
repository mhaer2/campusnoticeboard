package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

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
        return allMenuEntries;
    }

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


}
