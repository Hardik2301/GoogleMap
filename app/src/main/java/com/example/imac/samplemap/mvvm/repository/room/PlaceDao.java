package com.example.imac.samplemap.mvvm.repository.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.imac.samplemap.model.Place;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by imac on 11/17/17.
 */

@Dao
public interface PlaceDao {

    @Query("Select * from Place")
    LiveData<List<Place>> getPlacesList();

    @Insert(onConflict = REPLACE)
    void addPlace();
}
