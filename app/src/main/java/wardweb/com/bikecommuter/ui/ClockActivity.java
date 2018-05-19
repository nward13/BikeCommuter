package wardweb.com.bikecommuter.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.triggertrap.seekarc.SeekArc;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wardweb.com.bikecommuter.R;
import wardweb.com.bikecommuter.model.Preacher;



public class ClockActivity extends Activity {
    private TextClock clock;
    private String text;
    private Calendar calendar;
    private Preacher preacher;
    private Button startStopButton, numAlertsPickButton, numberPickerSetButton;
    private TextView intervalText, nextAlertText, clockAMPMText;
    private int hour, minute, progress, numberOfAlerts, alertCounter, preacherStart;
    private ScheduledExecutorService scheduler;
    private boolean speechIsOn, infiniteAlerts, numberPickerUsed;
    private SeekArc timeSeeker;
    private NumberPicker alertNumberPicker;
    private String[] numberPickerValues;
    private static final int MY_DATA_CHECK_CODE = 1234; // Checksum

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set formats for text clock (12hr, 24hr)
        clock = findViewById(R.id.textClock1);
        clock.setFormat12Hour("hh:mm:ss");
        clock.setFormat24Hour("kk:mm:ss");

        // If 12-hr clock, get am/pm to format in TextView below TextClock
        clockAMPMText = findViewById(R.id.clockAMPMText);
        if (android.text.format.DateFormat.is24HourFormat(ClockActivity.this)) {
            clockAMPMText.setVisibility(View.INVISIBLE);
        }
        else {
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.AM_PM) == Calendar.AM) {
                clockAMPMText.setText(R.string.AM);
            }
            else {
                clockAMPMText.setText(R.string.PM);
            }
        }

        // Create start button, text views, and Time Seeker
        startStopButton = findViewById(R.id.startStopButton);
        timeSeeker = findViewById(R.id.seekArc);
        intervalText = findViewById(R.id.intervalText);
        nextAlertText = findViewById(R.id.nextAlertText);

        // Ensures that the start button onClick method does not try to access theh number picker before it has been initialized
        numberPickerUsed = false;

        // Used to determine the values on the "Number of Alerts" number picker wheel
        // Initialized before the number picker dialog so that the start button onClick method can access it's length attribute
        numberPickerValues = new String[]{"1", "2", "3", "4", "5", getResources().getString(R.string.number_of_alerts_max_value)};

        // Create numAlertsPick Button and set onClick listener to open the number picker dialog
        numAlertsPickButton = findViewById(R.id.numAlertsValueButton);
        numAlertsPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Opens the number picker dialog
            showNumberPicker();
            }
        });

        // Check that Text to Speech Engine is installed on user's device, if not, prompt installation
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);


        // Use startStopButton to activate/deactivate text to speech

        // Used to determine whether the button status is currently ON or OFF
        speechIsOn = false;
        // Set initial button text to "Start.."
        startStopButton.setText(R.string.clock_start_reminders_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speechIsOn) {
                    // NOTE: This is method is also called via startStopButton.performClick() when the number of requested alerts has been reached
                    // Executed when user presses the STOP button: shuts down the scheduler and text to speech and changes the start/stop button
                    startStopButton.setText(R.string.clock_start_reminders_button);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.colorStartButton));
                    timeSeeker.setEnabled(true);
                    numAlertsPickButton.setEnabled(true);
                    // Shut down the scheduler
                    scheduler.shutdown();
                    // Shut down text to speech
                    preacher.ttsStop();
                }
                else {
                    // Executed when user presses the START button: initializes text to speech, schedules speaking at designated intervals, and changes the text of the button
                    startStopButton.setText(R.string.clock_stop_reminders_button);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.colorStopButton));

                    // Initialize text to speech
                    preacher = new Preacher(ClockActivity.this);
                    preacherStart = preacher.ttsStart();

                    // If text to speech was properly initialized, schedule speaking of text at designated interval, else make Toast with problem
                    if (preacherStart == 0) {
                        int alertInterval = timeSeekerProgress();
                        alertCounter = 0;

                        // if the user has already used the number picker to select a value for number of alerts, the showNumberPicker()
                        // method has been called and alertNumberPicker has been initialized. If not (user has continued with default "continuous"
                        // number of alerts and not opened the number picker), alertNumberPicker will be null, so set numberOfAlerts to the length of
                        // the numberPickerValues array, which corresponds to "Continuous", which will always be the last value on the number picker
                        if (numberPickerUsed) {
                            numberOfAlerts = alertNumberPicker.getValue();
                        }
                        else {
                            numberOfAlerts = numberPickerValues.length;
                        }

                        // If any value except the last value in the numberPickerValues array ("Continuous") is selected, use index value as the number of alerts
                        if (numberOfAlerts < numberPickerValues.length) {
                            infiniteAlerts = false;
                        }
                        // Otherwise, "Continuous" is selected, so set infiniteAlerts to true to continue alerts until Stop button is pressed
                        else {
                            infiniteAlerts = true;
                        }
                        // Disable timeSeeker so length of alert interval can not be adjusted until Stop button is clicked
                        timeSeeker.setEnabled(false);
                        numAlertsPickButton.setEnabled(false);

                        // Call the schedule function to schedule the alerts at the alert interval currently in the number picker
                        schedule(alertInterval);

                    }
                    else {
                        // If text to speech not properly initialized, alert user
                        Toast.makeText(ClockActivity.this, "Text to speech not currently available.", Toast.LENGTH_LONG).show();
                    }
                }
                // Toggle speechIsOn
                speechIsOn = !speechIsOn;

                // Update the time value for the next alert
                updateNextAlert();
            }
        });

        // Update initial value of alert interval display
        intervalText.setText(String.valueOf(timeSeekerProgress()));
        updateNextAlert();

        // Update alert interval display as time selector is scrolled
        timeSeeker.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                intervalText.setText(String.valueOf(timeSeekerProgress()));
                updateNextAlert();
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                intervalText.setText(String.valueOf(timeSeekerProgress()));
            }
        });

        // Set onClick listener for info button -> open about meny
        Button infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClockActivity.this, InfoActivity.class));
            }
        });
    }

    private void showNumberPicker() {
        // Called when the "Number of Alerts" value button is pressed, creates the dialog to set number of alerts via number picker wheel

        final Dialog wheelView = new Dialog(ClockActivity.this);
        wheelView.setContentView(R.layout.dialog);
        numberPickerSetButton = wheelView.findViewById(R.id.numberPickerSetButton);

        // Create Number Picker for the number of voice alerts
        alertNumberPicker = wheelView.findViewById(R.id.alertNumberPicker);
        alertNumberPicker.setWrapSelectorWheel(false);
        alertNumberPicker.setMinValue(1);
        alertNumberPicker.setMaxValue(numberPickerValues.length);
        alertNumberPicker.setDisplayedValues(numberPickerValues);
        alertNumberPicker.setValue(numberPickerValues.length);


        numberPickerSetButton.setOnClickListener(new View.OnClickListener() {
            // Sets an onClick listener for the "set" button, to close dialog and transfer the number picker value to the alert initializer
            @Override
            public void onClick(View v) {
                // Close dialog
                wheelView.dismiss();

                // set numberOfAlerts and set the text of the number of alerts button either to the number of alerts, or to "Continuous" (the max value)
                numberOfAlerts = alertNumberPicker.getValue();
                if (numberOfAlerts < numberPickerValues.length) {
                    numAlertsPickButton.setText(String.valueOf(numberOfAlerts));
                }
                else {
                    numAlertsPickButton.setText(R.string.number_of_alerts_max_value);
                }

                // Set numberPickerused to true for remainder of app runtime. numberOfAlerts should no longer be null
                numberPickerUsed = true;
            }
        });

        // Display the dialog
        wheelView.show();
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
        // Calendar.HOUR returns 0 instead of 12 am/pm, which is unintuitive for users, so change to 12
        if (hour == 0) {
            hour = 12;
        }
        minute = calendar.get(Calendar.MINUTE);
        if (minute == 0) {
            text = "The time is" + hour + "o'clock";
        }
        else {
            text = "The time is " + hour + ":" + String.format(Locale.US, "%02d", minute);
        }

        // Speak the text
        preacher.preach(text);

        // If "Continuous" is selected, alerts should be scheduled until user presses Stop button.
        // Otherwise, alertCounter is incremented and alerts are stopped when the requested numberOfAlerts is reached
        if (!infiniteAlerts) {
            alertCounter += 1;
            if (alertCounter >= numberOfAlerts) {
                // Must be run on the same thread originally used to create the views in order to alter them
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Run all the same functionality as if the user had pressed the Stop button
                        startStopButton.performClick();
                    }
                });

            }
        }

        runOnUiThread(new Runnable() {
            // Must be run on the same thread originally used to create the views in order to alter them
            @Override
            public void run() {
                // Update the nextAlert time
                updateNextAlert();
            }
        });


    }

    public void schedule(int interval) {
        // Schedules the task at the selected interval. NOTE: Stopping the alerts after the requested
        // number is reached is handled within the clockReader method that is called by the scheduler
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
        }, interval, interval, TimeUnit.MINUTES);
    }

    public int timeSeekerProgress() {
        // Determine where the timeSeeker is set and return the progress value
        if (timeSeeker.getProgress() == 0) {
            // timeSeeker range can only start at 0, but an alert every 0 seconds is a poor setting.
            // So, 0 is forced to a value of 5, and every other value is set to value * 5 + 5
            progress = timeSeeker.getProgress() + 5;
        }
        else {
            progress = timeSeeker.getProgress() * 5 + 5;
        }
        return progress;
    }

    public void updateNextAlert() {
        // Updates the nextAlertText with the time that the next alert will be spoken
        Calendar cal = Calendar.getInstance();
        int alertInterval = timeSeekerProgress();
        cal.add(Calendar.MINUTE, alertInterval);
        String nextAlert = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        nextAlertText.setText(nextAlert);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != MY_DATA_CHECK_CODE) {
            // failed, please install proper Text to Speech software
            Toast.makeText(ClockActivity.this, "Please install text to speech engine.", Toast.LENGTH_LONG).show();
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }
    }
}
