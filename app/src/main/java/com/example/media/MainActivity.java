package com.example.media;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private EditText urlEditText;

    private boolean isVideoPlaying = false;

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    this::handleSelectedFile
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupVideoController();
        setupButtons();
    }

    private void initializeViews() {
        videoView = findViewById(R.id.videoView);
        urlEditText = findViewById(R.id.urlEditText);
    }

    private void setupVideoController() {
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
    }

    private void setupButtons() {

        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button btnOpenUrl = findViewById(R.id.btnOpenUrl);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnRestart = findViewById(R.id.btnRestart);

        btnOpenFile.setOnClickListener(v ->
                filePicker.launch("*/*"));

        btnOpenUrl.setOnClickListener(v -> {
            String url = urlEditText.getText().toString().trim();

            if (url.isEmpty()) {
                showToast("Enter a URL");
                return;
            }

            if (!Patterns.WEB_URL.matcher(url).matches()) {
                showToast("Invalid URL");
                return;
            }

            playVideo(Uri.parse(url));
        });

        btnPlay.setOnClickListener(v -> playMedia());

        btnPause.setOnClickListener(v -> pauseMedia());

        btnStop.setOnClickListener(v -> stopMedia());

        btnRestart.setOnClickListener(v -> restartMedia());
    }

    private void handleSelectedFile(Uri uri) {

        if (uri == null) return;

        String type = getContentResolver().getType(uri);

        if (type != null && type.startsWith("video")) {
            playVideo(uri);
        } else {
            playAudio(uri);
        }
    }

    private void playAudio(Uri uri) {

        releaseAudioPlayer();

        mediaPlayer = MediaPlayer.create(this, uri);

        if (mediaPlayer != null) {
            mediaPlayer.start();
            isVideoPlaying = false;
            showToast("Playing Audio");
        }
    }

    private void playVideo(Uri uri) {

        releaseAudioPlayer();

        videoView.setVideoURI(uri);
        videoView.start();

        isVideoPlaying = true;

        showToast("Playing Video");
    }

    private void playMedia() {

        if (isVideoPlaying) {
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void pauseMedia() {

        if (isVideoPlaying) {
            videoView.pause();
        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMedia() {

        if (isVideoPlaying) {
            videoView.stopPlayback();
        } else {
            releaseAudioPlayer();
        }
    }

    private void restartMedia() {

        if (isVideoPlaying) {
            videoView.seekTo(0);
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    private void releaseAudioPlayer() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAudioPlayer();
    }
}
