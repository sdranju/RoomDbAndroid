# RoomDbAndroid
Step-by-Step How-To: Setting Up and Implementing Room Database
<br><br>
***Introduction***

SQLite and Room are both database solutions for Android applications, but they serve different purposes and offer different features. While SQLite is a powerful and widely-used database engine, Room offers a higher-level abstraction, better developer experience, and improved support for modern Android development practices. Room is particularly 
beneficial for developers who prefer an ORM approach, value compile-time safety, and want to take advantage of the Android Architecture Components.

Room database has some extra advantages which SQLite do not provide, such as:

**Compile-Time Verification:** Room provides compile-time verification of SQL queries. If there are any issues with your queries, the compiler catches them during the build process, reducing the chance of runtime errors.

**LiveData and RxJava Integration:** Room integrates seamlessly with LiveData and RxJava. This allows you to observe changes in the database and automatically update the UI when the data changes, making it easier to implement reactive UIs.

Let's explore the power of Room database in a simple login application.

![RoomDBApp](RoomDBApp.png)
<br><br>

***Step 1: Add Room Database Dependencies***

In your app-level build.gradle file, add the following dependencies:
```
implementation "androidx.room:room-runtime:2.6.1"
annotationProcessor "androidx.room:room-compiler:2.6.1"
```
<br>

***Step 2: Create the Entity Class***

Define the structure of your User entity class, representing the user profile information.
```java
@Entity(tableName = "user_table")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "login_id")
    public String loginId;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "contact")
    public String contact;
}

```
<br>

***Step 3: Create the DAO (Data Access Object)***

Define the Data Access Object interface to perform CRUD operations on the User entity.
```java
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

```
<br>

***Step 4: Create the Room Database***

Build the Room Database by extending RoomDatabase and include the DAO.
```java
@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DbConfig.ROOM_DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

```
<br>

***Step 5: Implement CRUD Operations in Your Application***

In your activities or fragments, use the UserDao methods to perform database operations.
<br><br>

***Step 6: Initialize the Database***

Initialize the database in your application class or the entry point of your app.
```java
public class InitDb extends Application {
    public static AppDatabase appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = AppDatabase.getDatabase(this);
    }
}

```
<br>

***Step 7: Create an Activity***

In this example, we're creating a login activity class to perform the database task. But one important thing you must notice, performing a database query on the main thread in Room is not allowed. To fix this issue, database operations must be performed on a background thread and you should use the Executors class or Kotlin Coroutines for background threading.


```java
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
```
<br>

Congratulations! You've successfully set up and implemented a Room database in your Android application. Feel free to adapt and expand upon this example based on your specific use case and requirements.

