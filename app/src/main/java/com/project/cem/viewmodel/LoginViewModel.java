package com.project.cem.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.User;
import com.project.cem.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> loginStatus = new MutableLiveData<>();

    // Constructor nhận Application context
    public LoginViewModel(Application application) {
        super(application);  // Gọi constructor của AndroidViewModel với Application context
        userRepository = new UserRepository(application.getApplicationContext());  // Truyền Context vào UserRepository
    }

    // Phương thức xử lý đăng nhập
    public void login(String email, String password) {
        // Gọi phương thức login từ UserRepository và trả về User nếu thành công
        User user = userRepository.login(email, password);

        // Nếu user != null, có nghĩa là đăng nhập thành công
        if (user != null) {
            loginStatus.setValue(user);
        } else {
            loginStatus.setValue(null);  // Đăng nhập thất bại
        }
    }

    // Lấy LiveData của loginStatus
    public LiveData<User> getLoginStatus() {
        return loginStatus;
    }
}
