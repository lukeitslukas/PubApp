package com.ratemypub.PubApp.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ratemypub.PubApp.NewPubActivity;
import com.ratemypub.PubApp.databinding.FragmentMapBinding;
import com.ratemypub.PubApp.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final String TAG = null;
    private FragmentMapBinding binding;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://pubapp-64a3b-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("coordinates");
    private Marker currentLocationMarker;
    private LatLng currentLocation = new LatLng(0,0);
    private ArrayList<Marker> markerList;
    private LocationManager locationManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel mapViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        requestLocationPermissions();

        getLocation();

        markerList = new ArrayList<>();

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.map_view, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        return root;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager
                            .GPS_PROVIDER, 5000, 1, this);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager
                            .NETWORK_PROVIDER, 5000, 1, this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        UiSettings settings = googleMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (markerList.isEmpty()) {
                    for (DataSnapshot s : dataSnapshot.getChildren()){
                        Coordinates coordinates = s.getValue(Coordinates.class);
                        assert coordinates != null;
                        LatLng location = new LatLng(coordinates.lat, coordinates.lng);
                        markerList.add(googleMap.addMarker(new MarkerOptions()
                                .position(location)
                                .icon(BitmapDescriptorFactory.defaultMarker((float) (coordinates.rating * 24)))
                                .title(coordinates.placeName)));
                    }
                } else {
                    int counter = 0;
                    for (DataSnapshot s : dataSnapshot.getChildren()){
                        Coordinates coordinates = s.getValue(Coordinates.class);
                        assert coordinates != null;
                        LatLng location = new LatLng(coordinates.lat,coordinates.lng);
                        try {
                            markerList.get(counter)
                                    .setPosition(location);
                            markerList.get(counter)
                                    .setIcon(BitmapDescriptorFactory.defaultMarker((float) (coordinates.rating * 24)));
                            markerList.get(counter)
                                    .setTitle(coordinates.placeName);
                        } catch (Exception e) {
                            markerList.add(googleMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .icon(BitmapDescriptorFactory.defaultMarker((float) (coordinates.rating * 24)))
                                    .title(coordinates.placeName)));
                        }
                        counter += 1;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(56.462002, -2.970700), 12.0f));

        currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title("Current Location"));

        googleMap.setOnMapLongClickListener(this::onMapLongClick);
    }

    public void onMapLongClick(LatLng latLng) {
        new AlertDialog.Builder(requireActivity())
                .setTitle("Add a pub")
                .setMessage(String.format("Do you want to add a pub at %s, %s", latLng.latitude, latLng.longitude))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO add marker to db
                        Intent intent = new Intent(getActivity(), NewPubActivity.class);
                        intent.putExtra("latLng", latLng);
                        startActivity(intent);
                        Toast.makeText(requireActivity(), "Yaay", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private boolean requestLocationPermissions() {
        AtomicBoolean permission = new AtomicBoolean(false);
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                permission.set(true);
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                permission.set(true);
                            } else {
                                permission.set(false);
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        return permission.get();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocationMarker.setPosition(currentLocation);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}