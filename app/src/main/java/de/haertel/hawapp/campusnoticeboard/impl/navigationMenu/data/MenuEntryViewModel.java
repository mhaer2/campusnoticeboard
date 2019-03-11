package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class MenuEntryViewModel extends AndroidViewModel {

    private MenuEntryRepository repository;
    private LiveData<List<MenuEntry>> allMenuEntries;

    public MenuEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new MenuEntryRepository(application);
        allMenuEntries = repository.getAllMenuEntries();
    }

    public void insert(MenuEntry pMenuEntry){
        repository.insert(pMenuEntry);
    }
    public void update(MenuEntry pMenuEntry){
        repository.update(pMenuEntry);
    }
    public void delete(MenuEntry pMenuEntry){
        repository.delete(pMenuEntry);
    }
    public void deleteAllMenuEntries(){
        repository.deleteAllMenuEntries();
    }
    public LiveData<List<MenuEntry>> getAllMenuEntries(){
        return allMenuEntries;
    }
//    public LiveData<List<MenuEntry>> getAllRootMenuEntries(){
//        return repository.getAllRootMenuEntries();
//    }
//    public LiveData<List<MenuEntry>> getAllNonRootParentEntries(String pTopic){
//        return repository.getAllNonRootParentEntries(pTopic);
//    }
//    public LiveData<List<MenuEntry>> getAllChildMenuEntries(){
//        return repository.getAllChildMenuEntries();
//    }
}
