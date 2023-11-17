    package com.example.app8.UIStudent;

    import android.content.Context;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.ExpandableListView;
    import android.widget.ListView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;

    import com.dvinfosys.model.HeaderModel;
    import com.dvinfosys.ui.NavigationListView;
    import com.example.app8.Login.AdminLogin;
    import com.example.app8.Login.LecturerLogin;
    import com.example.app8.Login.StudentLogin;
    import com.example.app8.NavigationDrawer.Common;
    import com.example.app8.NavigationDrawer.SettingsActivity;
    import com.example.app8.R;
    import com.example.app8.SQLServer.SQLConnection;
    import com.example.app8.UIAdmin.StudentListActivity;
    import com.example.app8.UIClass.ClassAddActivity;
    import com.example.app8.UIClass.CustomClassListAdapter;
    import com.example.app8.UIClass.CustomClassListAdapterForStudent;
    import com.google.android.material.navigation.NavigationView;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.ArrayList;

    public class StudentActivity extends AppCompatActivity
            implements NavigationView.OnNavigationItemSelectedListener{
        private DrawerLayout drawer;
        private ActionBarDrawerToggle toggle;
        private ListView listView;
        private NavigationListView expandable_navigation;
        private Context context;
        private ArrayList<String> accountList;
        private ArrayList<Integer> backgroundList;
        private CustomClassListAdapterForStudent adapter;
        private Connection connection;
        private static final int ADD_ACCOUNT_REQUEST = 1;
        private TextView role, toolbar;
        private TextView adminName;
        private static String codeStudent;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.class_list_activity);

            // Tìm thẻ toolbar và TextView có id là toolbarTitle2
            Toolbar toolbar = findViewById(R.id.toolbar_layout);
            TextView toolbarTitle = toolbar.findViewById(R.id.toolbarTitle2);
            toolbarTitle.setText("UI Student");
            setSupportActionBar(toolbar);

            context = StudentActivity.this;


            drawer = findViewById(R.id.drawer_layout);
            listView = findViewById(R.id.listView);
            expandable_navigation = findViewById(R.id.expandable_navigation);
            NavigationView navigationView = findViewById(R.id.nav_view);
            View header= navigationView.getHeaderView(0);
            role= header.findViewById(R.id.role);
            role.setText("Student");
            Intent intent = getIntent();
            if (intent.hasExtra("CODE_STUDENT_KEY")) {
                codeStudent = intent.getStringExtra("CODE_STUDENT_KEY");
                String username = getUsernameFromCodeStudent(codeStudent);
                adminName = header.findViewById(R.id.adminName);
              //  adminName.setText(username.toUpperCase());
            }
            navigationView.setNavigationItemSelectedListener(this);

            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Trong phương thức onItemClick
                    int classId = getClassIdForPosition(position);
                    if (classId != -1) {
                        Intent intent = new Intent(StudentActivity.this, StudentListForStudent.class);
                        intent.putExtra("ClassId", classId); // Truyền class ID
                        startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                    }
                }
            });

            expandable_navigation.init(this)
                    .addHeaderModel(new HeaderModel("Home"))
                    .addHeaderModel(new HeaderModel("Log out"))
                    .build()
                    .addOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                            expandable_navigation.setSelected(groupPosition);

                            //drawer.closeDrawer(GravityCompat.START);
                            if (id == 0) {
                                //Home Menu
                                Common.showToast(context, "Home Select");
                                Intent intent = new Intent(v.getContext(), StudentActivity.class);
                                startActivity(intent);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                             else if (id == 1) {
                                //Wishlist Menu
                                Common.showToast(context, "Log out Selected");
                                Intent intent = new Intent(v.getContext(), AdminLogin.class);
                                startActivity(intent);
                                finish();
                            }
                            return false;
                        }
                    });
            //listView.expandGroup(2);


            accountList = new ArrayList<>();
            backgroundList = new ArrayList<>();

            adapter = new CustomClassListAdapterForStudent(this, accountList, backgroundList);
            listView.setAdapter(adapter);

            connection = SQLConnection.getConnection();

            if (connection != null) {
                loadAccountData(codeStudent);
            } else {
                Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }




            Button addAccountButton = findViewById(R.id.addAccountButton);
            addAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StudentActivity.this, UI_Student.class);
                    intent.putExtra("CODE_STUDENT_KEY", codeStudent); // Truyền codeStudent
                    startActivity(intent);
                }
            });


        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                Intent intent = new Intent(StudentActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

//            if(id == R.id.Home) {
//                Intent intent = new Intent(this, ClassListActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.Recognize) {
//                Intent intent = new Intent(this, RecognizeActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.Logout) {
//                Intent intent = new Intent(this, LoginActivity.class);
//                startActivity(intent);
//            }
       /* if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        private void loadAccountData(String studentCode) {
            accountList.clear();
            backgroundList.clear();

            String query = "SELECT [name_class], [name_subject], [background] FROM [PROJECT].[dbo].[CLASS] WHERE id IN (SELECT classid FROM THAMGIA WHERE code_student = ?)";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, studentCode);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String name_class = resultSet.getString("name_class");
                    String name_subject = resultSet.getString("name_subject");
                    int backgroundValue = resultSet.getInt("background");

                    String accountInfo = name_subject + "\n" + name_class;
                    accountList.add(accountInfo);
                    backgroundList.add(backgroundValue);
                }
                resultSet.close();
                preparedStatement.close();
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
                loadAccountData(codeStudent);
            }
        }

        private int getStudentCountForClass(int classId) {
            String query = "SELECT COUNT(*) AS studentCount FROM THAMGIA WHERE classid = ?";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, classId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("studentCount");
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }
        private int getClassIdForPosition(int position) {
            if (connection != null && position >= 0 && position < accountList.size()) {
                String selectedSubject = accountList.get(position).split("\n")[0];
                String query = "SELECT [id] FROM [PROJECT].[dbo].[CLASS] WHERE [name_subject] = ?";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, selectedSubject);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return -1; // Trả về -1 nếu không tìm thấy class ID
        }
        @Override
        protected void onResume() {
            super.onResume();
                loadAccountData(codeStudent);
        }
        @Override
        public void onBackPressed() {
            // Tạo một Intent để quay lại ClassListActivity
            Intent intent = new Intent(this, StudentLogin.class);
            startActivity(intent);
            finish(); // Kết thúc StudentListActivity để ngăn nó quay lại sau khi đã chuyển về ClassListActivity.
        }
        private String getUsernameFromCodeStudent(String codeStudent) {
            String username = null;

            if (connection != null) {
                try {
                    // Truy vấn SQL để lấy 'username' từ 'code_student'
                    String query = "SELECT username FROM STUDENT_LIST WHERE code_student = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, codeStudent);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    // Kiểm tra xem có dữ liệu trả về không
                    if (resultSet.next()) {
                        username = resultSet.getString("username");
                    }

                    resultSet.close();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return username;
        }


    }
