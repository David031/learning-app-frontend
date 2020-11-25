package com.example.learningapp.ui.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.CreateUserMutation;
import com.example.LearningApp.DynastyQuery;
import com.example.LearningApp.UserQuery;
import com.example.LearningApp.type.LevelCreateInput;
import com.example.LearningApp.type.LevelCreateOneInput;
import com.example.LearningApp.type.LevelWhereUniqueInput;
import com.example.LearningApp.type.UserCreateInput;
import com.example.LearningApp.type.UserWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

public class LoginFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        ConstraintLayout mainLayout = root.findViewById(R.id.fragment_login_main_layout);
        TextView textView = root.findViewById(R.id.fragment_login_textView);

        ConstraintLayout nameConstraintLayout = root.findViewById(R.id.fragment_login_name_layout);
        ConstraintLayout emailConstraintLayout = root.findViewById(R.id.fragment_login_email_layout);

        Apollo apollo = new Apollo();
        if (apollo.isNetworkAvailable(requireContext())) {
            textView.setText("登入/註冊");
            EditText nameEditText = root.findViewById(R.id.fragment_login_name_editText);
            EditText emailEditText = root.findViewById(R.id.fragment_login_email_editText);
            Button loginBtn = root.findViewById(R.id.fragment_login_loginButton);
            Button signUpBtn = root.findViewById(R.id.fragment_login_SignUpButton);
            loginBtn.setOnClickListener(v -> {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                Snackbar snackbar = Snackbar.make(mainLayout, "", Snackbar.LENGTH_LONG);
                if(!isValidEmail(email)){
                    snackbar.setText("電子郵箱格式錯誤！請重新輸入！").show();
                }else{
                    UserWhereUniqueInput where = UserWhereUniqueInput.builder().email(email).build();
                    UserQuery query = UserQuery.builder().where(where).build();
                    ApolloCall<UserQuery.Data> apolloCall = apollo.getApolloClient().query(query);
                    Response<UserQuery.Data> response = Rx3Apollo.from(apolloCall).blockingFirst();
                    UserQuery.User user = Objects.requireNonNull(response.getData()).user();
                    if(user == null){
                        snackbar.setText("用戶不存在！請先註冊！").show();
                    }else{
                        apollo.setDefaults("email",email,requireContext());
                        String prefEmail =  apollo.getDefaults("email",requireContext());
                        Log.i("Log",""+prefEmail);
                        requireActivity().recreate();
                        NavController navController = Navigation.findNavController(requireActivity(),R.id.main_nav_host_fragment);
                        navController.navigate(R.id.navigation_home);
                    }
                    Log.i("Apollo",user+"");
                }


            });
            signUpBtn.setOnClickListener(v -> {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                Snackbar snackbar = Snackbar.make(mainLayout, "", Snackbar.LENGTH_LONG);
                if(!isValidEmail(email)){
                    snackbar.setText("電子郵箱格式錯誤！請重新輸入！").show();
                }else{
                    UserWhereUniqueInput where = UserWhereUniqueInput.builder().email(email).build();
                    UserQuery query = UserQuery.builder().where(where).build();
                    ApolloCall<UserQuery.Data> apolloUserCall = apollo.getApolloClient().query(query);
                    Response<UserQuery.Data> response = Rx3Apollo.from(apolloUserCall).blockingFirst();
                    UserQuery.User user = Objects.requireNonNull(response.getData()).user();
                    Log.i("Apollo",user+"");
                    if(user != null){
                        snackbar.setText("用戶已存在！請直接登入！").show();
                    }else{
                        Log.i("Log","createuser");
                        Log.e("Log",name+"");
                        if(name.length()<1){
                            snackbar.setText("暱稱輸入錯誤！請重新輸入！").show();
                        }else{
                            LevelWhereUniqueInput levelWhereUniqueInput = LevelWhereUniqueInput.builder().code(1).build();
                            LevelCreateOneInput levelCreateOneInput = LevelCreateOneInput.builder().connect(levelWhereUniqueInput).build();
                            UserCreateInput data = UserCreateInput.builder().email(email).name(name).maxUnlockedLevel(levelCreateOneInput).build();
                            CreateUserMutation mutation = CreateUserMutation.builder().data(data).build();
                            ApolloCall<CreateUserMutation.Data> apolloCreateUserCall = apollo.getApolloClient().mutate(mutation);
                            Response<CreateUserMutation.Data> createUserResponse = Rx3Apollo.from(apolloCreateUserCall).blockingFirst();
                            CreateUserMutation.CreateUser createUser =  Objects.requireNonNull(createUserResponse.getData()).createUser();

                            apollo.setDefaults("email",email,requireContext());
                            String prefEmail =  apollo.getDefaults("email",requireContext());
                            Log.i("Log",""+prefEmail);
                            requireActivity().recreate();
                            NavController navController = Navigation.findNavController(requireActivity(),R.id.main_nav_host_fragment);
                            navController.navigate(R.id.navigation_home);
                        }
                    }

                }

            });
        } else {
            textView.setText("請檢查網絡連接");
        }

        return root;
    }
    private  boolean isValidEmail(String email){
        String regex = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
        return email.matches(regex);
    }
}