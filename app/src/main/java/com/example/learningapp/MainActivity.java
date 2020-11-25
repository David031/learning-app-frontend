package com.example.learningapp;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Apollo apollo = new Apollo(this.getBaseContext(),this);
        if (apollo.isLogin()){
            NavController navController = Navigation.findNavController(this, R.id.main_nav_host_fragment);
            BottomNavigationView navView = findViewById(R.id.main_nav_view);
            NavigationUI.setupWithNavController(navView, navController);
        }
    }
}