package com.example.imac.samplemap.mvvm.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.imac.samplemap.PolyUtil;
import com.example.imac.samplemap.R;
import com.example.imac.samplemap.model.Place;
import com.example.imac.samplemap.mvvm.viewModel.PlacesViewModel;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imac on 11/17/17.
 */

public class FragmentMap extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
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
    Boolean Is_MAP_Moveable = true;
    Boolean Is_Draggable = false;

    Polygon mPolygon;
    Projection projection;
    List<LatLng> mlist;
    List<LatLng> mBorderLatLng;
    double latitude, longitude;

    List<Place> mPlacelist;

    public FragmentMap() {
    }

    public static FragmentMap newInstance() {
        FragmentMap fragment = new FragmentMap();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void ObserveData(PlacesViewModel placesViewModel) {

        placesViewModel.getPlacesObservable().observe(this, new Observer<List<Place>>() {
            @Override
            public void onChanged(@Nullable List<Place> places) {
                if (places != null) {
                    mPlacelist.addAll(places);
                    addMarkerOnMap(mPlacelist);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        fram_map = (FrameLayout) view.findViewById(R.id.fram_map);
        btn_draw = (FloatingActionButton) view.findViewById(R.id.btn_draw);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        mlist = new ArrayList<LatLng>();
        mPlacelist = new ArrayList<Place>();
        mBorderLatLng = new ArrayList<LatLng>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        btn_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Is_Draggable) {
                    mMap.clear();
                    addMarkerOnMap(mPlacelist);
                    btn_draw.setImageResource(R.drawable.ic_play_dark);
                    Is_Draggable = false;
                } else {
                    Is_MAP_Moveable = false;
                    btn_draw.setImageResource(R.drawable.ic_close_dark);
                    Is_Draggable = true;
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
                        if (!Is_MAP_Moveable) {
                            mMap.clear();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mlist.add(new LatLng(latitude, longitude));
                        Draw_Polyline();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!Is_MAP_Moveable && mlist.size() > 1) {
                            Draw_Polygon();
                        }
                        break;
                }

                if (!Is_MAP_Moveable) {
                    //Log.e("on Draw complete: ", "Yes");
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public void Draw_Polyline() {
        //Log.e("on Draw complete: ", "draw map called");
        PolylineOptions plineOptions = new PolylineOptions();
        plineOptions.addAll(mlist);
        plineOptions.width(7);
        plineOptions.geodesic(true);
        plineOptions.color(Color.BLACK);
        mMap.addPolyline(plineOptions);
    }

    public void Draw_Polygon() {
        PolygonOptions rectOptions = new PolygonOptions();
        rectOptions.addAll(mlist);
        rectOptions.strokeWidth(7);
        rectOptions.strokeColor(Color.BLACK);
        rectOptions.fillColor(0x7F000000);
        mPolygon = mMap.addPolygon(rectOptions);
        addMarkerOnPolygon(mPlacelist);
        Log.e("Draw_Polygon: List", mlist.toString());
        Is_MAP_Moveable = true;
        mlist.clear();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
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
            if (checkLocationPermission()) {
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

                LatLng topLeft = projection.fromScreenLocation(new Point(0, 0));
                LatLng topRight = projection.fromScreenLocation(new Point(viewportWidth, 0));
                LatLng bottomRight = projection.fromScreenLocation(new Point(viewportWidth, viewportHeight));
                LatLng bottomLeft = projection.fromScreenLocation(new Point(0, viewportHeight));

                mBorderLatLng.add(topLeft);
                mBorderLatLng.add(topRight);
                mBorderLatLng.add(bottomLeft);
                mBorderLatLng.add(bottomRight);

                if (mLastLocation != null && !Is_Draggable) {
                    CallApi();
                }
            }
        });

    }

    private void CallApi() {
        PlacesViewModel placesViewModel = ViewModelProviders.of(this).get(PlacesViewModel.class);
        ObserveData(placesViewModel);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

        try {
            Geocoder gc = new Geocoder(getActivity());
            List<Address> lstAdd = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address ad = lstAdd.get(0);
            String str = ad.getAddressLine(0);
            Log.e("Address from location", str);
            Log.e("Locality", ad.getLocality());
            Log.e("Pincode", ad.getPostalCode());
            Log.e("Country Name", ad.getCountryName());
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        } finally {
            Log.e("Finally", "Finally called");
        }
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

                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
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
        if (ContextCompat.checkSelfPermission(getActivity(),
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

    private void addMarkerOnMap(List<Place> mPlacelist) {
        mMap.clear();
        for (int i = 0; i < mPlacelist.size(); i++) {
            Place place = mPlacelist.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitute()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(place.getName());
            mMap.addMarker(markerOptions);
        }
    }

    private void addMarkerOnPolygon(List<Place> mPlacelist) {
        for (int i = 0; i < mPlacelist.size(); i++) {
            Place place = mPlacelist.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitute()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(place.getName());
            if (PolyUtil.pointInPolygon(latLng, mPolygon)) {
                mMap.addMarker(markerOptions);
            }
        }
    }
}
