package com.project.cem.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.repository.UserRepository;

public class RegisterViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> registerStatus = new MutableLiveData<>();

    // Constructor nhận Application context
    public RegisterViewModel(Application application) {
        super(application);  // Gọi constructor của AndroidViewModel với Application context
        userRepository = new UserRepository(application.getApplicationContext());  // Truyền Context vào UserRepository
    }

    // Phương thức xử lý đăng ký
    public void register(String email, String password) {
        // Kiểm tra dữ liệu đầu vào
        if (email.isBlank() || password.isBlank()) {
            registerStatus.setValue(false);
            return;
        }

        // Gọi phương thức register từ UserRepository
        boolean success = userRepository.register(email, password);

        // Cập nhật trạng thái đăng ký
        registerStatus.setValue(success);
    }

    // Lấy LiveData của registerStatus
    public LiveData<Boolean> getRegisterStatus() {
        return registerStatus;
    }
}
