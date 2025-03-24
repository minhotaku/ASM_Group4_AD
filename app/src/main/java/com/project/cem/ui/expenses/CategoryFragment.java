package com.project.cem.ui.expenses;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.User;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;
import com.project.cem.viewmodel.ExpenseCategoryViewModel;

import java.util.Arrays;
import java.util.List;

public class CategoryFragment extends Fragment {

    private ExpenseCategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private RecyclerView recyclerView;
    private int userId;
    private ExpenseCategoryRepository categoryRepository;

    // Danh sách danh mục mặc định
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Food", "Transportation", "Entertainment", "Housing", "Utilities", "Health", "Education"
    );

    // Biến static để kiểm soát việc hiển thị thông báo trong một phiên làm việc
    private static boolean hasShownEmptyDialogInSession = false;

    public CategoryFragment() {
        // Required empty public constructor
    }

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo repository
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        categoryRepository = new ExpenseCategoryRepository(dbHelper);

        // Lắng nghe kết quả từ AddCategoryFragment
        getParentFragmentManager().setFragmentResultListener("category_added_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean categoryAdded = result.getBoolean("category_added", false);
                if (categoryAdded) {
                    viewModel.fetchCategoriesWithCount(userId); // Cập nhật danh sách
                }
            }
        });

        // Lắng nghe kết quả từ EditCategoryFragment (chỉnh sửa)
        getParentFragmentManager().setFragmentResultListener("category_updated_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean categoryUpdated = result.getBoolean("category_updated", false);
                if (categoryUpdated) {
                    viewModel.fetchCategoriesWithCount(userId); // Cập nhật danh sách
                }
            }
        });

        // Lắng nghe kết quả từ EditCategoryFragment (xóa)
        getParentFragmentManager().setFragmentResultListener("category_deleted_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean categoryDeleted = result.getBoolean("category_deleted", false);
                if (categoryDeleted) {
                    viewModel.fetchCategoriesWithCount(userId); // Cập nhật danh sách
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        recyclerView = view.findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(getId());
        recyclerView.setAdapter(adapter);

        // Thiết lập Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("Expense Category");

            // Xử lý sự kiện nhấn nút quay lại trực tiếp trên Toolbar
            toolbar.setNavigationOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
        }

        // Xử lý sự kiện nhấn FloatingActionButton
        FloatingActionButton fabAddCategory = view.findViewById(R.id.fabAddCategory);
        fabAddCategory.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(getId(), AddCategoryFragment.newInstance());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ExpenseCategoryViewModel.class);

        // Lấy userID và lấy dữ liệu danh mục
        User currentUser = UserPreferences.getUser(requireContext());
        if (currentUser != null) {
            userId = currentUser.getUserID();

            // Sử dụng SharedPreferences để kiểm tra xem danh mục mặc định đã được chèn chưa
            SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String prefKey = "default_categories_inserted_" + userId;
            boolean isDefaultCategoriesInserted = prefs.getBoolean(prefKey, false);

            if (!isDefaultCategoriesInserted) {
                // Nếu chưa chèn danh mục mặc định, chèn các danh mục mặc định
                for (String categoryName : DEFAULT_CATEGORIES) {
                    ExpenseCategory category = new ExpenseCategory();
                    category.setCategoryName(categoryName);
                    category.setUserID(userId);
                    categoryRepository.insertExpenseCategory(category);
                }
                // Đánh dấu rằng danh mục mặc định đã được chèn
                prefs.edit().putBoolean(prefKey, true).apply();
            }

            // Lấy danh sách danh mục và cập nhật UI
            viewModel.fetchCategoriesWithCount(userId);
            viewModel.getCategoriesWithCountLiveData().observe(getViewLifecycleOwner(), categories -> {
                adapter.setCategoryList(categories);
                // Kiểm tra nếu danh sách rỗng và chưa hiển thị dialog trong phiên làm việc này
                if (categories.isEmpty() && !hasShownEmptyDialogInSession) {
                    // Đặt cờ để ngăn hiển thị lại trong cùng phiên làm việc
                    hasShownEmptyDialogInSession = true;

                    // Hiển thị dialog
                    new AlertDialog.Builder(requireContext())
                            .setTitle("No Categories")
                            .setMessage("No categories available. Add a new category.")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Chuyển đến AddCategoryFragment
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(getId(), AddCategoryFragment.newInstance());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false) // Ngăn người dùng đóng dialog bằng nút Back
                            .show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Xử lý nút quay lại (dự phòng)
            getParentFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}