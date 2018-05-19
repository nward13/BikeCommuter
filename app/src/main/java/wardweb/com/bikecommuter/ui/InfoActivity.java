package wardweb.com.bikecommuter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.widget.TextView;

import wardweb.com.bikecommuter.R;

public class InfoActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Info pop-up with contact and license information

        setContentView(R.layout.info_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.65), (int)(height*0.65));

        TextView infoWindowText = findViewById(R.id.infoWindowText);
        infoWindowText.setText(Html.fromHtml("<h1>Contact Us</h1>" +
                        "<p>Please feel free to contact WardWeb with questions, concerns, or suggestions. We would love to hear from you.</p>" +
                        "<p>wardweb360@gmail.com</p>" +
                        "<h1>Open Source Licenses</h1>" +
                        "<p>Notice for file(s)/functionality:</p>" +
                        "<p>Circular Seek Arc</p>" +
                        "<p>The MIT License (MIT)</p><p>Copyright (c) 2013 Triggertrap Ltd<br>" +
                        "Author Neil Davies</p>" +
                        "<p>Permission is hereby granted, free of charge, to any person obtaining a copy of\n" +
                        "this software and associated documentation files (the \"Software\"), to deal in\n" +
                        "the Software without restriction, including without limitation the rights to\n" +
                        "use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of\n" +
                        "the Software, and to permit persons to whom the Software is furnished to do so,\n" +
                        "subject to the following conditions:</p>" +
                        "<p>The above copyright notice and this permission notice shall be included in all\n" +
                        "copies or substantial portions of the Software.</p>" +
                        "<p>THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS\n" +
                        "FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR\n" +
                        "COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER\n" +
                        "IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN\n" +
                        "CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</p>"
        ));
    }
}
