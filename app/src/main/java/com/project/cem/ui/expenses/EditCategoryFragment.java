package com.project.cem.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class EditCategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_USER_ID = "user_id";

    private ExpenseCategory category;
    private EditText etCategoryName;
    private Button btnSave;
    private ExpenseCategoryRepository categoryRepository;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_category, container, false);

        etCategoryName = view.findViewById(R.id.etCategoryName);
        btnSave = view.findViewById(R.id.btnSave);

        // Hiển thị thông tin danh mục hiện tại
        if (category != null) {
            etCategoryName.setText(category.getCategoryName());
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
            String newCategoryName = etCategoryName.getText().toString().trim();
            if (newCategoryName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật danh mục
            category.setCategoryName(newCategoryName);
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