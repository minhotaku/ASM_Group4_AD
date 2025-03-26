package com.project.cem.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.utils.SampleDataInitializer;
import com.project.cem.utils.UserPreferences;
import com.project.cem.utils.ValidationUtils;
import com.project.cem.viewmodel.LoginViewModel;


public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView textViewMoveRegister;
    private Button btnLogin;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Kiem tra thong tin nguoi dung khi dang nhap
        if(UserPreferences.getUser(this)!=null){
            navigateStudentHome();
        }


        // Khởi tạo dữ liệu mẫu
//        SampleDataInitializer sampleDataInitializer = new SampleDataInitializer(this);
//        sampleDataInitializer.initializeSampleData();

        setContentView(R.layout.activity_login);

        // Khởi tạo các view
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        btnLogin = findViewById(R.id.btnRegister);
        textViewMoveRegister = findViewById(R.id.textViewMoveLogin);
        // Khởi tạo LoginViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Xử lý sự kiện nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if(!ValidationUtils.isValidEmail(email)){
                Toast.makeText(LoginActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!ValidationUtils.isValidPassword(password)){
                Toast.makeText(LoginActivity.this, "Mật khẩu phải có ít nhất 5 ký tự, bao gồm chữ và số", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi phương thức login trong ViewModel
            loginViewModel.login(email, password);
        });


        textViewMoveRegister.setOnClickListener(v -> {
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
                Toast.makeText(LoginActivity.this, "Sai mật khẩu hoặc tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void navigateStudentHome(){
        Intent intent = new Intent(this, MainActivity.class);
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
