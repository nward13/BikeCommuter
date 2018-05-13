package wardweb.com.bikecommuter.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wardweb.com.bikecommuter.R;
import wardweb.com.bikecommuter.model.Preacher;

public class ClockActivity extends AppCompatActivity {
    private TextClock clock;
    private String text;
    private Calendar calendar;
    private Preacher preacher;
    private Button startStopButton;
    private int hour;
    private int minute;
    private ScheduledExecutorService scheduler;
    private boolean speechIsOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set formats for text clock (12hr, 24hr)
        clock = findViewById(R.id.textClock1);
        clock.setFormat12Hour("hh : mm : ssaa");
        clock.setFormat24Hour("kk : mm : ss");

        // Create buttons
        startStopButton = findViewById(R.id.startStopButton);

        // Use startStopButton to activate/deactivate text to speech
        speechIsOn = false;
        startStopButton.setText(R.string.clock_start_reminders_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speechIsOn) {
                    // Executed when user presses the STOP button: shuts down the scheduler and text to speech and changes the text of the button
                    startStopButton.setText(R.string.clock_start_reminders_button);
                    startStopButton.setBackgroundColor(R.color.colorStartButton);
                    scheduler.shutdown();
                    preacher.ttsStop();

                }
                else {
                    // Executed when user presses the START button: initializes text to speech, schedules speaking at designated intervals, and changes the text of the button
                    startStopButton.setText(R.string.clock_stop_reminders_button);
                    startStopButton.setBackgroundColor(R.color.colorStopButton);

                    // Initialize text to speech
                    preacher = new Preacher(ClockActivity.this);
                    int preacher_start = preacher.ttsStart();

                    // If text to speech was properly initialized, schedule speaking of text at designated interval, else make Toast with problem
                    if (preacher_start == 0) {
                        schedule(15);
                    }
                    else {
                        Toast.makeText(ClockActivity.this, "ttsStart failed", Toast.LENGTH_LONG).show();
                    }
                }
                // Toggle speechIsOn
                speechIsOn = !speechIsOn;
            }
        });
    }


    public void clockReader() {
        // Creates correct text to be spoken, then calls preacher.preach(text) to speak the text

        // Prepare text to be spoken
        calendar = Calendar.getInstance();
        if (android.text.format.DateFormat.is24HourFormat(ClockActivity.this)) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = calendar.get(Calendar.HOUR);
        }
        minute = calendar.get(Calendar.MINUTE);
        text = "The time is" + hour + " " + String.format(Locale.US, "%02d", minute);

        // Speak the text
        preacher.preach(text);
    }

    public void schedule(int interval) {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Handle exceptions in runnable so that scheduler continues
                try {
                    clockReader();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1, interval, TimeUnit.SECONDS);
    }
}
