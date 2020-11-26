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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.UserQuery;
import com.example.LearningApp.type.UserWhereUniqueInput;

import java.util.Objects;

public class Apollo {
    ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl("http://dxdev.myds.me:1234")
            .build();

    Context context;
    Activity activity;

    public Apollo(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void checkIsLoginWithAction() {
        String email = getDefaults("email");
        if (email == null) {
            NavController navController = Navigation.findNavController(activity, R.id.main_nav_host_fragment);
            navController.navigate(R.id.loginFragment);
        }
    }

    public boolean isLogin() {
        String email = getDefaults("email");
        return email != null;
    }

    public void setDefaults(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getDefaults(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public UserQuery.User getUser() {
        String email = getDefaults("email");
        UserWhereUniqueInput where = UserWhereUniqueInput.builder().email(email).build();
        UserQuery query = UserQuery.builder().where(where).build();
        ApolloCall<UserQuery.Data> apolloCall = getApolloClient().query(query);
        Response<UserQuery.Data> response = Rx3Apollo.from(apolloCall).blockingFirst();
        UserQuery.User user = Objects.requireNonNull(response.getData()).user();
        return user;
    }
}
