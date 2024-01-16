package com.ranju.roomdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ranju.roomdbapp.DAO.UserDao;
import com.ranju.roomdbapp.Database.InitDb;
import com.ranju.roomdbapp.Model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText etLoginId;
    private EditText etPassword;
    private Button btnLogin;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the UserDao
        userDao = InitDb.appDatabase.userDao();

        // Initialize UI components
        etLoginId = findViewById(R.id.etLoginId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Set onClickListener for the login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // Initialize the UserDao
        userDao = InitDb.appDatabase.userDao();

        // Insert a test user for demonstration purposes
        insertTestUser();
    }

    private void login() {
        String loginId = etLoginId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(loginId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter login credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        // Execute the database query on a background thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final User user = userDao.getUserByLoginId(loginId);

                // Handle the result on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null && password.equals(user.getPassword())) {
                            // Successful login, navigate to the main activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Invalid login credentials
                            Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    private void insertTestUser() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Check if the test user 'admin' already exists in the db
                if (userDao.getUserByLoginId("admin") == null) {
                    // Insert the test user
                    User testUser = new User();
                    testUser.setLoginId("admin");
                    testUser.setPassword("admin");  // Note: In a real application, passwords should be hashed
                    testUser.setFullName("Admin User");
                    testUser.setContact("admin@example.com");

                    userDao.insert(testUser);
                }
            }
        });
    }

}

/*
The error you're encountering indicates that you are trying to perform a database query on the main thread, which is not allowed in Room. To fix this issue, you should perform database operations on a background thread.

One way to achieve this is by using an AsyncTask to move the database operation off the main thread. However, starting from Android API level 30, AsyncTask is deprecated. An alternative is to use Kotlin Coroutines or the Executors class for background threading.
 */