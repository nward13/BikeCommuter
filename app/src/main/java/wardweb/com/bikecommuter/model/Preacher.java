package wardweb.com.bikecommuter.model;

// Common class to convert each Activity's desired text into speech

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.Locale;

public class Preacher {

    private TextToSpeech tts;
    private int init_status; // Used to return status from ttsStart() without needing to return from void OnInitListener()
    private Context context; // Stores the context of the activity that instantiated the Preacher class
    private Boolean shutErDown;


    public Preacher(Context context) {
        this.context = context;
    }

    // Initialize text to speech. If initialization is successful and language is supported and available, return 0 (success)
    // Else, make Toast of the problem and return 1 or 2
    public int ttsStart() {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    shutErDown = false;
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            // After text is spoken, checks to see if shutdown is requested by ttsShutdown method
                            if (shutErDown) {
                                tts.shutdown();
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "Text to speech is not supported on your device.", Toast.LENGTH_LONG).show();
                        init_status = 2;
                    }
                    else {
                        init_status = 0;
                    }
                }
                else {
                    Toast.makeText(context, "Text to speech initialization failed", Toast.LENGTH_LONG).show();
                    init_status = 1;
                }
            }
        });
        return init_status;
    }

    // Text to Speech
    // MUST call ttsStart(context) before calling preach()
    public void preach(String text) {
        if (text == null || text.isEmpty()) {
            text = "Sorry, the time was not correctly processed for text to speech.";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // Shuts down text to speech
    public void ttsStop() {
        // shutErDown variable is used to indicate that a shutdown has been requested by the activity,
        // but should not be shutdown until the last scheduled text is spoken
        shutErDown = true;
    }

}
