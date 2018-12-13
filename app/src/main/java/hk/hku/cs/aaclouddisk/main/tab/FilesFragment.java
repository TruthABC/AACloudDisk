package hk.hku.cs.aaclouddisk.main.tab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;

public class FilesFragment extends Fragment {

    private TextView mCreateNewFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_files, container, false);

        initViews(rootView);
        initEvents();
        initFinal();

        return rootView;
    }

    private void initViews(View v) {
        mCreateNewFolder = v.findViewById(R.id.new_folder_text);
    }

    private void initEvents() {
        mCreateNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCreateNewFolder();
            }
        });
    }

    private void doCreateNewFolder() {
        //get user id
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id", "");

        //Use another thread to do server work
        Thread nreFolderRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/create_folder";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", ((MainActivity)getActivity()).lastRelativePath);


                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x18;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x18){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //result
                            if (response.getErrcode() == 0){
                                showToast("Create Folder Successful");
                                ((MainActivity)getActivity()).getFileInfoListAndResetAdaptor(((MainActivity)getActivity()).lastRelativePath);
                            } else {
                                showToast("Create Folder Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }
                    }
                }
            };
        };
        nreFolderRunnable.start();
    }

    private void initFinal() {
        String lastRelativePath = ((MainActivity)getActivity()).lastRelativePath;
        ((MainActivity)getActivity()).getFileInfoListAndResetAdaptor(lastRelativePath);
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}