package com.example.imac.samplemap.data;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Custom Request Class for JSON
 */
public class CustomRequestString extends Request<String> {
	
    private Map<String,String> params;        // the request params
    private Response.Listener<String> listener; // the response listener
    
    public CustomRequestString(int requestMethod, String url, Map<String,String> params,
                               Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
    	
        super(requestMethod, url, errorListener); // Call parent constructor
        this.params = params;
        this.listener = responseListener;
    }
    
    // We HAVE TO implement this function
    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response); // Call response listener
    }
    
    // Proper parameter behavior
    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    public String getUrl() {
        return super.getUrl();
    }

    // Same as JsonObjectRequest#parseNetworkResponse
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(jsonString,null);
          //  HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception je) {
            return Response.error(new ParseError(je));
        }
    }

 
}