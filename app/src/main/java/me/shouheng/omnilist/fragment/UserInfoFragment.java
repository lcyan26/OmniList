package me.shouheng.omnilist.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentUserInfoBinding;
import me.shouheng.omnilist.fragment.base.CommonFragment;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.UserUtil;
import me.shouheng.omnilist.viewmodel.StatisticViewModel;

/**
 * Created by wangshouheng on 2017/8/11. */
public class UserInfoFragment extends CommonFragment<FragmentUserInfoBinding> {

    private final static int REQUEST_FOR_LOGIN = 50000;

    private boolean logined = false;

    private StatisticViewModel statisticViewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_user_info;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        statisticViewModel = ViewModelProviders.of(this).get(StatisticViewModel.class);

        configAccountViews();

        getBinding().ctvTimeline.setOnCardTitleClickListener(this::toTimeLine);

        outputStatistic();
        getBinding().ctvStatistic.setOnCardTitleClickListener(this::toStatistics);

        getBinding().llLogout.setOnClickListener(v -> logout());
        ColorUtils.addRipple(getBinding().llLogout);
    }

    private void configAccountViews() {
        logined = UserUtil.getInstance(getContext()).getUserIdKept() != 0;
        if (!logined) {
            getBinding().tvUserAccount.setText("");
            getBinding().tvUserAccount.setVisibility(View.GONE);
            getBinding().tvUserName.setText(R.string.not_login_click_to_login);
            getBinding().tvUserName.setTextColor(accentColor());
        }

        getBinding().llUser.setOnClickListener(v -> login());
        ColorUtils.addRipple(getBinding().llUser);
        getBinding().ctvSchool.setOnCardTitleClickListener(this::showSchoolEditor);
        getBinding().ctvMajor.setOnCardTitleClickListener(this::showMajorEditor);
    }

    private void toTimeLine() {
        if (getActivity() != null && getActivity() instanceof OnItemSelectedListener) {
            ((OnItemSelectedListener) getActivity()).onTimelineSelected();
        }
    }

    private void outputStatistic() {
        getBinding().lcv.setValueSelectionEnabled(false);
        getBinding().lcv.setLineChartData(statisticViewModel.getDefaultAssignmentData(primaryColor()));
        statisticViewModel.getAddedModelData(ModelType.ASSIGNMENT).observe(this, listResource -> {
            LogUtils.d(listResource);
            if (listResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                return;
            }
            switch (listResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
                case LOADING:
                    break;
                case SUCCESS:
                    outputNotesStats(listResource.data);
                    break;
            }
        });
    }

    private void outputNotesStats(List<Integer> notes) {
        for (Line line : getBinding().lcv.getLineChartData().getLines()) {
            int length = line.getValues().size();
            PointValue pointValue;
            for (int i=0; i<length; i++) {
                pointValue = line.getValues().get(i);
                pointValue.setTarget(pointValue.getX(), notes.get(i));
            }
        }
        getBinding().lcv.startDataAnimation();
    }

    private void toStatistics() {
        if (getActivity() != null && getActivity() instanceof OnItemSelectedListener) {
            ((OnItemSelectedListener) getActivity()).onChatHeaderSelected();
        }
    }

    private void login() { }

    private void logout() { }

    private void showSchoolEditor() { }

    private void showMajorEditor() { }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(R.string.user_info);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FOR_LOGIN:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnItemSelectedListener {
        void onTimelineSelected();
        void onChatHeaderSelected();
    }
}
