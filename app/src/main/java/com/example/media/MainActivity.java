

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

    private boolean isVideoMode = false;

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

        if (uri == null) {
            showToast("No file selected");
            return;
        }

        String type = getContentResolver().getType(uri);

        if (type == null) {
            showToast("Unsupported file");
            return;
        }

        if (type.startsWith("video/")) {
            playVideo(uri);
        } else if (type.startsWith("audio/")) {
            playAudio(uri);
        } else {
            showToast("Please select an audio or video file");
        }
    }

    private void playAudio(Uri uri) {

        releaseAudioPlayer();

        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                showToast("Playing Audio");
            });

            mediaPlayer.setOnCompletionListener(mp ->
                    showToast("Audio Finished"));

            mediaPlayer.prepareAsync();

            isVideoMode = false;

        } catch (Exception e) {
            showToast("Unable to play audio");
        }
    }

    private void playVideo(Uri uri) {

        releaseAudioPlayer();

        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            showToast("Playing Video");
        });

        videoView.setOnCompletionListener(mp ->
                showToast("Video Finished"));

        isVideoMode = true;
    }

    private void playMedia() {

        try {

            if (isVideoMode) {

                if (!videoView.isPlaying()) {
                    videoView.start();
                }

            } else if (mediaPlayer != null) {

                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }

        } catch (Exception e) {
            showToast("Play failed");
        }
    }

    private void pauseMedia() {

        try {

            if (isVideoMode) {

                if (videoView.isPlaying()) {
                    videoView.pause();
                }

            } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                mediaPlayer.pause();
            }

        } catch (Exception e) {
            showToast("Pause failed");
        }
    }

    private
