package com.ratemypub.PubApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ratemypub.PubApp.ui.map.Coordinates;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class NewPubActivity extends AppCompatActivity {

    private static final String TAG = null;
    // setup rounding formats and database
    private final DecimalFormat latLngFormat = new DecimalFormat("#.#####");
    private final DecimalFormat reviewFormat = new DecimalFormat("#.#");
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://pubapp-64a3b-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("coordinates");
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pub);

        // set rounding mode to round if 0.5 or above
        latLngFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        reviewFormat.setRoundingMode(RoundingMode.HALF_EVEN);

        // retrieve location from map fragment
        Bundle extras = getIntent().getExtras();
        latLng = (LatLng) extras.get("latLng");

        // setup text to display location in latlng
        TextView textLocation = (TextView) findViewById(R.id.text_location);
        textLocation.setText(String.format("%s%s, %s",
                getResources().getString(R.string.textView_Location), latLngFormat.format(latLng.latitude), latLngFormat.format(latLng.longitude)));
    }

    // on click, verify data and upload to database
    public void onClick(View view) {
        String placeName = ((EditText) findViewById(R.id.editText_placeName)).getText().toString().trim();
        String reviewText = ((EditText) findViewById(R.id.editText_review)).getText().toString().trim();
        Double review;

        if (!reviewText.isEmpty()) {
            review = Double.parseDouble(reviewText);
                // if values aren't empty and are verified, round review and post to database
            if (!placeName.isEmpty() && review >= 0 && review <= 5) {
                review = Double.parseDouble(reviewFormat.format(review));

                databaseReference.push().setValue(new Coordinates(Double.parseDouble(latLngFormat.format(latLng.latitude)), Double.parseDouble(latLngFormat.format(latLng.longitude)), placeName, review));
                finish();
            } else if (placeName.isEmpty()){
                Toast.makeText(this, "Pub Name Field is empty", Toast.LENGTH_SHORT).show();
            } else if (review < 0 || review > 5) {
                Toast.makeText(this, "Review field must be between 0-5", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Review Field is empty", Toast.LENGTH_SHORT).show();
        }
    }
}