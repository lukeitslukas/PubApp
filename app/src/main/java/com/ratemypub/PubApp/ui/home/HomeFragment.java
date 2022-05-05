package com.ratemypub.PubApp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ratemypub.PubApp.NewPubActivity;
import com.ratemypub.PubApp.R;
import com.ratemypub.PubApp.ReviewActivity;
import com.ratemypub.PubApp.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    // setup database reference
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://pubapp-64a3b-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reviews");
    private AppBarConfiguration mAppBarConfiguration;
    private ArrayList<TextView> reviewsList;
    private FragmentHomeBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // create list to store TextViews
        reviewsList = new ArrayList<>();

        // check if user logged in
        FirebaseUser user = mAuth.getCurrentUser();

        LinearLayout linearLayout = (LinearLayout) root.findViewById(R.id.inner_linear);

        // create database listener to setup textviews and change them dynamically
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear the layout and the textview list
                linearLayout.removeAllViews();
                reviewsList.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    // for each value in review db, create textview and add to list
                    Reviews reviews = s.getValue(Reviews.class);
                    assert reviews != null;
                    TextView tv = new TextView(getContext());
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10, 10, 10, 10);
                    tv.setLayoutParams(layoutParams);
                    tv.setText(HtmlCompat.fromHtml("<b>" + reviews.pubName + " : " + reviews.rating + "</b>" +  "<br />" + reviews.description, HtmlCompat.FROM_HTML_MODE_COMPACT));
                    tv.setTextColor(0xFF000000);
                    tv.setBackgroundColor(0xFFe6e6e6); // hex color 0xAARRGGBB
                    tv.setGravity(Gravity.CENTER);
                    tv.setPadding(0, 20, 0, 20);// in pixels (left, top, right, bottom
                    linearLayout.addView(tv, 0);
                    reviewsList.add(tv);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);

        // if user is logged in, display FAB
        if (user != null) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ReviewActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            fab.setVisibility(View.GONE);
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if user is logged in and update FAB
        FirebaseUser user = mAuth.getCurrentUser();
        FloatingActionButton fab = (FloatingActionButton) requireActivity().findViewById(R.id.fab);
        if (user != null) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }
}