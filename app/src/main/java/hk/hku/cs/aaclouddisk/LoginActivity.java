package hk.hku.cs.aaclouddisk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;

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
        final String id = mEditTextId.getText().toString().trim();
        final String password = mEditTextPassword.getText().toString().trim();

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

        //Set button non-clickable
        mBtnLogin.setClickable(false);
        mBtnRegister.setClickable(false);

        //Use another thread to do server authentication
        Thread loginRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/login";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id",id);
                params.put("password",password);

                String response = HttpUtilsHttpURLConnection.getContextByHttp(url,params);

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x11;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x11){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //Authentication result
                            if (response.getErrcode() == 0){
                                showToast("Login Successful");
                                //go to main page
                                loginAndForward();
                            } else {
                                showToast("Login Failed");
                            }
                        } catch (JsonSyntaxException e) {
                            showToast("Network error, plz contact maintenance.");
                        }

                        //Set button back to clickable
                        mBtnLogin.setClickable(true);
                        mBtnRegister.setClickable(true);

                        //hide loading box
//                        hideLoading();
                    }
                }
            };
        };
        loginRunnable.start();
    }

    private void loginAndForward() {
        //go to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivityForResult(intent, 0);
//        finish();//close this page
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
