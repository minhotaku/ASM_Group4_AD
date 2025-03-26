package com.project.cem.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.utils.ValidationUtils;
import com.project.cem.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtRegisterEmail, edtRegisterPassword, edtConfirmedPassword;
    private Button btnRegister;
    private TextView textViewMoveLogin;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo view
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmedPassword = findViewById(R.id.edtConfirmedPassword);
        btnRegister = findViewById(R.id.btnRegister);
        textViewMoveLogin = findViewById(R.id.textViewMoveLogin);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        btnRegister.setOnClickListener(v -> {
            String email = edtRegisterEmail.getText().toString();
            String password = edtRegisterPassword.getText().toString();
            String confirmPassword = edtConfirmedPassword.getText().toString();
            if(!ValidationUtils.isValidEmail(email)){
                Toast.makeText(RegisterActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!ValidationUtils.isValidPassword(password)){
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 5 ký tự, bao gồm chữ và số", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!confirmPassword.equals(password)){
                Toast.makeText(RegisterActivity.this, "Mật khẩu không trùng nhau", Toast.LENGTH_SHORT).show();
                return;
            }
            registerViewModel.register(email, password);
        });


        textViewMoveLogin.setOnClickListener(v -> {
            navigateLogin();
        });


        registerViewModel.getRegisterStatus().observe(this,success->{
            if(success){
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void navigateLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }
}