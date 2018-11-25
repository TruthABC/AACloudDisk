package hk.hku.cs.aaclouddisk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    //Local Data
    private SharedPreferences sharedPreferences;

    //View Widgets
    private Button mBtnLogin;
    private Button mBtnRegister;
    private EditText mEditTextId;
    private EditText mEditTextPassword;
    private CheckBox mCheckBoxPassword;
    private CheckBox mCheckBoxLogin;
    private ImageView mImageViewSeePw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initToolBar();
        initEvents();
        initFinal();
    }

    private void initViews() {
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnRegister = findViewById(R.id.btn_register);
        mEditTextId = findViewById(R.id.et_account);
        mEditTextPassword = findViewById(R.id.et_password);
        mCheckBoxPassword = findViewById(R.id.checkBox_password);
        mCheckBoxLogin = findViewById(R.id.checkBox_login);
        mImageViewSeePw = findViewById(R.id.iv_see_password);
    }

    private void initToolBar() {

    }

    private void initEvents() {
        //login button logic
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAuthentication();
            }
        });
    }

    private void initFinal() {

    }

    //login button logic
    private void doAuthentication() {
        String id = mEditTextId.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        //should not be empty
        if (id.equals("")) {
            showToast("Account cannot be empty");
            return;
        }
        if (password.equals("")) {
            showToast("Password cannot be empty");
            return;
        }

        //show loading box
//                showLoading();

        //Use another thread to do server authentication
        Thread loginRunnable = new Thread() {
            @Override
            public void run() {
                super.run();
                //Set button non-clickable
                mBtnLogin.setClickable(false);
                mBtnRegister.setClickable(false);

                //Authentication by id and password TODO: here http request and callback !
//                        if (getAccount().equals("csdn") && getPassword().equals("123456")) {
//                            showToast("登录成功");
//                            //applyCheckBoxState();//记录下当前用户记住密码和自动登录的状态;
//                            loginAndForward();
//                        } else {
//                            showToast("Authentication Failed");
//                        }

                //Set button back to clickable
                mBtnLogin.setClickable(true);
                mBtnRegister.setClickable(true);

                //hide loading box
//                        hideLoading();
            }
        };
        loginRunnable.start();

        //go to main page
        loginAndForward();
    }

    private void loginAndForward() {
        //go to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivityForResult(intent, 0);
        finish();//close this page
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
