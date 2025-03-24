package com.project.cem.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class EditCategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_USER_ID = "user_id";

    private ExpenseCategory category;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ExpenseCategoryRepository categoryRepository;
    private List<ExpenseCategory> allCategories; // Danh sách tất cả danh mục
    private List<String> categoryNames; // Danh sách tên danh mục để hiển thị trong Spinner

    public EditCategoryFragment() {
        // Required empty public constructor
    }

    public static EditCategoryFragment newInstance(ExpenseCategory category) {
        EditCategoryFragment fragment = new EditCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, category.getCategoryID());
        args.putString(ARG_CATEGORY_NAME, category.getCategoryName());
        args.putInt(ARG_USER_ID, category.getUserID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        categoryRepository = new ExpenseCategoryRepository(dbHelper);

        if (getArguments() != null) {
            int categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            String categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            int userId = getArguments().getInt(ARG_USER_ID);

            // Tái tạo đối tượng ExpenseCategory
            category = new ExpenseCategory();
            category.setCategoryID(categoryId);
            category.setCategoryName(categoryName);
            category.setUserID(userId);
        }

        // Lấy danh sách tất cả danh mục
        allCategories = categoryRepository.getAllCategories(category.getUserID());
        categoryNames = new ArrayList<>();
        for (ExpenseCategory cat : allCategories) {
            categoryNames.add(cat.getCategoryName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_category, container, false);

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);

        // Thiết lập Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Đặt danh mục hiện tại làm lựa chọn mặc định
        if (category != null) {
            int position = categoryNames.indexOf(category.getCategoryName());
            if (position != -1) {
                spinnerCategory.setSelection(position);
            }
        }

        // Thiết lập Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("Edit Category");
        }
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> {
            String selectedCategoryName = spinnerCategory.getSelectedItem().toString();
            if (selectedCategoryName.equals(category.getCategoryName())) {
                Toast.makeText(getContext(), "No changes made", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
                return;
            }

            // Cập nhật danh mục
            category.setCategoryName(selectedCategoryName);
            categoryRepository.updateExpenseCategory(category);

            // Gửi kết quả về CategoryFragment
            Bundle result = new Bundle();
            result.putBoolean("category_updated", true);
            getParentFragmentManager().setFragmentResult("category_updated_request", result);

            // Quay lại CategoryFragment
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}