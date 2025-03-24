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
import com.project.cem.model.User;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;

import java.util.List;

public class AddCategoryFragment extends Fragment {

    private EditText etCategoryName;
    private Button btnSave;
    private ExpenseCategoryRepository categoryRepository;

    public AddCategoryFragment() {
        // Required empty public constructor
    }

    public static AddCategoryFragment newInstance() {
        return new AddCategoryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        categoryRepository = new ExpenseCategoryRepository(dbHelper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category, container, false);

        etCategoryName = view.findViewById(R.id.etCategoryName);
        btnSave = view.findViewById(R.id.btnSave);

        // Thiết lập Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("Add Category");
        }
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            User currentUser = UserPreferences.getUser(requireContext());
            if (currentUser == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = currentUser.getUserID();

            // Kiểm tra xem tên danh mục đã tồn tại hay chưa
            List<ExpenseCategory> existingCategories = categoryRepository.getAllCategories(userId);
            boolean isCategoryExists = existingCategories.stream()
                    .anyMatch(cat -> cat.getCategoryName().equalsIgnoreCase(categoryName));
            if (isCategoryExists) {
                Toast.makeText(getContext(), "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo đối tượng ExpenseCategory
            ExpenseCategory newCategory = new ExpenseCategory();
            newCategory.setCategoryName(categoryName);
            newCategory.setUserID(userId);

            // Thêm danh mục mới
            categoryRepository.insertExpenseCategory(newCategory);

            // Gửi kết quả về CategoryFragment để cập nhật danh sách
            Bundle result = new Bundle();
            result.putBoolean("category_added", true);
            getParentFragmentManager().setFragmentResult("category_added_request", result);

            // Quay lại CategoryFragment
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}