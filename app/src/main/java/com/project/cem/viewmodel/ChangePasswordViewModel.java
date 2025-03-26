package com.project.cem.viewmodel;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.project.cem.model.User;
import com.project.cem.repository.UserRepository;
import com.project.cem.utils.UserPreferences;
import com.project.cem.utils.ValidationUtils;
import com.project.cem.utils.ValidationUtils;
public class ChangePasswordViewModel extends AndroidViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<String>  errorMessage = new MutableLiveData<>(); // Lỗi trong quá trình nhập mật khẩu
    private final MutableLiveData<String> isPasswordChanged = new MutableLiveData<>(); // Lưu trạng thái thay đổi mật khẩu

    public ChangePasswordViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());

    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if(oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
            errorMessage.setValue("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        if(ValidationUtils.isValidPassword(newPassword) == false){
            errorMessage.setValue("Mật khẩu phải có ít nhất 5 ký tự, bao gồm chữ và số");
            return;
        }

        if(oldPassword.equals(newPassword)){
            errorMessage.setValue("Mật khẩu mới không được trùng với mật khẩu cũ");
            return;
        }

        if(!newPassword.equals(confirmPassword)){
            errorMessage.setValue("Mật khẩu mới và mật khẩu xác nhận không khớp");
            return;
        }

        User user = UserPreferences.getUser(getApplication().getApplicationContext());
        if(user == null){
            errorMessage.setValue("Người dùng không hợp lệ");
            return;
        }

        if(userRepository.login(user.getEmail(), oldPassword) == null){
            errorMessage.setValue("Mật khẩu cũ không đúng");
            return;
        }

        if(userRepository.changePassword(user.getUserID(), newPassword)){
            isPasswordChanged.setValue("Mật khẩu đã được thay đổi thành công");
        }else{
            errorMessage.setValue("Lỗi khi thay đổi mật khẩu");
        }

    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;


    }

    public LiveData<String> getIsPasswordChanged() {
        return isPasswordChanged;
    }




}
