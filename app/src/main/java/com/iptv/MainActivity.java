package com.iptv;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    PlayerView playerView;
    ExoPlayer player;

    String M3U_URL = "https://iptv-org.github.io/iptv/index.m3u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.playerView);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        loadFirstChannel();
    }

    void loadFirstChannel(){
        new Thread(() -> {
            try {
                URL url = new URL(M3U_URL);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream())
                );

                String line;

                while((line = reader.readLine()) != null){
                    if(line.startsWith("http")){
                        playStream(line);
                        break; // first channel hi load karna
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    void playStream(String url){
        runOnUiThread(() -> {
            MediaItem item = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(item);
            player.prepare();
            player.play();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
