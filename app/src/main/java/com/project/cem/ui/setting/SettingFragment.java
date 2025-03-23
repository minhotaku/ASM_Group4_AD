package com.project.cem.ui.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.project.cem.R;
import com.project.cem.ui.LoginActivity;
import com.project.cem.utils.UserPreferences;
import com.project.cem.ui.setting.ChangePasswordFragment;
import com.project.cem.ui.setting.report.ExpenseReportFragment;
import com.project.cem.ui.setting.recurring.RecurringExpenseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Button btnChangePassword = view.findViewById(R.id.btnGoToChangePassword);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnGotoRecurringExpense = view.findViewById(R.id.btnGotoRecurringExpense);
        Button btnGotoExpenseReport = view.findViewById(R.id.btnGotoExpenseReport);

        btnChangePassword.setOnClickListener(v -> {
            Fragment changePasswordFragment = new ChangePasswordFragment();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            // Thay thế bằng layout của container chứa fragment
            transaction.replace(R.id.frame_layout, changePasswordFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnGotoRecurringExpense.setOnClickListener(v -> {
            Fragment recurringExpenseFragment = new RecurringExpenseFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, recurringExpenseFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnGotoExpenseReport.setOnClickListener(v -> {
            Fragment expenseReportFragment = new ExpenseReportFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, expenseReportFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnLogout.setOnClickListener(v -> {

            UserPreferences.clearUser(getActivity());

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack Activity
            startActivity(intent);
        });

        return view;
    }
    }
