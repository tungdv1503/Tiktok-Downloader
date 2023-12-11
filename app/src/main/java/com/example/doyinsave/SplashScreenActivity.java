package com.example.doyinsave;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import com.example.doyinsave.admob.GoogleMobileAdsConsentManager;
import com.example.doyinsave.admob.MyApplication;
import com.google.android.gms.ads.MobileAds;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashScreenActivity extends AppCompatActivity {
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private static final long COUNTER_TIME_MILLISECONDS = 5000;

    private long secondsRemaining;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        createTimer(COUNTER_TIME_MILLISECONDS);

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                "LOG_TAG",
                                String.format(
                                        "%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }
    private void showInterstitialAds(){
        Application application = getApplication();
        ((MyApplication) application)
                .showAdIfAvailable(
                        this,
                        new MyApplication.OnShowAdCompleteListener() {
                            @Override
                            public void onShowAdComplete() {
                                if (googleMobileAdsConsentManager.canRequestAds()) {

                                }
                            }
                        });
    }
    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this);

        // Load an ad.
        Application application = getApplication();
        ((MyApplication) application).loadAd(this);
    }
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }
    private void createTimer(long time) {

        CountDownTimer countDownTimer =
                new CountDownTimer(time, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1;
                    }

                    @Override
                    public void onFinish() {
                        secondsRemaining = 0;
                        showInterstitialAds();
                    }
                };
        countDownTimer.start();
    }
}