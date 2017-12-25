package com.example.imac.samplemap.mvvm.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.example.imac.samplemap.R;
import com.example.imac.samplemap.data.AsyncLoadVolley;
import com.example.imac.samplemap.data.AsyncResponse;
import com.example.imac.samplemap.data.OnAsyncTaskListener;
import com.example.imac.samplemap.model.Place;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imac on 11/17/17.
 */

public class PlacesRepository {

    private static PlacesRepository mInstance;

    public static PlacesRepository getInstance() {
        if (mInstance == null) {
            mInstance = new PlacesRepository();
        }
        return mInstance;
    }

    public LiveData<List<Place>> getPlacesList(Context context) {
        final MutableLiveData<List<Place>> data = new MutableLiveData<>();

        AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, "https://maps.googleapis.com/maps/api/place/nearbysearch/json");
        asyncLoadVolley.setOnAsyncTaskListener(new OnAsyncTaskListener() {
            @Override
            public void onTaskBegin() {

            }

            @Override
            public void onTaskComplete(boolean success, String response) {
                if (success) {
                    AsyncResponse mResponse = new AsyncResponse(response);
                    if (mResponse.ifSuccess()) {
                        data.setValue(mResponse.getPlacelist());
                        Log.e("Total Locations: ", mResponse.getPlacelist() + "");
                    }
                }
            }
        });

        Map<String,String> param=new HashMap<String, String>();
        param.put("location","72.5577,23.0261");
        param.put("radius","5000");
        param.put("types","atm,restaurant,bank");
        param.put("sensor","true");
        param.put("key","AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8");

        asyncLoadVolley.setParameters(param);
        asyncLoadVolley.beginTask("?location=23.0260983,72.5576983"+
                "&radius=50000&types=atm,restaurant,bank"+
                "&sensor=true&key=AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8");

        return data;
    }
}
