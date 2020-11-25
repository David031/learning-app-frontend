package com.example.learningapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.apollographql.apollo.ApolloClient;

import static androidx.core.content.ContextCompat.getSystemService;

public class Apollo {
    ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl("http://192.168.1.88:4466")
            .build();

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

    public boolean isNetworkAvailable( Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void checkIsLoginWithAction(Context context , Activity activity){
        String email = getDefaults("email",context);
        Log.i("Log Apollo",""+email);
        if(email == null){
            NavController navController = Navigation.findNavController(activity,R.id.main_nav_host_fragment);
            navController.navigate(R.id.loginFragment);
        }
    }
    public boolean isLogin(Context context ){
        String email = getDefaults("email",context);
        return email != null;
    }
    public  void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public  String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}
