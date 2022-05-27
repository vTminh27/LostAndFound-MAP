package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class newAdvert extends AppCompatActivity {


    private static final String TAG = "New Advert ";
    RadioGroup lostFoundRadioGroup;
    RadioButton selectedRadioButton;
    EditText newNameEditText;
    EditText newPhoneEditText;
    EditText newDescriptionEditText;
    EditText newDateEditText;
    Button newGetCurrentLocationButton;
    Button newSaveButton;
    String placeName, placeLat, placeLng;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_advert);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        lostFoundRadioGroup = findViewById(R.id.lostFoundRadioGroup);
        newNameEditText = findViewById(R.id.newNameEditText);
        newPhoneEditText = findViewById(R.id.newPhoneEditText);
        newDescriptionEditText = findViewById(R.id.newDescriptionEditText);
        newDateEditText = findViewById(R.id.newDateEditText);
        newGetCurrentLocationButton = findViewById(R.id.newGetCurrentLocationButton);
        newSaveButton = findViewById(R.id.newSaveButton);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(newAdvert.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        String API_KEY = BuildConfig.MAPS_API_KEY;
        // Initialize the SDK
        Places.initialize(getApplicationContext(), API_KEY);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                placeName = place.getName();
                placeLat = Double.toString(place.getLatLng().latitude);
                placeLng = Double.toString(place.getLatLng().longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                // Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        newGetCurrentLocationButton.setOnClickListener(view -> {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            // Call findCurrentPlace and handle the response (first check that the user has granted permission).
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                placeResponse.addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FindCurrentPlaceResponse response = task.getResult();
                        placeName = response.getPlaceLikelihoods().get(0).getPlace().getName();
                        placeLat = Double.toString(response.getPlaceLikelihoods().get(0).getPlace().getLatLng().latitude);
                        placeLng = Double.toString(response.getPlaceLikelihoods().get(0).getPlace().getLatLng().longitude);
                        autocompleteFragment.setText(placeName);

                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                });
            } else {
                // A local method to request required permissions;
                // See https://developer.android.com/training/permissions/requesting
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(newAdvert.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            }
        });



        DatabaseHelper db = new DatabaseHelper(this);

        newSaveButton.setOnClickListener(view -> {
            int selectedId = lostFoundRadioGroup.getCheckedRadioButtonId();
            String name = newNameEditText.getText().toString();
            String phone = newPhoneEditText.getText().toString();
            String description = newDescriptionEditText.getText().toString();
            String date = newDateEditText.getText().toString();


            if (name.equals("") || phone.equals("") || description.equals("") || date.equals("") || placeName == null || placeLat == null || placeLng == null) { // check for empty fields
                Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
            } else if (selectedId == -1) { // check if radio button selected
                Toast.makeText(this, "Please select post type", Toast.LENGTH_SHORT).show();
            } else {
                selectedRadioButton = findViewById(selectedId);
                name = selectedRadioButton.getText().toString() + " " + name;

                Item item = new Item(name, phone, description, date, placeName, placeLat, placeLng);
                db.insertItem(item);
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(newAdvert.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}