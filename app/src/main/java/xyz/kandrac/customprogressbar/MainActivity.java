package xyz.kandrac.customprogressbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import xyz.kandrac.progressbar.HeavyProgressBar;

public class MainActivity extends AppCompatActivity {

    private HeavyProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seek);
        progressBar = (HeavyProgressBar) findViewById(R.id.progress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressBar.animateProgress(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
