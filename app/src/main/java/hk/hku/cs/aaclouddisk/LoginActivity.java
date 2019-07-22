package hk.hku.cs.aaclouddisk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;

public class LoginActivity extends AppCompatActivity {

    //Tag
    public static final String TAG = "LoginActivity";
    public static final String DEBUG_TAG = "shijian";

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

    //Http Response handler
    private LoginActivityHandler mLoginActivityHandler = new LoginActivityHandler(LoginActivity.this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);//TODO: Rejecting re-init on previously-failed class
        Log.v(TAG, "onCreate");

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        initViews();
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
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mEditTextId = (EditText) findViewById(R.id.et_account);
        mEditTextPassword = (EditText) findViewById(R.id.et_password);
        mCheckBoxPassword = (CheckBox) findViewById(R.id.checkBox_password);
        mCheckBoxLogin = (CheckBox) findViewById(R.id.checkBox_login);
        mImageViewSeePw = (ImageView) findViewById(R.id.iv_see_password);
    }

    private void initEvents() {
        //see password
        mImageViewSeePw.setOnClickListener((v) -> {
            toggleSeePw();
        });
        //login button logic
        mBtnLogin.setOnClickListener((v) -> {
            trySaveUserInput();
            doAuthentication(mEditTextId.getText().toString(), mEditTextPassword.getText().toString());
        });
        //register button logic
        mBtnRegister.setOnClickListener((v) -> {
            doRegister(mEditTextId.getText().toString(), mEditTextPassword.getText().toString());
        });
        //checkboxes check logic
        mCheckBoxPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {   //if not "remember me", then must not "auto login"
                mCheckBoxLogin.setChecked(false);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("RememberMe", mCheckBoxPassword.isChecked());
            editor.putBoolean("AutoLogin", mCheckBoxLogin.isChecked());
            editor.commit();
        });
        mCheckBoxLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {   //if "auto login", then must "remember me"
                mCheckBoxPassword.setChecked(true);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("RememberMe", mCheckBoxPassword.isChecked());
            editor.putBoolean("AutoLogin", mCheckBoxLogin.isChecked());
            editor.commit();
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
    private void doAuthentication(String id, String password) {

        //should not be empty
        if (id.equals("")) {
            showToast("Account cannot be empty");
            return;
        }
        if (password.equals("")) {
            showToast("Password cannot be empty");
            return;
        }

        //Set button non-clickable
        mBtnLogin.setClickable(false);
        mBtnRegister.setClickable(false);

        //Use another thread to do server authentication
        Thread loginRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/login";
            Map<String, String> params = new HashMap<>();
            params.put("id", id);
            params.put("password", password);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mLoginActivityHandler.obtainMessage();
            msg.what = LoginActivityHandler.LOGIN_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mLoginActivityHandler.sendMessage(msg);
        });
        loginRunnable.start();
    }

    //register button logic
    private void doRegister(String id, String password) {

        //should not be empty
        if (id.equals("")) {
            showToast("Account cannot be empty");
            return;
        }
        if (password.equals("")) {
            showToast("Password cannot be empty");
            return;
        }

        //Set button non-clickable
        mBtnLogin.setClickable(false);
        mBtnRegister.setClickable(false);

        //Use another thread to do server authentication
        Thread registerRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/register";
            Map<String, String> params = new HashMap<>();
            params.put("id", id);
            params.put("password", password);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mLoginActivityHandler.obtainMessage();
            msg.what = LoginActivityHandler.REGISTER_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mLoginActivityHandler.sendMessage(msg);
        });
        registerRunnable.start();
    }

    private void loginAndForward() {
        //save its login timestamp
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastSuccessLogin", new Date().getTime());
        editor.putString("lastSuccessId", mEditTextId.getText().toString());
        editor.putString("lastSuccessPassword", mEditTextPassword.getText().toString());
        editor.commit();
        //go to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivityForResult(intent, 0);
    }

    private void showToast(final String msg) {
        runOnUiThread(() -> {
            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * The Thread Message Handler (Should be "static", so "inner" class)
     *  Eg. handling HTTP responses
     *  Hint: Instances of static inner classes do not hold an implicit reference to their outer class.
     *  Hint: In Java, non-static inner and anonymous classes hold an implicit reference to their outer class.
     *        Static inner classes, on the other hand, do not.
     */
    private static class LoginActivityHandler extends Handler {
        //Message::what
        private static final int LOGIN_RESP = 0x11;
        private static final int REGISTER_RESP = 0x21;

        private final WeakReference<LoginActivity> mActivityRef;

        public LoginActivityHandler(LoginActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //Context
            LoginActivity activity = mActivityRef.get();
            if (activity == null) {
                // cannot be here
                Log.e(DEBUG_TAG, "LoginActivityHandler 'activity == null'");
                return;
            }

            //Which Message switch:
            if (msg.what == LOGIN_RESP){
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //Authentication result
                    if (response.getErrcode() == 0){
                        activity.showToast("Login Successful");
                        //go to main page
                        activity.loginAndForward();
                    } else {
                        activity.showToast("Login Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                    long lastSuccessLogin = activity.sharedPreferences.getLong("lastSuccessLogin", 0);
                    String lastSuccessId = activity.sharedPreferences.getString("lastSuccessId", "~!@#$%^&*()_+");
                    String lastSuccessPassword = activity.sharedPreferences.getString("lastSuccessPassword", "~!@#$%^&*()_+");
                    if (lastSuccessId.equals(activity.mEditTextId.getText().toString()) && lastSuccessPassword.equals(activity.mEditTextPassword.getText().toString())) {
                        long restHour = 24 - ((new Date().getTime() - lastSuccessLogin) / 1000 / 60 / 60);
                        if (restHour >= 0) {
                            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
                            normalDialog.setIcon(R.drawable.round_wifi_off_black_36);
                            normalDialog.setTitle("Network Error");
                            normalDialog.setMessage("Enter Offline Mode? (available in " + restHour + " hours)");
                            normalDialog.setPositiveButton("Enter", (dialog, which) -> {
                                activity.loginAndForward();
                            });
                            normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
                                //do nothing
                            });
                            normalDialog.show();
                        }
                    }
                }

                //Set button back to clickable
                activity.mBtnLogin.setClickable(true);
                activity.mBtnRegister.setClickable(true);
            } else if (msg.what == REGISTER_RESP){
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //register result
                    if (response.getErrcode() == 0){
                        activity.showToast("Register Successful");
                        activity.mEditTextId.setText("");
                        activity.mEditTextPassword.setText("");
                    } else {
                        activity.showToast("Register Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }

                //Set button back to clickable
                activity.mBtnLogin.setClickable(true);
                activity.mBtnRegister.setClickable(true);
            }
        }
    }
}