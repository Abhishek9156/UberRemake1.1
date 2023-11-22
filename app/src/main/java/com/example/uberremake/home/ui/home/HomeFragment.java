package com.example.uberremake.home.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.uberremake.Common;
import com.example.uberremake.R;
import com.example.uberremake.databinding.FragmentHomeBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements OnMapReadyCallback{
    private GoogleMap mMap;
    private FragmentHomeBinding binding;
    HomeViewModel homeViewModel;
    SupportMapFragment mapFragment;
//    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

//    //online system
    DatabaseReference onlineRef, currentRef, deriverLocationRef;
    GeoFire geoFire;
    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() )
                currentRef.onDisconnect().removeValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();

        return root;
    }

    private void init() {
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        deriverLocationRef=FirebaseDatabase.getInstance().getReference(Common.DRIVER_LOCATION_REFERENCE);
        currentRef = deriverLocationRef
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                geoFire = new GeoFire(deriverLocationRef);
registerOnlineSystem();


        //        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Snackbar.make(getView(), getString(R.string.permission_required), Snackbar.LENGTH_LONG).show();
//            return;
//        }


        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));


                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                                locationResult.getLastLocation().getLongitude()),
                                        (key, error) -> {
                                            if (error != null)
                                                Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                                                        .show();
                                            else
                                                Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_LONG)
                                                        .show();
                                        });





//                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
//                            List<Address> addressList;
//                            try {
//                                addressList = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),
//                                        locationResult.getLastLocation().getLongitude(), 1);
//                                String cityName = addressList.get(0).getLocality();
//
//                                deriverLocationRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_LOCATION_REFERENCE)
//                                        .child(cityName);
//                                currentRef = deriverLocationRef
//                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                geoFire = new GeoFire(deriverLocationRef);
//
//                                //Update location
//                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
//                                        new GeoLocation(locationResult.getLastLocation().getLatitude(),
//                                                locationResult.getLastLocation().getLongitude()),
//                                        (key, error) -> {
//                                            if (error != null)
//                                                Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
//                                                        .show();
//                                            else
//                                                Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_LONG)
//                                                        .show();
//                                        });
//
//                                registerOnlineSystem();
//
//
//
//                            } catch (IOException e) {
//                                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
//                            }





            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(), getString(R.string.permission_required), Snackbar.LENGTH_LONG).show();

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());



    }

    @Override
    public void onDestroyView() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        super.onDestroyView();
        binding = null;

    }


    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        //check permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(getView(), getString(R.string.permission_required), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnSuccessListener(location -> {
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                                    });
                            return true;
                        });

                        //set layout button
                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 50);


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(), "Permission " + permissionDeniedResponse.getPermissionName() + "" + "was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_map_style));
            if (!success)
                Toast.makeText(getContext(), "Style Parsing error", Toast.LENGTH_SHORT).show();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }


    }
}