package com.ratemypub.PubApp.ui.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ratemypub.PubApp.LoginActivity;
import com.ratemypub.PubApp.R;
import com.ratemypub.PubApp.RegisterActivity;
import com.ratemypub.PubApp.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // assign buttons
        Button login = (Button) root.findViewById(R.id.button_login);
        Button register = (Button) root.findViewById(R.id.button_register);

        // setup button listeners to launch activity on press
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        // check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        final NavController navController = Navigation.findNavController(this.requireActivity(), R.id.nav_host_fragment_content_main);

        // move to home fragment if logged in
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                navController.navigate(R.id.nav_home);
            }
        } catch(Exception error){
            System.out.println("error");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // check if logged in
        mAuth = FirebaseAuth.getInstance();
        final NavController navController = Navigation.findNavController(this.requireActivity(), R.id.nav_host_fragment_content_main);

        // move to home frament if logged in
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                navController.navigate(R.id.nav_home);
            }
        } catch(Exception error){
            System.out.println("WeeWoo");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}