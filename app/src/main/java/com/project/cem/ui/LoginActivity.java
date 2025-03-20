package com.project.cem.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.project.cem.R;
import com.project.cem.utils.SampleDataInitializer;
import com.project.cem.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnNavigateRegister;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo dữ liệu mẫu
        SampleDataInitializer sampleDataInitializer = new SampleDataInitializer(this);
        sampleDataInitializer.initializeSampleData();

        setContentView(R.layout.activity_login);

        // Khởi tạo các view
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNavigateRegister = findViewById(R.id.btnNavigateRegister);

        // Khởi tạo LoginViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Xử lý sự kiện nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                // Check email hoặc password có trống hay không
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi phương thức login trong ViewModel
            loginViewModel.login(email, password);
        });


        btnNavigateRegister.setOnClickListener(v -> {
            navigateRegister();
        });

        // Lắng nghe kết quả đăng nhập từ LoginViewModel
        loginViewModel.getLoginStatus().observe(this, user -> {
            if (user != null) {
                // Kiểm tra Role
                if (user.getRole().equals("user")) {
                    navigateStudentHome(); // Chuyển hướng đến StudentHomeActivity
                } else if (user.getRole().equals("admin")) {
                    navigateAdminHome(); // Chuyển hướng đến AdminHomeActivity
                }
            } else {
                // Đăng nhập thất bại
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void navigateStudentHome(){
        Intent intent = new Intent(this, StudentHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateAdminHome(){
//        Intent intent = new Intent(this,AdminHomeActivity.class);
//        startActivity(intent);
//        finish();
    }
    private void navigateRegister(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
