package com.schef.rss.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public String [] urls = {"http://www.evilcorgi.com/contentservice/site/vic",
            "http://www.evilcorgi.com/contentservice/site/nyt",
            "http://www.evilcorgi.com/contentservice/site/hfp",
            "http://www.evilcorgi.com/contentservice/site/usa",
            "http://www.evilcorgi.com/contentservice/site/ars"};

    public int original;
    public boolean changed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        Button setReturnButton = (Button)findViewById(R.id.settings_back);
        setReturnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                resultIntent.putExtra("changed",changed);
                finish();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.paper_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.papers_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        String currentPaper = Utils.getPaper(getApplicationContext());
        int i = 0;
        for(;i < urls.length; i++) {
            if(urls[i].equalsIgnoreCase(currentPaper)) {
                break;
            }
        }
        original = i;
        spinner.setSelection(i);

        TextView emailText = (TextView)findViewById(R.id.problem);
        emailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] to = {"dizzydeviltesting@gmail.com"};
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT,"You Broke Something");
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nI always want feedback and If you want something I can add almost anything to the app you can think of. \n\n -Aaron");
                startActivity(Intent.createChooser(intent, "Send Email"));
                finish();
            }
        });

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        mIntent.putExtra("changed",changed);
        setResult(RESULT_OK, mIntent);
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position >= 0 && position < urls.length) {
            Utils.setPaper(view.getContext(),urls[position]);
            if(position != original) {
                changed = true;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
