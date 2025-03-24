package com.project.cem.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.cem.R;
import com.project.cem.model.User;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;
import com.project.cem.viewmodel.ExpenseViewModel;
import com.project.cem.repository.ExpenseRepository;

public class ExpensesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter expenseAdapter;
    private RecyclerView recyclerView;
    private int userId;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    public static ExpensesFragment newInstance(String param1, String param2) {
        ExpensesFragment fragment = new ExpensesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Cho phép fragment sử dụng menu

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        ExpenseRepository expenseRepository = new ExpenseRepository(dbHelper);
        expenseViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ExpenseViewModel(expenseRepository);
            }
        }).get(ExpenseViewModel.class);

        // Lắng nghe kết quả từ AddExpenseFragment
        getParentFragmentManager().setFragmentResultListener("expense_added_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean expenseAdded = result.getBoolean("expense_added", false);
                if (expenseAdded) {
                    expenseViewModel.fetchExpenses(userId);
                }
            }
        });

        // Lắng nghe kết quả từ EditExpenseFragment (chỉnh sửa)
        getParentFragmentManager().setFragmentResultListener("expense_updated_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean expenseUpdated = result.getBoolean("expense_updated", false);
                if (expenseUpdated) {
                    expenseViewModel.fetchExpenses(userId);
                }
            }
        });

        // Lắng nghe kết quả từ EditExpenseFragment (xóa)
        getParentFragmentManager().setFragmentResultListener("expense_deleted_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean expenseDeleted = result.getBoolean("expense_deleted", false);
                if (expenseDeleted) {
                    expenseViewModel.fetchExpenses(userId);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        recyclerView = view.findViewById(R.id.expenseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseViewModel, getId()); // Truyền getId() vào constructor
        recyclerView.setAdapter(expenseAdapter);

        // Thiết lập Toolbar làm ActionBar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("Expenses");
        }

        // Xử lý sự kiện nhấn FloatingActionButton
        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(getId(), new AddExpenseFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User currentUser = UserPreferences.getUser(requireContext());
        if (currentUser != null) {
            userId = currentUser.getUserID();
            expenseViewModel.fetchExpenses(userId);
            expenseViewModel.getExpensesLiveData().observe(getViewLifecycleOwner(), expenses -> {
                expenseAdapter.setExpenseList(expenses);
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu_expenses, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_category) {
            // Mở CategoryFragment khi nhấn vào item "Category"
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(getId(), CategoryFragment.newInstance());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}