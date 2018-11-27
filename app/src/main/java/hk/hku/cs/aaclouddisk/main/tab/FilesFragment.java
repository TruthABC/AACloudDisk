package hk.hku.cs.aaclouddisk.main.tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import hk.hku.cs.aaclouddisk.R;

public class FilesFragment extends Fragment {

    private ListView mListViewFiles;
//    private FileInfoListAdapter fileInfoListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_files, container, false);

        initViews(rootView);
        initEvents();
        initFinal();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void initViews(View v) {
        mListViewFiles = (ListView) getActivity().findViewById(R.id.list_view_files);
    }

    private void initEvents() {

    }

    private void initFinal() {

    }

}