package com.ratemypub.PubApp.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
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

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ratemypub.PubApp.NewPubActivity;
import com.ratemypub.PubApp.databinding.FragmentMapBinding;
import com.ratemypub.PubApp.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final String TAG = null;
    private FragmentMapBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://pubapp-64a3b-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("coordinates");
    private Marker currentLocationMarker;
    private LatLng currentLocation = new LatLng(0,0);
    private ArrayList<Marker> markerList;
    private LocationManager locationManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // check location permissions and get current location
        requestLocationPermissions();
        getLocation();

        // list to store added markers
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
        // get map settings and enable zoom controls
        UiSettings settings = googleMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        // check location permissions and enable current location
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // setup database listener and update pub markers
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

        // move camera to centre on dundee
        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(56.462002, -2.970700), 12.0f));

        // setup long click listener
        googleMap.setOnMapLongClickListener(this::onMapLongClick);
    }

    public void onMapLongClick(LatLng latLng) {
        // check if logged in and if logged in launch dialog to create pub
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle("Add a pub")
                    .setMessage(String.format("Do you want to add a pub at %s, %s", latLng.latitude, latLng.longitude))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(getActivity(), NewPubActivity.class);
                            intent.putExtra("latLng", latLng);
                            startActivity(intent);
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            Toast.makeText(requireActivity(), "You must be logged in to access this feature.", Toast.LENGTH_SHORT).show();
        }
    }

    // request location permissions
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
    }
}