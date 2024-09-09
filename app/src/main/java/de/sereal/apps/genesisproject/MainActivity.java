package de.sereal.apps.genesisproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.*;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.button:
                final Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                break;

            case R.id.buttonLoad:
                final Intent loadIntent = new Intent(this, GameActivity.class);
                loadIntent.putExtra("savegame","test");
                startActivity(loadIntent);
                break;
        }
    }
}
