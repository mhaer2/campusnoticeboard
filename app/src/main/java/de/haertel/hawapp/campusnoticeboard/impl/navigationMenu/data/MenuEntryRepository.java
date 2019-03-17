package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/**
 * Repository, das als Mediator fungiert. So kann das View Model auf ein Repository zugreifen,
 * und das Repository leitet die Anfrage an verschiedene Datenquellen weiter.
 */
public class MenuEntryRepository {
    private MenuEntryDatabase database;
    private MenuEntryDao menuEntryDao;
    private LiveData<List<MenuEntry>> allMenuEntries;

    /**
     * Konstruktor des Repositories.
     *
     * @param pApplication das Applikation-Objekt
     */
    MenuEntryRepository(Application pApplication) {
        database = MenuEntryDatabase.getInstance(pApplication);
        menuEntryDao = database.menuEntryDao();
        allMenuEntries = menuEntryDao.getAllMenuEntries();

    }

    /**
     * Ruft einen Asynchronen Task auf der den Insert vornimmt.
     *
     * @param pMenuEntry der Menüeintrag
     */
    public void insert(MenuEntry pMenuEntry) {
        new InsertMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }

    /**
     * Ruft einen Asynchronen Task auf der das Update vornimmt.
     *
     * @param pMenuEntry der Menüeintrag
     */
    void update(MenuEntry pMenuEntry) {
        new UpdateMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }

    /**
     * Ruft einen Asynchronen Task auf der das Delete vornimmt.
     *
     * @param pMenuEntry der Menüeintrag
     */
    public void delete(MenuEntry pMenuEntry) {
        new DeleteMenuEntryAsyncTask(menuEntryDao).execute(pMenuEntry);
    }

    /**
     * Ruft einen Asynchronen Task auf der alle Datensätze löscht vornimmt.
     */
    void deleteAllMenuEntries() {
        new DeleteAllMenuEntryAsyncTask(menuEntryDao).execute();
    }

    /**
     * Ruft einen Asynchronen Task auf der alle Datensätze liefert.
     *
     * @return alle Einträge als Live Daten.
     */
    LiveData<List<MenuEntry>> getAllMenuEntries() {
        return allMenuEntries;
    }

    /**
     * Der Asynchrone Task, der den Eintrag einpflegt.
     */
    private static class InsertMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private InsertMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao) {
            menuEntryDao = pMenuEntryDao;
        }

        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.insert(pMenuEntries[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der den Eintrag updated.
     */
    private static class UpdateMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private UpdateMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao) {
            menuEntryDao = pMenuEntryDao;
        }

        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.update(pMenuEntries[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der den Eintag löscht.
     */
    private static class DeleteMenuEntryAsyncTask extends AsyncTask<MenuEntry, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private DeleteMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao) {
            menuEntryDao = pMenuEntryDao;
        }

        @Override
        protected Void doInBackground(MenuEntry... pMenuEntries) {
            menuEntryDao.delete(pMenuEntries[0]);
            return null;
        }
    }

    /**
     * Der Asynchrone Task, der alle Daten löscht.
     */
    private static class DeleteAllMenuEntryAsyncTask extends AsyncTask<Void, Void, Void> {
        private MenuEntryDao menuEntryDao;

        private DeleteAllMenuEntryAsyncTask(MenuEntryDao pMenuEntryDao) {
            menuEntryDao = pMenuEntryDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            menuEntryDao.deleteAllMenuEntries();
            return null;
        }
    }


}
