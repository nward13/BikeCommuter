package wardweb.com.bikecommuter.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import wardweb.com.bikecommuter.R;
import wardweb.com.bikecommuter.model.Preacher;

public class ClockActivity extends AppCompatActivity {
    private TextClock clock;
    private String text;
    private Calendar calendar;
    private Preacher preacher;
    private Button startStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set formats for text clock (12hr, 24hr)
        clock = findViewById(R.id.textClock1);
        clock.setFormat12Hour("hh : mm : ssaa");
        clock.setFormat24Hour("kk : mm : ss");

        // Use startStopButton to activate/deactivate text to speech
        startStopButton = findViewById(R.id.startStopButton);


        // Initialize text to speech
        preacher = new Preacher(ClockActivity.this);
        int preacher_start = preacher.ttsStart();

        // If text to speech was properly initialized, call preach() to speak text, else make Toast with problem
        if (preacher_start == 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    // Prepare text to be spoken
                    int hour;
                    int minute;
                    calendar = Calendar.getInstance();
                    if (android.text.format.DateFormat.is24HourFormat(ClockActivity.this)) {
                        hour = calendar.get(Calendar.HOUR_OF_DAY);
                    }
                    else {
                        hour = calendar.get(Calendar.HOUR);
                    }
                    minute = calendar.get(Calendar.MINUTE);
                    text = "The time is" + hour + " " + String.format("%02d", minute);

                    // Speak the text
                    preacher.preach(text);

                }
            }, 0, 30000);
        }
        else {
            Toast.makeText(ClockActivity.this, "ttsStart failed", Toast.LENGTH_LONG).show();
        }
    }
}
