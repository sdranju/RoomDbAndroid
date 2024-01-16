package com.ranju.roomdbapp.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ranju.roomdbapp.Database.DbConfig;
import com.ranju.roomdbapp.Model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM user_table WHERE login_id = :userId")
    User getUserByLoginId(String userId);

    @Query("SELECT * FROM " + DbConfig.USER_TABLE)
    List<User> getAllUsers();

}
