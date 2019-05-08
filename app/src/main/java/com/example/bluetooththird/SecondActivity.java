package com.example.bluetooththird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class SecondActivity extends Activity {

    Button acti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_window);
        acti= (Button) findViewById(R.id.callActivity);
    }

    public void onButtonClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("getMessage", "giveMeMoney");
        startActivityForResult( intent, 101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101)
        {
            String str= data.getExtras().getString("answer");
            acti.setText(str);

        }
    }
}
