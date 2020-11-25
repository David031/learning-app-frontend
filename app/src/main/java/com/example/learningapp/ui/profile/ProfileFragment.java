package com.example.learningapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.UserQuery;
import com.example.LearningApp.type.UserWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.fragment_profile_textView);
        TextView emailTextView = root.findViewById(R.id.fragment_profile_email_textView);
        TextView nameTextView = root.findViewById(R.id.fragment_profile_name_textView);
        Button logoutButton = root.findViewById(R.id.fragment_profile_logout_button);
        Apollo apollo = new Apollo();
        apollo.checkIsLoginWithAction(requireContext(),requireActivity());
        textView.setText("我的");
        if (apollo.isNetworkAvailable(requireContext())){
            String email = apollo.getDefaults("email",requireContext());
            UserWhereUniqueInput where = UserWhereUniqueInput.builder().email(email).build();
            UserQuery query = UserQuery.builder().where(where).build();
            ApolloCall<UserQuery.Data> apolloCall = apollo.getApolloClient().query(query);
            Response<UserQuery.Data> response = Rx3Apollo.from(apolloCall).blockingFirst();
            UserQuery.User user = Objects.requireNonNull(response.getData()).user();
            emailTextView.setText(user.email());
            nameTextView.setText(user.name());
            logoutButton.setOnClickListener(v -> {
                apollo.setDefaults("email",null,requireContext());
                requireActivity().recreate();
                NavController navController = Navigation.findNavController(requireActivity(),R.id.main_nav_host_fragment);
                navController.navigate(R.id.loginFragment);
            });
        }else{
            textView.setText("請檢查網絡連接");
        }

        return root;
    }
}