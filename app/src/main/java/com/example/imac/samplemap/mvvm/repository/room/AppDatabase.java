package com.example.imac.samplemap.mvvm.repository.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.imac.samplemap.model.Place;

/**
 * Created by imac on 11/18/17.
 */

@Database(entities = {Place.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase mAppDatabase;

    public static AppDatabase getAppDatabase(Context context) {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "myplaces_db").build();
        }
        return mAppDatabase;
    }

    public abstract PlaceDao placeModel();
}
