package com.example.learningapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.LearningApp.DynastiesQuery;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.fragment_home_textView);
        textView.setText("成語學習");
        LinearLayout linearLayout = root.findViewById(R.id.fragment_home_scrollView_layout);
        Apollo apollo = new Apollo();
        apollo.getApolloClient().query(new DynastiesQuery()).enqueue((new ApolloCall.Callback<DynastiesQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<DynastiesQuery.Data> response) {
                List<DynastiesQuery.Dynasty> dynasties = response.getData().dynasties();
                Log.i("Apollo", "Data: " + dynasties);
                getActivity().runOnUiThread(() -> {
                    for (int i = 0; i < dynasties.size(); i++) {
                        addCardView(linearLayout, dynasties.get(i).dynastyName(),"成語",dynasties.get(i).code(),  root);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
            }
        }));


        return root;

    }

    private void addCardView(LinearLayout linearLayout, String title, String content, String dynastyCode, View view) {
        LinearLayout cardLinearLayout = new LinearLayout(view.getContext());
        cardLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout cardLinearLayoutRow = new LinearLayout(view.getContext());
        cardLinearLayoutRow.setOrientation(LinearLayout.HORIZONTAL)
        ;
        CardView cardView = new CardView(view.getContext());

        cardView.setLayoutParams(new CardView.LayoutParams(900, LinearLayout.LayoutParams.WRAP_CONTENT));
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(32, 32, 32, 32);
        cardView.setRadius(10);
        cardView.setElevation(10);
        cardView.addView(cardLinearLayoutRow);
        cardLinearLayoutRow.addView(cardLinearLayout);
        cardLinearLayoutRow.setGravity(Gravity.CENTER);

        ImageView iconView = new ImageView(view.getContext());
        iconView.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24);

        cardLinearLayoutRow.addView(iconView);
        cardLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(790, LinearLayout.LayoutParams.WRAP_CONTENT));
        cardLinearLayout.addView(addTitleTextView(title, view));
        cardLinearLayout.addView(addContentTextView(content, view));
        cardView.setClickable(true);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragmentDirections.ActionNavigationHomeToIdiomFragment action = HomeFragmentDirections.actionNavigationHomeToIdiomFragment(dynastyCode);
                Navigation.findNavController(view).navigate(action);
            }
        });
        linearLayout.addView(cardView);
    }

    private TextView addTitleTextView(String title, View view) {
        TextView textView = new TextView(view.getContext());
        textView.setText(title);
        textView.setTextSize(20);
        return textView;
    }

    private TextView addContentTextView(String content, View view) {
        TextView textView = new TextView(view.getContext());
        textView.setText(content);
        textView.setPadding(0,16,0,0);
        return textView;
    }


}