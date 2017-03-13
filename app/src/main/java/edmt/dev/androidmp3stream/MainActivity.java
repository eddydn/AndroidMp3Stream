package edmt.dev.androidmp3stream;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import dyanamitechetan.vusikview.VusikView;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnCompletionListener{

    private ImageButton btn_play_pause;
    private SeekBar seekBar;
    private TextView textView;

    private VusikView musicView;

    private MediaPlayer mediaPlayer;
    private int mediaFileLength;
    private int realtimeLength;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicView = (VusikView)findViewById(R.id.musicView);



        seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setMax(99); // 100% (0~99)
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               if(mediaPlayer.isPlaying())
               {
                   SeekBar seekBar = (SeekBar)v;
                   int playPosition = (mediaFileLength/100)*seekBar.getProgress();
                   mediaPlayer.seekTo(playPosition);
               }
               return false;
            }
        });

        textView = (TextView)findViewById(R.id.textTimer);

        btn_play_pause = (ImageButton) findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(new View.OnClickListener() {




            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);


                AsyncTask<String,String,String> mp3Play = new AsyncTask<String, String, String>() {

                    @Override
                    protected void onPreExecute() {
                        mDialog.setMessage("Please wait");
                        mDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... params) {
                       try{
                           mediaPlayer.setDataSource(params[0]);
                           mediaPlayer.prepare();
                       }
                       catch (Exception ex)
                       {

                       }
                       return "";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mediaFileLength = mediaPlayer.getDuration();
                        realtimeLength = mediaFileLength;
                        if(!mediaPlayer.isPlaying())
                        {
                            mediaPlayer.start();
                            btn_play_pause.setImageResource(R.drawable.ic_pause);
                        }
                        else
                        {
                            mediaPlayer.pause();
                            btn_play_pause.setImageResource(R.drawable.ic_play);
                        }

                        updateSeekBar();
                        mDialog.dismiss();
                    }
                };

                mp3Play.execute("http://mic.duytan.edu.vn:86/ncs.mp3"); // direct link mp3 file

                musicView.start();
            }
        });

       mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);


    }

    private void updateSeekBar() {
        seekBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition() / mediaFileLength)*100));
        if(mediaPlayer.isPlaying())
        {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realtimeLength-=1000; // declare 1 second
                    textView.setText(String.format("%d:%d",TimeUnit.MILLISECONDS.toMinutes(realtimeLength),
                            TimeUnit.MILLISECONDS.toSeconds(realtimeLength) -
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realtimeLength))));

                }

            };
            handler.postDelayed(updater,1000); // 1 second
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play_pause.setImageResource(R.drawable.ic_play);
        musicView.stopNotesFall();

    }
}
