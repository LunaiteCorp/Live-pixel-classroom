package com.lunaite.jnesta;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view)
    {
        Intent intent = new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, Wallpaper.class));
        startActivity(intent);
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
}
