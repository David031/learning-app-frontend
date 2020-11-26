package com.example.learningapp.ui.result;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.example.LearningApp.CreateRecordMutation;
import com.example.LearningApp.CreateUserMutation;
import com.example.LearningApp.LevelsQuery;
import com.example.LearningApp.UpdateRecordMutation;
import com.example.LearningApp.UpdateUserMutation;
import com.example.LearningApp.UserQuery;
import com.example.LearningApp.type.LevelCreateOneInput;
import com.example.LearningApp.type.LevelCreateOneWithoutRecordsInput;
import com.example.LearningApp.type.LevelCreateWithoutRecordsInput;
import com.example.LearningApp.type.LevelUpdateOneRequiredInput;
import com.example.LearningApp.type.LevelWhereUniqueInput;
import com.example.LearningApp.type.RecordCreateInput;
import com.example.LearningApp.type.RecordCreateWithoutUserInput;
import com.example.LearningApp.type.RecordStatus;
import com.example.LearningApp.type.RecordUpdateInput;
import com.example.LearningApp.type.RecordUpdateManyWithoutUserInput;
import com.example.LearningApp.type.RecordWhereUniqueInput;
import com.example.LearningApp.type.UserCreateOneWithoutRecordsInput;
import com.example.LearningApp.type.UserUpdateInput;
import com.example.LearningApp.type.UserWhereUniqueInput;
import com.example.learningapp.Apollo;
import com.example.learningapp.R;
import com.example.learningapp.ui.idiom.IdiomFragmentArgs;
import com.example.learningapp.ui.level.LevelFragmentDirections;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResultFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_result, container, false);
        final TextView textView = root.findViewById(R.id.fragment_result_textView);
        textView.setText("關卡完成");
        Apollo apollo = new Apollo(requireContext(), requireActivity());
        apollo.checkIsLoginWithAction();
        if (getArguments() != null) {
            ResultFragmentArgs fragmentArgs = ResultFragmentArgs.fromBundle(getArguments());
            int correctCount = fragmentArgs.getCorrectCount();
            int levelCode = fragmentArgs.getLevelCode();
            String levelName = fragmentArgs.getLevelName();
            if (apollo.isNetworkAvailable()) {
                UserQuery.Record oldRecord = checkRecordExist(levelCode, apollo);
                if (oldRecord == null) {
                    CreateRecordMutation.CreateRecord createRecord = createRecordByLevel(correctCount, levelCode, apollo);
                    if (correctCount >= 3) {
                        UpdateUserMutation.UpdateUser updateUser = updateUserMaxLevel(apollo);
                    }
                } else {
                    UserQuery.MaxUnlockedLevel level = apollo.getUser().maxUnlockedLevel();
                    if (level.code() == levelCode && correctCount >=  3) {
                        UpdateUserMutation.UpdateUser updateUser = updateUserMaxLevel(apollo);
                    }
                    UpdateRecordMutation.UpdateRecord updateRecord = updateRecord(getRecordStatus(correctCount), oldRecord.id(), apollo);
                }
                requireActivity().runOnUiThread(() -> {
                    addStarView(root, getRecordStatus(correctCount));
                    TextView correctCountView = root.findViewById(R.id.fragment_result_correctCount);
                    TextView levelNameView = root.findViewById(R.id.fragment_result_level_textview);
                    Button backBtn = root.findViewById(R.id.fragment_result_backHome);
                    levelNameView.setText(levelName);
                    correctCountView.setText(String.format("正確%s題 / 共5題", correctCount));
                    backBtn.setOnClickListener((v -> {
                        NavDirections navDirections = ResultFragmentDirections.actionResultFragmentToNavigationLevel();
                        Navigation.findNavController(root).navigate(navDirections);
                    }));
                });
            } else {
                textView.setText("請檢查網絡連接");
            }
        }

        return root;
    }

    private void addStarView(View view, RecordStatus status) {
        ImageView starView3 = new ImageView(view.getContext());
        ImageView starView2 = new ImageView(view.getContext());
        ImageView starView1 = new ImageView(view.getContext());
        LinearLayout linearLayout = view.findViewById(R.id.fragment_result_star_layout);
        linearLayout.setGravity(Gravity.CENTER);

        starView1.setPadding(12, 12, 12, 12);
        starView2.setPadding(12, 12, 12, 12);
        starView3.setPadding(12, 12, 12, 12);

        switch (status) {
            case $UNKNOWN:
            case NOT_FINISH:
                starView1.setImageResource(R.drawable.ic_baseline_star_outline_24);
                starView2.setImageResource(R.drawable.ic_baseline_star_outline_24);
                starView3.setImageResource(R.drawable.ic_baseline_star_outline_24);
                break;
            case FINISH_TWO:
                starView1.setImageResource(R.drawable.ic_baseline_star_24);
                starView2.setImageResource(R.drawable.ic_baseline_star_outline_24);
                starView3.setImageResource(R.drawable.ic_baseline_star_outline_24);
                break;
            case FINISH_THREE:
                starView1.setImageResource(R.drawable.ic_baseline_star_24);
                starView2.setImageResource(R.drawable.ic_baseline_star_24);
                starView3.setImageResource(R.drawable.ic_baseline_star_outline_24);
                break;
            case FINISH_ALL:
                starView1.setImageResource(R.drawable.ic_baseline_star_24);
                starView2.setImageResource(R.drawable.ic_baseline_star_24);
                starView3.setImageResource(R.drawable.ic_baseline_star_24);
                break;
        }
        linearLayout.addView(starView1);
        linearLayout.addView(starView2);
        linearLayout.addView(starView3);
    }

    private UpdateRecordMutation.UpdateRecord updateRecord(RecordStatus status, String recordId, Apollo apollo) {
        RecordUpdateInput data = RecordUpdateInput.builder().status(status).build();
        RecordWhereUniqueInput whereUniqueInput = RecordWhereUniqueInput.builder().id(recordId).build();
        UpdateRecordMutation mutation = UpdateRecordMutation.builder().data(data).where(whereUniqueInput).build();

        ApolloCall<UpdateRecordMutation.Data> apolloCall = apollo.getApolloClient().mutate(mutation);
        Response<UpdateRecordMutation.Data> response = Rx3Apollo.from(apolloCall).blockingFirst();
        return Objects.requireNonNull(response.getData()).updateRecord();
    }

    private UserQuery.Record checkRecordExist(int levelCode, Apollo apollo) {
        UserQuery.User user = apollo.getUser();
        List<UserQuery.Record> records = user.records();
        UserQuery.Record result = null;
        for (int i = 0; i < Objects.requireNonNull(records).size(); i++) {
            if (records.get(i).level().code() == levelCode) {
                result = records.get(i);
                break;
            }
        }
        return result;
    }

    private UpdateUserMutation.UpdateUser updateUserMaxLevel(Apollo apollo) {
        String email = apollo.getDefaults("email");
        UserQuery.User user = apollo.getUser();
        UserQuery.MaxUnlockedLevel level = user.maxUnlockedLevel();
        LevelWhereUniqueInput levelWhereUniqueInput = LevelWhereUniqueInput.builder().code(level.code() + 1).build();
        LevelUpdateOneRequiredInput levelUpdateOneRequiredInput = LevelUpdateOneRequiredInput.builder().connect(levelWhereUniqueInput).build();
        UserUpdateInput data = UserUpdateInput.builder().maxUnlockedLevel(levelUpdateOneRequiredInput).build();
        UserWhereUniqueInput where = UserWhereUniqueInput.builder().email(email).build();
        UpdateUserMutation mutation = UpdateUserMutation.builder().data(data).where(where).build();

        ApolloCall<UpdateUserMutation.Data> apolloCall = apollo.getApolloClient().mutate(mutation);
        Response<UpdateUserMutation.Data> response = Rx3Apollo.from(apolloCall).blockingFirst();
        return Objects.requireNonNull(response.getData()).updateUser();
    }

    private CreateRecordMutation.CreateRecord createRecordByLevel(int correctCount, int levelCode, Apollo apollo) {
        RecordStatus recordStatus = getRecordStatus(correctCount);
        String email = apollo.getDefaults("email");
        UserWhereUniqueInput userWhereUniqueInput = UserWhereUniqueInput.builder().email(email).build();
        UserCreateOneWithoutRecordsInput userCreateOneWithoutRecordsInput = UserCreateOneWithoutRecordsInput.builder().connect(userWhereUniqueInput).build();

        LevelWhereUniqueInput levelWhereUniqueInput = LevelWhereUniqueInput.builder().code(levelCode).build();
        LevelCreateOneWithoutRecordsInput levelCreateOneWithoutRecordsInput = LevelCreateOneWithoutRecordsInput.builder().connect(levelWhereUniqueInput).build();

        RecordCreateInput data = RecordCreateInput.builder().status(recordStatus).user(userCreateOneWithoutRecordsInput).level(levelCreateOneWithoutRecordsInput).build();
        CreateRecordMutation mutation = CreateRecordMutation.builder().data(data).build();

        ApolloCall<CreateRecordMutation.Data> apolloCreateRecordCall = apollo.getApolloClient().mutate(mutation);
        Response<CreateRecordMutation.Data> createRecordResponse = Rx3Apollo.from(apolloCreateRecordCall).blockingFirst();
        return Objects.requireNonNull(createRecordResponse.getData()).createRecord();
    }

    private RecordStatus getRecordStatus(int correctCount) {

        if (correctCount == 5) {
            return RecordStatus.FINISH_ALL;
        } else if (correctCount >= 3) {
            return RecordStatus.FINISH_THREE;
        } else if (correctCount >= 2) {
            return RecordStatus.FINISH_TWO;
        } else {
            return RecordStatus.NOT_FINISH;
        }
    }
}
