package com.example.imac.samplemap.mvvm.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.imac.samplemap.model.Place;
import com.example.imac.samplemap.mvvm.repository.PlacesRepository;

import java.util.List;

/**
 * Created by imac on 11/17/17.
 */

public class PlacesViewModel extends AndroidViewModel {

    private LiveData<List<Place>> mPlacesObservable;

    public PlacesViewModel(@NonNull Application application) {
        super(application);

        mPlacesObservable = PlacesRepository.getInstance().getPlacesList(application.getBaseContext());
    }

    public LiveData<List<Place>> getPlacesObservable() {
        return mPlacesObservable;
    }

}
