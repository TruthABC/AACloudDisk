package hk.hku.cs.aaclouddisk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;

public class LoginActivity extends AppCompatActivity {

    //Local State
    SharedPreferences sharedPreferences;

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

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        initViews();
        initToolBar();
        initEvents();
        initFinal();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEditTextId.setText("");
        mEditTextPassword.setText("");
        mEditTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mEditTextPassword.setTypeface(Typeface.DEFAULT);
        loadAndSetCheckBoxState();
        tryRecoverUserInput();
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
        //see password
        mImageViewSeePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSeePw();
            }
        });
        //login button logic
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySaveUserInput();
                doAuthentication(mEditTextId.getText().toString(), mEditTextPassword.getText().toString());
            }
        });
        //checkboxes check logic
        mCheckBoxPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {   //if not "remember me", then must not "auto login"
                    mCheckBoxLogin.setChecked(false);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("RememberMe", mCheckBoxPassword.isChecked());
                editor.putBoolean("AutoLogin", mCheckBoxLogin.isChecked());
                editor.commit();
            }
        });
        mCheckBoxLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {   //if "auto login", then must "remember me"
                    mCheckBoxPassword.setChecked(true);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("RememberMe", mCheckBoxPassword.isChecked());
                editor.putBoolean("AutoLogin", mCheckBoxLogin.isChecked());
                editor.commit();
            }
        });
    }

    private void initFinal() {
        tryAutoLogin();
    }

    /**
     *  To auto log user in, called in onCreate last
     *   depends on "Auto Login"
     */
    private void tryAutoLogin() {
        if (sharedPreferences.getBoolean("AutoLogin", true)) {
            String id = sharedPreferences.getString("id","");
            String password = sharedPreferences.getString("password","");
            if (id != null && password != null && id.length()>0 && password.length()>0) {
                doAuthentication(id, password);
            }
        }
    }

    /**
     *  To auto fill id and password, called in onResume
     */
    private void tryRecoverUserInput() {
        mEditTextId.setText(sharedPreferences.getString("id",""));
        mEditTextPassword.setText(sharedPreferences.getString("password",""));
    }

    /**
     *  To cache user id and password, called when login clicked
     *   depends on Remember Me
     */
    private void trySaveUserInput() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (mCheckBoxPassword.isChecked()) {
            editor.putString("id", mEditTextId.getText().toString());
            editor.putString("password", mEditTextPassword.getText().toString());
        } else {
            editor.putString("id", "");
            editor.putString("password", "");
        }
        editor.commit();
    }

    /**
     * Load Local state of Remember me and Auto login
     */
    public void loadAndSetCheckBoxState() {
        //get check state
        boolean rememberMeState = sharedPreferences.getBoolean("RememberMe", true);
        boolean autoLoginState = sharedPreferences.getBoolean("AutoLogin", true);

        //set check state
        mCheckBoxPassword.setChecked(rememberMeState);
        mCheckBoxLogin.setChecked(autoLoginState);
    }

    //Visibility of password
    private boolean passwordSeeFlag = false;
    private void toggleSeePw() {
        //Toggle Visibility
        if (!passwordSeeFlag) {
            mEditTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mEditTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mEditTextPassword.setTypeface(Typeface.DEFAULT);
        }
        passwordSeeFlag = !passwordSeeFlag;

        //Focus
        mEditTextPassword.clearFocus();
        mEditTextPassword.requestFocus();
        //Index to Last Word Input
        mEditTextPassword.setSelection(mEditTextPassword.getText().toString().length());
    }

    //login button logic
    private void doAuthentication(final String id, final String password) {

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
                params.put("id", id);
                params.put("password", password);

                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

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
                                showToast("Login Failed: " + response.getErrmsg());
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

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
