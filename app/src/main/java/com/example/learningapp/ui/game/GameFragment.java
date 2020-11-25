package com.example.learningapp.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.DynastyQuery;
import com.example.LearningApp.LevelQuery;
import com.example.LearningApp.type.LevelWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.example.learningapp.ui.idiom.IdiomFragmentArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GameFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game, container, false);
        TextView textView = root.findViewById(R.id.fragment_game_textView);
        TextView questionTextView = root.findViewById(R.id.fragment_game_question_textView);
        Apollo apollo = new Apollo(requireContext(), requireActivity());
        apollo.checkIsLoginWithAction();
        if (getArguments() != null) {
            GameFragmentArgs fragmentArgs = GameFragmentArgs.fromBundle(getArguments());
            int levelCode = fragmentArgs.getLevelCode();
            if (apollo.isNetworkAvailable()) {
                LevelQuery.Level level = getLevel(apollo, levelCode);
                textView.setText(level.name());
                List<LevelQuery.Idiom> idioms = level.idioms();

                int questionIndex = 0;
                String currentIdiom = idioms.get(questionIndex).idiom();
                ArrayList<String> selectedChars = new ArrayList<String>();

                questionTextView.setText(getQuestionPlaceholder(selectedChars, currentIdiom.length()));


            } else {
                textView.setText("請檢查網絡連接");
            }

        }
        return root;

    }
    private  void addAnsView(View view){

    }
    private String getQuestionPlaceholder(ArrayList<String> selectedChars, int idiomLength) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < selectedChars.size(); i++) {
            result.append(selectedChars.get(i));
        }
        for (int i = 0; i < (idiomLength - selectedChars.size()); i++) {
            result.append(" _ ");
        }
        return result.toString();
    }

    private LevelQuery.Level getLevel(Apollo apollo, int levelCode) {
        LevelWhereUniqueInput levelWhereUniqueInput = LevelWhereUniqueInput.builder().code(levelCode).build();
        LevelQuery levelQuery = LevelQuery.builder().where(levelWhereUniqueInput).build();
        ApolloCall<LevelQuery.Data> levelApolloCall = apollo.getApolloClient().query(levelQuery);
        Response<LevelQuery.Data> levelResponse = Rx3Apollo.from(levelApolloCall).blockingFirst();
        return Objects.requireNonNull(levelResponse.getData()).level();
    }
}
