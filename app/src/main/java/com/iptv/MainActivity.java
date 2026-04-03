package com.iptv;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    PlayerView playerView;
    ExoPlayer player;

    TextView numberText, bufferText;

    ArrayList<Channel> channels = new ArrayList<>();
    int currentIndex = 0;

    String inputBuffer = "";
    Handler handler = new Handler();

    String M3U_URL = "https://iptv-org.github.io/iptv/index.m3u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.playerView);
        numberText = findViewById(R.id.channelNumber);
        bufferText = findViewById(R.id.bufferText);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        loadM3U();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if(state == Player.STATE_BUFFERING){
                    bufferText.setText("Buffering...");
                }
                if(state == Player.STATE_READY){
                    bufferText.setText("Playing");
                }
            }
        });
    }

    // 🔥 M3U LOAD
    void loadM3U(){
        new Thread(() -> {
            try {
                URL url = new URL(M3U_URL);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream())
                );

                String line;
                String name = "";
                int num = 1;

                while((line = reader.readLine()) != null){

                    if(line.startsWith("#EXTINF")){
                        name = line.split(",")[1];
                    }

                    else if(line.startsWith("http")){
                        Channel ch = new Channel();
                        ch.name = name;
                        ch.url = line;
                        ch.number = num++;

                        channels.add(ch);
                    }
                }

                runOnUiThread(() -> playCurrent());

            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    void playCurrent(){
        if(channels.size() == 0) return;

        MediaItem item = MediaItem.fromUri(channels.get(currentIndex).url);
        player.setMediaItem(item);
        player.prepare();
        player.play();

        numberText.setText("" + channels.get(currentIndex).number);
    }

    // 🔢 Remote number input
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9){
            int digit = keyCode - KeyEvent.KEYCODE_0;
            inputBuffer += digit;

            numberText.setVisibility(TextView.VISIBLE);
            numberText.setText(inputBuffer);

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> {
                playByNumber(inputBuffer);
                inputBuffer = "";
                numberText.setVisibility(TextView.GONE);
            }, 1500);

            return true;
        }

        // ⬆⬇ channel switch
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            currentIndex++;
            if(currentIndex >= channels.size()) currentIndex = 0;
            playCurrent();
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            currentIndex--;
            if(currentIndex < 0) currentIndex = channels.size()-1;
            playCurrent();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    void playByNumber(String num){
        int n = Integer.parseInt(num);

        for(int i=0;i<channels.size();i++){
            if(channels.get(i).number == n){
                currentIndex = i;
                playCurrent();
                break;
            }
        }
    }
}
