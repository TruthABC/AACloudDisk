package hk.hku.cs.aaclouddisk.main.tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import hk.hku.cs.aaclouddisk.R;

public class MeFragment extends Fragment {

    private Button mBtnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_me, container, false);

        initViews(rootView);
        initEvents();
        initFinal();

        return rootView;
    }

    private void initViews(View v) {
        mBtnLogout = v.findViewById(R.id.btn_logout);
    }

    private void initEvents() {
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAndForward();
            }
        });
    }

    private void initFinal() {

    }

    private void logoutAndForward() {
        getActivity().finish();
    }
}
