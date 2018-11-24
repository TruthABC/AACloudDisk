package hk.hku.cs.aaclouddisk.tasklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import hk.hku.cs.aaclouddisk.R;

public class TaskListActivity extends AppCompatActivity {

    ImageButton mBackButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        initViews();
        initToolBar();
    }

    private void initViews() {
        mBackButton = (ImageButton) findViewById(R.id.task_list_back);
    }

    private void initToolBar() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
