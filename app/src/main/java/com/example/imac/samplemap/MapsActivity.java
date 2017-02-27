package com.example.imac.samplemap;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.imac.samplemap.data.AsyncLoadVolley;
import com.example.imac.samplemap.data.AsyncResponse;
import com.example.imac.samplemap.data.OnAsyncTaskListener;
import com.example.imac.samplemap.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    FrameLayout fram_map;
    FloatingActionButton btn_draw;
    ProgressBar progressBar;
    Boolean Is_MAP_Moveable = false;

    Projection projection;
    List<LatLng> mlist;
    List<LatLng> mBorderLatLng;
    double latitude,longitude;

    private AsyncLoadVolley asyncLoadVolley;
    List<Place> mPlacelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        fram_map = (FrameLayout) findViewById(R.id.fram_map);
        btn_draw = (FloatingActionButton) findViewById(R.id.btn_draw);
        progressBar=(ProgressBar)findViewById(R.id.progressBar2);
        mlist=new ArrayList<LatLng>();
        mPlacelist=new ArrayList<Place>();
        mBorderLatLng = new ArrayList<LatLng>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Is_MAP_Moveable) {
                    mMap.clear();
                    addMarkerOnMap(mPlacelist);
                    btn_draw.setImageResource(R.drawable.ic_play_dark);
                    Is_MAP_Moveable = false;
                } else {
                    btn_draw.setImageResource(R.drawable.ic_close_dark);
                    Is_MAP_Moveable = true;
                }
            }
        });

        fram_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                int x_co = Math.round(x);
                int y_co = Math.round(y);

                projection = mMap.getProjection();
                Point x_y_points = new Point(x_co, y_co);

                LatLng latLng = mMap.getProjection().fromScreenLocation(x_y_points);
                latitude = latLng.latitude;

                longitude = latLng.longitude;

                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN:
                        mlist.clear();
                        // finger touches the screen
                        mlist.add(new LatLng(latitude, longitude));

                    case MotionEvent.ACTION_MOVE:
                        if (Is_MAP_Moveable) {
                            mMap.clear();
                        }
                        // finger moves on the screen
                        mlist.add(new LatLng(latitude, longitude));
                        Draw_Polyline();
                        break;
                    case MotionEvent.ACTION_UP:
                        // finger leaves the screen
                        if(Is_MAP_Moveable) {
                            Draw_Polygon();
                        }
                        break;
                }

                if (Is_MAP_Moveable) {
                    Log.e("on Draw complete: ", "Yes");
                    return true;
                } else {
                    return false;
                }
            }
        });

        asyncLoadVolley = new AsyncLoadVolley(getApplicationContext(), "https://maps.googleapis.com/maps/api/place/nearbysearch/json");
        asyncLoadVolley.setOnAsyncTaskListener(AsyncTaskListener);

    }

    public void Draw_Polyline() {
        Log.e("on Draw complete: ", "draw map called");
        PolylineOptions plineOptions=new PolylineOptions();
        plineOptions.addAll(mlist);
        plineOptions.geodesic(true);
        plineOptions.color(Color.BLACK);
        mMap.addPolyline(plineOptions);
    }

    public void Draw_Polygon()
    {
        PolygonOptions rectOptions = new PolygonOptions();
        rectOptions.addAll(mlist);
        rectOptions.strokeColor(Color.BLACK);
        Polygon polygon=mMap.addPolygon(rectOptions);
        addMarkerOnPolygon(mPlacelist);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mBorderLatLng.clear();
                projection = mMap.getProjection();
                int viewportWidth = fram_map.getWidth();
                int viewportHeight = fram_map.getHeight();

                LatLng topLeft     = projection.fromScreenLocation(new Point(0, 0));
                LatLng topRight    = projection.fromScreenLocation(new Point(viewportWidth, 0));
                LatLng bottomRight = projection.fromScreenLocation(new Point(viewportWidth, viewportHeight));
                LatLng bottomLeft  = projection.fromScreenLocation(new Point(0, viewportHeight));

                mBorderLatLng.add(topLeft);
                mBorderLatLng.add(topRight);
                mBorderLatLng.add(bottomLeft);
                mBorderLatLng.add(bottomRight);

                if(mLastLocation != null) {
                    CallApi();
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.e("onLocationChanged: ", "On location called");
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public void CallApi(){
        Map<String,String> param=new HashMap<String, String>();
        param.put("location",mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        param.put("radius","5000");
        param.put("types","atm,restaurant,bank");
        param.put("sensor","true");
        //param.put("key",getResources().getString(R.string.google_maps_key));
        param.put("key","AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8");

        asyncLoadVolley.setParameters(param);
        asyncLoadVolley.beginTask("?location="+param.get("location")+
                "&radius=50000&types=atm,restaurant,bank"+
                "&sensor=true&key=AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8");
    }
    OnAsyncTaskListener AsyncTaskListener=new OnAsyncTaskListener() {
        @Override
        public void onTaskBegin() {
            Log.e("onRemoteSource ", "Api start calling");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTaskComplete(boolean success, String response) {
            Log.e("onRemoteSource ", "Api finished calling");
            if(progressBar.getVisibility() == View.VISIBLE){
                progressBar.setVisibility(View.GONE);
            }

            if(success) {
                AsyncResponse mresponse = new AsyncResponse(response);
                if(mresponse.ifSuccess()){
                    mPlacelist=mresponse.getPlacelist();
                    Log.e("Total Locations: ", mPlacelist.size()+"");
                    addMarkerOnMap(mPlacelist);
                }
            }else{
                Snackbar.make(btn_draw,"Error in fatching data",Snackbar.LENGTH_LONG);
            }
        }
    };

    private void addMarkerOnMap(List<Place> mPlacelist) {
        mMap.clear();
        for(int i=0 ;i < mPlacelist.size();i++){
            Place place=mPlacelist.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitute()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(place.getName());
            if(PolyUtil.FindLatLngFromList(latLng,mBorderLatLng)) {
                mMap.addMarker(markerOptions);
            }
        }
    }

    private void addMarkerOnPolygon(List<Place> mPlacelist) {
        for(int i=0 ;i < mPlacelist.size();i++){
            Place place=mPlacelist.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitute()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(place.getName());
            if(PolyUtil.isPointInPolygon(latLng,mlist)) {
                mMap.addMarker(markerOptions);
            }
        }
    }
}
