package com.dv.apps.purpleplayer.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dv.apps.purpleplayer.BuildConfig;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.github.javiersantos.piracychecker.enums.Display;
import com.github.javiersantos.piracychecker.enums.InstallerID;

/**
 * Created by Dhaval on 30-04-2018.
 */

public class PurpleHelper {

    private static PurpleHelper instance;
    public final String mobileAdsInitialize = "ca-app-pub-3940256099942544~3347511713";
    public final String bannerAdId = "ca-app-pub-9589539002030859/7903238812";
    public final String interstetialAdId = "ca-app-pub-9589539002030859/7346365267";
    public final String rewardedVideoAdId = "ca-app-pub-9589539002030859/2050180592";
    public final String testDevice = "DD0CDAB405F30F550CD856F507E39725";

    public final String lyricsServer = "https://api.lyrics.ovh/v1/";

    public PurpleHelper(){
        //Empty Constructor
        instance = this;
    }

    public static PurpleHelper getInstance(){
        if (instance == null){
            instance = new PurpleHelper();
            return instance;
        }else {
            return instance;
        }
    }

    public void validate(Context context, PiracyChecker checker, SharedPreferences preferences){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")) {
            checker = new PiracyChecker(context)
//                    .enableGooglePlayLicensing("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgBT+tKXqMH4FEejIu9Zhbs6+1N/UXFPN7TK11PYzkYe5qSvQnfENkdjXfJQ55h2aAbMn1jOXXB5xQwDHyRE2VNlrGBIplIRPFfDpZ4Vl/2niCwseLbke9VetHGIgx9vROBsJs9QMWJC0/yphxPqARXNJ+uYkQg164ZXaLcAl7/7pOxucZ9DKN0lbIqwE8eysFr6gcCeVutGfn5tDya5+cFj9zMGq6ImQSaCPTcWXm4/up2HyASKVw9TYuCgvGRvVF1BrP6ifs6uXFxZvK1mYCnVHGXPhAlQjlnTMp2k8Wy/KJdgCYRYjeMfvm+Z/KOp2mLZBW5QAc6Aro4jG9Pxr+wIDAQAB")
//                    .saveResultToSharedPreferences(preferences, "valid_license")
//                    .enableSigningCertificate("ldgUxo13aF54Jsqay5L9W/S4/g0=")     google
//                    .enableSigningCertificate("uxh/RlppBQNr/6nlf3bO4UmKmNg=")
                    .enableUnauthorizedAppsCheck()
                    .enableInstallerId(InstallerID.GOOGLE_PLAY)
                    .enableInstallerId(InstallerID.AMAZON_APP_STORE)
                    .enableInstallerId(InstallerID.GALAXY_APPS)
                    .blockIfUnauthorizedAppUninstalled(preferences, "app_unauth")
                    .display(Display.ACTIVITY);
            checker.start();
        }
    }
}
