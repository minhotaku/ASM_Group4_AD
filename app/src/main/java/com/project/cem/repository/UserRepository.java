package com.project.cem.repository;

import android.content.Context;

import com.project.cem.dao.UserDao;
import com.project.cem.model.User;
import com.project.cem.utils.SQLiteHelper;

public class UserRepository {

    private SQLiteHelper sqLiteHelper;
    private UserDao userDao;

    // Constructor để khởi tạo SQLiteHelper và FirestoreHelper
    public UserRepository(Context context) {
        sqLiteHelper = new SQLiteHelper(context); // Khởi tạo SQLiteHelper với context
        userDao = new UserDao(context);
    }

    public User login(String email, String password){
        // Truy vấn dữ liệu từ SQLLite thông qua UserDao
        User user = userDao.getUserByEmailAndPassword(email, password);
        if (user != null) {
            return user;
        }
        return null; // Trả về null nếu đăng nhập thất bại
    }


    public boolean register(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("user");
        long id = userDao.insertUser(user);
        if(id != -1) {
            return true;
        }
        return false;

    }
}
