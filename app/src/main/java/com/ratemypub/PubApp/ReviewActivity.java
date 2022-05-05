package com.ratemypub.PubApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ratemypub.PubApp.ui.home.Reviews;
import com.ratemypub.PubApp.ui.map.Coordinates;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ReviewActivity extends AppCompatActivity {

    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> adapter;
    private final DecimalFormat ratingFormat = new DecimalFormat("#.#");
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://pubapp-64a3b-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference reviewsReference = firebaseDatabase.getReference("reviews");
    private final DatabaseReference coordinatesReference = firebaseDatabase.getReference("coordinates");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // set rounding format
        ratingFormat.setRoundingMode(RoundingMode.HALF_EVEN);

        // setup list of string and setup adapter
        stringArrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stringArrayList);

        spinner.setAdapter(adapter);

        // setup listener for initial db setup and
        coordinatesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear the list on data change
                stringArrayList.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    Coordinates coordinates = s.getValue(Coordinates.class);
                    assert coordinates != null;
                    stringArrayList.add(coordinates.placeName);
                }
                // update adapter
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if back button pressed return to home fragment
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        // take in data and validate
        String reviewText = ((EditText) findViewById(R.id.editText_reviewText)).getText().toString().trim();
        String pubName = ((Spinner) findViewById(R.id.spinner)).getSelectedItem().toString();
        Double rating = Double.parseDouble(((EditText) findViewById(R.id.editText_rating)).getText().toString().trim());
        rating = Double.parseDouble(ratingFormat.format(rating));

        if (!reviewText.isEmpty() && !pubName.isEmpty()) {
            // if values aren't empty and are verified, round review and post to database
            if (rating >= 0.0 && rating <= 5.0) {
                // push new review and find pub to update average rating number then finish activity
                reviewsReference.push().setValue(new Reviews(reviewText, pubName, rating));
                Query query = coordinatesReference.orderByChild("placeName").equalTo(pubName);
                Double finalRating = rating;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot s : dataSnapshot.getChildren()) {
                            Coordinates coordinates = s.getValue(Coordinates.class);
                            String key = s.getKey();
                            assert key != null;
                            assert coordinates != null;
                            coordinatesReference.child(key).child("rating")
                                    .setValue(Double.parseDouble(ratingFormat.format((coordinates.rating + finalRating) / 2)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
                finish();
            } else if (rating < 0 || rating > 5) {
                Toast.makeText(this, "Review field must be between 0-5", Toast.LENGTH_SHORT).show();
            }
        } else if(pubName.isEmpty()){
            Toast.makeText(this, "Please pick a pub", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Review Field is empty", Toast.LENGTH_SHORT).show();
        }
    }
}