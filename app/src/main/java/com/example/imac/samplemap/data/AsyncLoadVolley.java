package com.example.imac.samplemap.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AsyncLoadVolley {
	
	private static final String TAG = "AsyncLoadVolley Respnse";
	
	private OnAsyncTaskListener asyncTaskListener;
	
	private Map<String,String> params;
	
	private RequestQueue queue;
	
	private String url;
	
	private Context context;
	
	private ConnectionDetector connectionDetector;
		
	public AsyncLoadVolley(Context context, String filename) {
		queue = Volley.newRequestQueue(context);
		url = filename;
		this.context = context;
		connectionDetector = new ConnectionDetector(context);
		init();
	}
	
	private void init() {
		params = new HashMap<>();
	}
	
	public void setOnAsyncTaskListener(OnAsyncTaskListener listener) {
        this.asyncTaskListener=listener;            
    }
    
    public void setParameters(Map<String, String> map) {
    	params=map;
	}
    
    public void beginTask(String param) {
    	asyncTaskListener.onTaskBegin();
    	CustomRequestString request = new CustomRequestString(Request.Method.GET, url+param, params, listener, errorListener);
		queue.add(request);
		Log.e("beginTask: Url is - ", request.getUrl());
	}
    
    Response.Listener<String> listener = new Response.Listener<String>() {
		
		@Override
		public void onResponse(String response) 
		{	
			Log.e(TAG, response);
			asyncTaskListener.onTaskComplete(true, response);
		}	
	};
	
	Response.ErrorListener errorListener =  new Response.ErrorListener() {
		
		@Override
		public void onErrorResponse(VolleyError error) {
			asyncTaskListener.onTaskComplete(false, error.getMessage());
		}
	};
}