package com.example.imac.samplemap.data;

import android.util.Log;

import com.example.imac.samplemap.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsyncResponse {
	
	private static final String TAG = "AsyncResponse";
	private JSONObject jsonObject;
	private JSONArray jsonArray;
	
	private String message = "";
	private boolean success = false;
	private String response;
	
	public AsyncResponse(String response) {
		this.response = response;
	}
	
	public boolean ifSuccess() {
		try {
			if(response!=null) {
				jsonObject = new JSONObject(response);
				if(jsonObject.getString("status").equalsIgnoreCase("OK")) {
					success = true;
				}
			}
            return success;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data => " + e.toString());            
            return success;
        }
	}
	
	public List<Place> getPlacelist() {

		List<Place>  arrayList= new ArrayList<>();

		try {

			jsonArray = jsonObject.getJSONArray("results");

			try {
				Log.e("JSON Array : ", jsonArray.toString());

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject c = jsonArray.getJSONObject(i);

					JSONObject geometry=c.getJSONObject("geometry");
					JSONObject location=geometry.getJSONObject("location");

					String lat=location.getString("lat");
					String lng=location.getString("lng");

					String id = c.getString("id");
					String name = c.getString("name");
					String address = c.getString("vicinity");

					Place task = new Place(id,name,lat,lng,address);
					arrayList.add(task);
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing data : " + e.toString());
			}
		}catch (JSONException e){
			e.printStackTrace();
		}
        return arrayList;
	}
	

	
}
