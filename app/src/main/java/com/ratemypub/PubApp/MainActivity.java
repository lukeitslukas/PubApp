package com.ratemypub.PubApp;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ratemypub.PubApp.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // check if user is logged in and update header
        FirebaseUser currentUser = mAuth.getCurrentUser();
        editHeaderText(currentUser);
    }

    @Override
    public void onStart() {
        super.onStart();

        // setup nav drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // check if user is logged in and update header
        FirebaseUser currentUser = mAuth.getCurrentUser();
        editHeaderText(currentUser);

        // setup navbar
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map, R.id.nav_slideshow, R.id.nav_login, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();

        // show login/register or logout depending on user status
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(currentUser == null);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(currentUser != null);

        // finish nav setup
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public void onResume() {
        super.onResume();
        // re-setup nav drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // check if user logged in and update header
        FirebaseUser currentUser = mAuth.getCurrentUser();
        editHeaderText(currentUser);

        // show login/register or logout depending on user status
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(currentUser == null);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(currentUser != null);

        // finish nav setup
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void editHeaderText(FirebaseUser currentUser) {
        // show start of email in header if logged in
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        View nav_view_header =  nav_view.getHeaderView(0);
        TextView textViewEmail = (TextView) nav_view_header.findViewById(R.id.nav_header_text_main);
        TextView textViewDesc = (TextView) nav_view_header.findViewById(R.id.nav_header_text_desc);

        if (currentUser != null) {
            textViewEmail.setText(String.format("%s, %s",
                    getResources().getString(R.string.nav_header_title),
                    Objects.requireNonNull(currentUser.getEmail()).split("@")[0]));
            textViewDesc.setText(getResources().getString(R.string.nav_header_subtitle_loggedIn));
        } else {
            textViewEmail.setText(getResources().getString(R.string.nav_header_title));
            textViewDesc.setText(getResources().getString(R.string.nav_header_subtitle));
        }

    }
}