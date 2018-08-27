package com.liuinc.waterqualitysystem.core;

import android.app.Service;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

import com.liuinc.waterqualitysystem.R;
import com.liuinc.waterqualitysystem.core.data.BoundaryDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PollingService extends Service {
    private Handler handler = new Handler();

    public PollingService() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        List<BoundaryDTO> list = new ArrayList<>();
        try {
            list = (List<BoundaryDTO>) intent.getSerializableExtra("DEVICE_BOUNDARY");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundPool soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        int soundId = soundPool.load(getApplicationContext(), R.raw.diablo_3_black_smith_nokia_tune, 0);
        showTime = new ShowTimeTask(list, soundPool, soundId);
        handler.postDelayed(showTime, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        handler.postDelayed(showTime, 1000);
        return null;
    }

    @Override
    public void onDestroy() {
        if(showTime != null) {
            handler.removeCallbacks(showTime);
        }
        super.onDestroy();
    }

    private ShowTimeTask showTime;

    class ShowTimeTask implements Runnable {
        private List<BoundaryDTO> mBoundary;
        private SoundPool mSoundPool;
        private int mSoundId;
        ShowTimeTask(List<BoundaryDTO> boundary, SoundPool soundPool, int soundId) {
            mBoundary = boundary;
            mSoundPool = soundPool;
            mSoundId = soundId;
        }
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (BoundaryDTO limits : mBoundary) {
                        String result = getURL(limits.getThinkSpeakId(), WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE);
                        if (result.length() == 0) {
                            return;
                        }
                        float value = Float.parseFloat(result);
                        if ((limits.getTemperatureHigh() != null && value > limits.getTemperatureHigh()) ||
                                (limits.getTemperatureLow() != null && value < limits.getTemperatureLow())) {
                            mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
                            return;
                        }
                        result = getURL(limits.getThinkSpeakId(), WaterQualitySystem.THINK_SPEAK_LIQUID_PH7);
                        value = Float.parseFloat(result);
                        if ((limits.getPh7High() != null && value > limits.getPh7High()) ||
                                (limits.getPh7Low() != null && value < limits.getPh7Low())) {
                            mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
                            return;
                        }
                        result = getURL(limits.getThinkSpeakId(), WaterQualitySystem.THINK_SPEAK_LIQUID_PH4);
                        value = Float.parseFloat(result);
                        if (limits.getPh4() != null && value > limits.getPh4()) {
                            mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
                            return;
                        }
                        result = getURL(limits.getThinkSpeakId(), WaterQualitySystem.THINK_SPEAK_LIQUID_DO);
                        value = Float.parseFloat(result);
                        if (limits.getDoo() != null && value < limits.getDoo()) {
                            mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
                            return;
                        }
                    }
                }
            }).start();
            handler.postDelayed(this, 60000);
        }
    }

    private String getURL(String id, int type) {
        String response = "";
        try {
            URL url = new URL("https://thingspeak.com/channels/" + id + "/field/" + String.valueOf(type) + "/last.html");
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String data;
            while ((data = in.readLine()) != null) {
                response = data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
