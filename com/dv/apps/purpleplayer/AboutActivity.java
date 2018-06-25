package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dv.apps.purpleplayer.Utils.PurpleHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Date;


//Icon credit = psdblast.com
public class AboutActivity extends AppCompatActivity {

    ImageButton emailButton, fBPageButton;
    ImageView aboutImage;
    TextView textView, textView2;
    AdView adView;
    Button credits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.about);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        textView = (TextView) findViewById(R.id.version_about);
        textView.setText("v " + BuildConfig.VERSION_NAME);

        textView2 = (TextView) findViewById(R.id.name_about);
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
            textView2.setText("Purple Player");
        }else {
            textView2.setText("Purple Player Pro");
        }

        aboutImage =  findViewById(R.id.icon_about);
        aboutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long freeOfAdTill = PreferenceManager.getDefaultSharedPreferences(AboutActivity.this).getLong("ad_free_till", 0);
                Toast.makeText(AboutActivity.this, R.string.you_are_free_of_ads_till + " \n" + new Date(freeOfAdTill), Toast.LENGTH_SHORT).show();
            }
        });

        adView = findViewById(R.id.adView);
        if (Calendar.getInstance().getTimeInMillis() < PreferenceManager.getDefaultSharedPreferences(AboutActivity.this).getLong("ad_free_till", 0)){
            adView.setVisibility(View.GONE);
        }

        credits = findViewById(R.id.creditsButton);
        credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(AboutActivity.this).
                        customView(R.layout.credits_dialog, true)
                        .title(R.string.credits)
                        .positiveText(R.string.ok)
                        .show();

            }
        });

        fBPageButton = (ImageButton) findViewById(R.id.fb_message_icon);
        fBPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://messaging/" + "840546956122931"));
//                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/840546956122931"));
                    startActivity(intent);
                }catch (Exception e){
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/thepurpleplayer"));
                    startActivity(fbIntent);
                }

            }
        });

        emailButton = (ImageButton) findViewById(R.id.contact_icon);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: dkillworld@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Purple Player");
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        });

        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            AdView adView = findViewById(R.id.adView);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(PurpleHelper.getInstance().bannerAdId);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(PurpleHelper.getInstance().testDevice)
                    .build();
            adView.loadAd(adRequest);
            boolean isTestDevice = adRequest.isTestDevice(this);
            if (isTestDevice) {
                Toast.makeText(this, "Loaded on Test Device", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Aesthetic.resume(this);
    }
}
