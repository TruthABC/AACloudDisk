package hk.hku.cs.aaclouddisk.main.tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;

public class MP3Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_mp3, container, false);

        initViews(rootView);
        initEvents();
        initFinal();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hint: only ONE onCreateView & onResume is called even with multiple "Tab Switches"
        Log.i("shijian", "onMp3FragmentResume");
    }

    private void initViews(View v) {

    }

    private void initEvents() {

    }

    private void initFinal() {
        Log.i("shijian", "onMp3FragmentCreated");
        // Initialize music list at very beginning, even user not switched to this tab.
        ((MainActivity)getActivity()).getMP3InfoListAndResetAdaptor();
    }

}
