package com.example.learningapp.ui.level;

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
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.LevelsQuery;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.example.learningapp.ui.home.HomeFragmentDirections;

import java.util.List;
import java.util.Objects;

public class LevelFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level, container, false);
        final TextView textView = root.findViewById(R.id.fragment_level_textView);
        textView.setText("成語挑戰");
        LinearLayout linearLayout = root.findViewById(R.id.fragment_level_scrollView_layout);
        Apollo apollo = new Apollo();
        apollo.checkIsLoginWithAction(requireContext(),requireActivity());
        if(apollo.isNetworkAvailable(requireContext())){
            ApolloCall<LevelsQuery.Data> levelApolloCall = apollo.getApolloClient().query(new LevelsQuery());
            Response<LevelsQuery.Data> levelResponse = Rx3Apollo.from(levelApolloCall).blockingFirst();
            List<LevelsQuery.Level> levels = Objects.requireNonNull(levelResponse.getData()).levels();
            Log.d("Apollo", levels + "");
            requireActivity().runOnUiThread(() -> {
                for (int i = 0; i < levels.size(); i++) {
                    addCardView(linearLayout, levels.get(i).name(), "成語出自 - "+levels.get(i).dynasty().dynastyName(),levels.get(i).idioms() , root);
                }
            });
        }else{
            textView.setText("請檢查網絡連接");
        }

        return root;
    }

    private void addCardView(LinearLayout linearLayout, String title, String content,  List<LevelsQuery.Idiom> idioms, View view) {
        LinearLayout cardLinearLayout = new LinearLayout(view.getContext());
        cardLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout cardLinearLayoutRow = new LinearLayout(view.getContext());
        cardLinearLayoutRow.setOrientation(LinearLayout.HORIZONTAL);
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
                Log.d("Idioms",idioms+"");
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
        textView.setPadding(0, 16, 0, 0);
        return textView;
    }
}