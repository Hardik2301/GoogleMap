package com.example.imac.samplemap;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

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
    Boolean Is_MAP_Moveable = false;

    Projection projection;
    List<LatLng> mlist;
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
        mlist=new ArrayList<LatLng>();
        mPlacelist=new ArrayList<Place>();
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
                    Log.e("All Latlong on list:-", mlist.toString());
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

        asyncLoadVolley = new AsyncLoadVolley(getApplicationContext(), "http://jsonplaceholder.typicode.com/comments");
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
        mMap.addPolygon(rectOptions);
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

        asyncLoadVolley.beginTask();

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

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

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

    OnAsyncTaskListener AsyncTaskListener=new OnAsyncTaskListener() {
        @Override
        public void onTaskBegin() {
            Log.e("onRemoteSource ", "Api start calling");
        }

        @Override
        public void onTaskComplete(boolean success, String response) {
            Log.e("onRemoteSource ", "Api finished calling");
            if(success) {
                AsyncResponse mresponse = new AsyncResponse(response);
                if(mresponse.ifSuccess()){
                    mPlacelist=mresponse.getPlacelist();
                }
            }else{
                Snackbar.make(btn_draw,"Error in fatching data",Snackbar.LENGTH_LONG);
            }
        }
    };
}
