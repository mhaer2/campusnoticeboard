package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Das ViewModel für die Menüeinträge.
 * Über diese ViewModel können alle festgelegten Datenbankzugriffe ausgeführt werden.
 */

public class MenuEntryViewModel extends AndroidViewModel {

    private MenuEntryRepository repository;
    private LiveData<List<MenuEntry>> allMenuEntries;

    public MenuEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new MenuEntryRepository(application);
        allMenuEntries = repository.getAllMenuEntries();
    }

    /**
     * Ruft die Methode im Repository auf, die den Insert vornimmt.
     *
     * @param pMenuEntry der Menüeintrag
     */
    public void insert(MenuEntry pMenuEntry){
        repository.insert(pMenuEntry);
    }
    /**
     * Ruft die Methode im Repository auf, die das Update vornimmt.
     * @param pMenuEntry der Menüeintrag
     */
    public void update(MenuEntry pMenuEntry){
        repository.update(pMenuEntry);
    }
    /**
     * Ruft die Methode im Repository auf, die das Delete vornimmt.
     * @param pMenuEntry der Menüeintrag
     */
    public void delete(MenuEntry pMenuEntry){
        repository.delete(pMenuEntry);
    }
    /**
     * Ruft die Methode im Repository auf, die das Löschen aller Einträge vornimmt.
     */
    public void deleteAllMenuEntries(){
        repository.deleteAllMenuEntries();
    }

    /**
     * Ruft die Methode im Repository auf, die alle Einträge bereitstellt.
     * @return eine Liste mit Menüeinträgen als LiveDate.
     */
    public LiveData<List<MenuEntry>> getAllMenuEntries(){
        return allMenuEntries;
    }
}
