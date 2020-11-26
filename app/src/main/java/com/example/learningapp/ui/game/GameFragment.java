package com.example.learningapp.ui.game;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.DynastyQuery;
import com.example.LearningApp.LevelQuery;
import com.example.LearningApp.type.LevelWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.example.learningapp.ui.idiom.IdiomFragmentArgs;
import com.example.learningapp.ui.level.LevelFragmentDirections;
import com.example.learningapp.ui.result.ResultFragmentDirections;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class GameFragment extends Fragment {
    int questionIndex;
    String currentIdiom;
    String shuffledIdiom;
    ArrayList<String> selectedChars = new ArrayList<String>();
    TextView questionTextView;
    List<LevelQuery.Idiom> idioms;
    TextView questionIndexView;
    Handler handler = new Handler();
    CountDownTimer timer;
    ProgressBar progressBar;
    int correctCount = 0;
    int levelCode;
    String levelName;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game, container, false);
        TextView textView = root.findViewById(R.id.fragment_game_textView);
        questionTextView = root.findViewById(R.id.fragment_game_question_textView);
        Apollo apollo = new Apollo(requireContext(), requireActivity());
        apollo.checkIsLoginWithAction();
        if (getArguments() != null) {
            questionIndex = 0;
            GameFragmentArgs fragmentArgs = GameFragmentArgs.fromBundle(getArguments());
            levelCode = fragmentArgs.getLevelCode();
            if (apollo.isNetworkAvailable()) {

                LevelQuery.Level level = getLevel(apollo, levelCode);
                levelName = level.name();
                textView.setText(level.name());
                idioms = level.idioms();
                questionIndexView = root.findViewById(R.id.fragment_game_index);
                ImageView imageView = root.findViewById(R.id.fragment_game_ansCheckView);
                progressBar = root.findViewById(R.id.fragment_game_progress);
                TextView tipsView = root.findViewById(R.id.fragment_game_tips_textview);
                ImageView tipsIcon = root.findViewById(R.id.fragment_game_tips_icon);
                tipsView.setClickable(true);
                tipsIcon.setClickable(true);
                tipsIcon.setOnClickListener(v -> {
                    showTips(root, idioms.get(questionIndex).description());
                });
                tipsView.setOnClickListener(v -> {
                    showTips(root, idioms.get(questionIndex).description());
                });
                imageView.setImageResource(R.color.transparent);
                currentIdiom = idioms.get(questionIndex).idiom();
                shuffledIdiom = shuffle(idioms.get(questionIndex).idiom());

                questionTextView.setText(getQuestionPlaceholder(selectedChars, shuffledIdiom.length()));
                addAnsView(root, shuffledIdiom);
                timer = new CountDownTimer(10000, 20) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progressBar.setProgress(10000 - Math.toIntExact(millisUntilFinished));
                    }

                    @Override
                    public void onFinish() {
                        checkIsNextQuestion(root);
                        progressBar.setProgress(0);
                    }
                };
                timer.start();
            } else {
                textView.setText("請檢查網絡連接");
            }

        }
        return root;

    }
    private String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return  output.toString();
    }
    private void showTips(View view, String description) {
        PopUpClass popUpClass = new PopUpClass();
        popUpClass.showPopupWindow(view, description);
    }

    private void checkIsNextQuestion(View view) {
        ImageView imageView = view.findViewById(R.id.fragment_game_ansCheckView);
        FlexboxLayout flexboxLayout = view.findViewById(R.id.fragment_game_ans_layout);
        flexboxLayout.removeAllViews();
        StringBuilder checkString = new StringBuilder();
        for (String s : selectedChars) {
            checkString.append(s);
        }
        Log.i("Correct Ans",currentIdiom);
        Log.i("Yout Ans",checkString.toString());
        if (currentIdiom.equals(checkString.toString())) {
            imageView.setImageResource(R.drawable.ic_baseline_check_24);
            correctCount += 1;
        } else {
            imageView.setImageResource(R.drawable.ic_baseline_close_24);
        }
        timer.cancel();
        progressBar.setProgress(0);
        if (questionIndex >= 4) {
            Log.i("Log", "GameEnd");
            Runnable gameEndRender = () -> {
                GameFragmentDirections.ActionGameFragmentToResultFragment action = GameFragmentDirections.actionGameFragmentToResultFragment(correctCount, levelCode, levelName);
                Navigation.findNavController(view).navigate(action);
            };
            handler.postDelayed(gameEndRender, 2000);
        } else {

            Runnable render = () -> {
                questionIndex += 1;
                selectedChars.clear();
                currentIdiom = idioms.get(questionIndex).idiom();
                shuffledIdiom = shuffle(idioms.get(questionIndex).idiom());
                questionTextView.setText(getQuestionPlaceholder(selectedChars, shuffledIdiom.length()));

                addAnsView(view, shuffledIdiom);
                questionIndexView.setText(String.format("第%s題/共5題", questionIndex + 1));
                imageView.setImageResource(R.color.transparent);

                timer.start();
            };
            handler.postDelayed(render, 2000);
        }


    }

    private void addAnsView(View view, String idiom) {
        FlexboxLayout flexboxLayout = view.findViewById(R.id.fragment_game_ans_layout);
        for (int i = 0; i < idiom.length(); i++) {
            CardView cardView = new CardView(view.getContext());
            TextView textView = new TextView(view.getContext());
            String currentChar = String.valueOf(idiom.charAt(i));
            cardView.setLayoutParams(new CardView.LayoutParams(190, 190));
            cardView.setContentPadding(10, 10, 10, 10);
            cardView.setPadding(10, 10, 10, 10);
            cardView.setElevation(10);
            cardView.setUseCompatPadding(true);
            textView.setText(String.valueOf(idiom.charAt(i)));
            textView.setGravity(Gravity.CENTER);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(20);
            cardView.addView(textView);
            textView.setClickable(true);
            textView.setOnClickListener(v -> {
                        selectedChars.add(String.valueOf(currentChar));
                        cardView.animate().alpha(0).setDuration(300);
                        textView.animate().alpha(0).setDuration(300);
                        textView.setClickable(false);
                        flexboxLayout.removeView(cardView);
                        questionTextView.setText(getQuestionPlaceholder(selectedChars, shuffledIdiom.length()));
                        Log.i("OnClick", "NextChar");
                        if (selectedChars.size() == currentIdiom.length()) {
                            Log.i("OnClick", "NextQuestion");
                            checkIsNextQuestion(view);
                        }
                    }
            );
            flexboxLayout.addView(cardView);
        }

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
