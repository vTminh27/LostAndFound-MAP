package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.lostandfound.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.zip.Inflater;

public class MapsActivity extends FragmentActivity  implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync( this);
    }

    @Override
    public void onMapReady( GoogleMap googleMap) {
        mMap = googleMap;

        // Add new markers for each item and move the camera
        DatabaseHelper db = new DatabaseHelper(this);
        List<Item> itemList = db.fetchAllItem();
        for (Item item:itemList) {
            Double lat = Double.parseDouble(item.getLocationLat());
            Double lng = Double.parseDouble(item.getLocationLng());
            LatLng location = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(location).title(item.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }

    }
}