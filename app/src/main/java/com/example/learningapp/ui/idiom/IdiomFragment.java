package com.example.learningapp.ui.idiom;

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
import com.example.LearningApp.DynastyQuery;
import com.example.LearningApp.type.DynastyWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.example.learningapp.ui.home.HomeFragmentDirections;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IdiomFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_idiom, container, false);
        TextView textView = root.findViewById(R.id.fragment_idiom_textview);
        LinearLayout linearLayout = root.findViewById(R.id.fragment_idiom_scrollView_layout);
        if (getArguments() != null) {
            IdiomFragmentArgs fragmentArgs = IdiomFragmentArgs.fromBundle(getArguments());
            String dynastyCode = fragmentArgs.getDynastyCode();
            Apollo apollo = new Apollo();
            DynastyWhereUniqueInput where = DynastyWhereUniqueInput.builder().code(dynastyCode).build();
            DynastyQuery dynastyQuery =  DynastyQuery.builder().where(where).build();
            apollo.getApolloClient().query(dynastyQuery).enqueue((new ApolloCall.Callback<DynastyQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<DynastyQuery.Data> response) {
                    DynastyQuery.Dynasty dynasty = response.getData().dynasty();
                    List<DynastyQuery.Idiom> idioms = dynasty.idioms();
                    Log.i("Apollo", "Data: " + dynasty);

                    getActivity().runOnUiThread(() -> {
                        textView.setText(dynasty.dynastyName());
                        for (int i = 0; i < idioms.size(); i++) {
                            addCardView(linearLayout, idioms.get(i).idiom(),idioms.get(i).description(),  root);
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("Apollo", "Error", e);
                }
            }));

        }

        return root;
    }

    private void addCardView(LinearLayout linearLayout, String title, String content,  View view) {
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
        cardLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(790, LinearLayout.LayoutParams.WRAP_CONTENT));
        cardLinearLayout.addView(addTitleTextView(title, view));
        cardLinearLayout.addView(addContentTextView(content, view));
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
