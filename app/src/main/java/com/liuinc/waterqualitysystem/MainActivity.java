package com.liuinc.waterqualitysystem;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.liuinc.waterqualitysystem.core.BtSvc;
import com.liuinc.waterqualitysystem.core.IBtSearchPairing;
import com.liuinc.waterqualitysystem.core.IHttpGet;
import com.liuinc.waterqualitysystem.core.WaterQualitySystem;
import com.liuinc.waterqualitysystem.core.data.FieldDO;
import com.liuinc.waterqualitysystem.core.data.LimitDO;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static ViewPager mViewPager;

    private static Handler mTab2Handler;
    private static Handler mTab3Handler;

    private static WaterQualitySystem mSystemCore;
    private static BtSvc mBtSvc;

    // Data
    // Serial: "B-00002-T", ThinkSpeak ID: 468843
    // Name: "HC-05", Address: 98:D3:32:10:CE:5B
    // Password:1234
    // HC-05 WiFi IP: 192.168.4.1

    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSystemCore = new WaterQualitySystem();
        mSystemCore.start(this);
        mBtSvc = new BtSvc();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        if (mSystemCore.getDevice().length() == 0)
                            return;
                        mBtSvc.getDevice(mSystemCore.getDevice(), new IBtSearchPairing() {
                            @Override
                            public int onDeviceCallback(int returnCode) {
                                Message msg = new Message();
                                if(returnCode == BtSvc.ERR_BT_DEVICE_FOUND) {
                                    msg.what = 100;
                                } else {
                                    msg.what = 101;
                                }
                                if (mTab2Handler == null) {
                                    return -102;
                                }
                                mTab2Handler.sendMessage(msg);
                                return 0;
                            }
                        });
                        break;
                    case 2:
                        mBtSvc.getUnknownDevice(mSystemCore.listDevices(), new IBtSearchPairing() {
                            @Override
                            public int onDeviceCallback(int returnCode) {
                                switch (returnCode) {
                                    case BtSvc.ERR_BT_TURN_OFF: {
                                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                                        break;
                                    }

                                    case BtSvc.ERR_BT_DEVICE_FOUND: {
                                        Message msg = new Message();
                                        msg.what = 200;
                                        if (mTab3Handler == null) {
                                            return -201;
                                        }
                                        mTab3Handler.sendMessage(msg);
                                        break;
                                    }

                                    default:
                                        Message msg = new Message();
                                        msg.what = 201;
                                        if (mTab3Handler == null) {
                                            return -201;
                                        }
                                        mTab3Handler.sendMessage(msg);
                                }
                                return 0;
                            }
                        });
                        break;
                    default:
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                    case 2:
                        mBtSvc.resetDevice();
                        break;
                    default:
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBtSvc.resetDevice();
        mSystemCore.stop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class MonitorFragment extends PlaceholderFragment {
        private static final String ARG_SECTION_INDEX = "section_index";
        private Spinner mTab1Spinner;
        private WebView mWebView;
        private TextView mTab1TextMeasure;
        private ImageView mTab1ImageViewNormal;

        public MonitorFragment() {
        }

        public static MonitorFragment newInstance(int sectionNumber) {
            MonitorFragment fragment = new MonitorFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_INDEX, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scene1_frg_monitor, container, false);
            mTab1TextMeasure = rootView.findViewById(R.id.tab1_text_measure);
            String deviceId = mSystemCore.getDevice();
            if(deviceId.length() > 0) {
                mSystemCore.getThinkSpeakUrlAsync(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE, new IHttpGet() {
                    @Override
                    public int onGet(String response) {
                        mTab1TextMeasure.setText(response);
                        updateNormalImageView(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE, response);
                        return 0;
                    }
                });
            }
            mTab1ImageViewNormal = rootView.findViewById(R.id.tab1_image_view_normal);
            mTab1Spinner = rootView.findViewById(R.id.spinner);
            final String[] names = mSystemCore.listDevicesReadName();
            ArrayAdapter fieldList = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, names);
            mTab1Spinner.setAdapter(fieldList);
            mTab1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String deviceId = mSystemCore.getDevice(position);
                    mSystemCore.setDevice(deviceId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            Button button = rootView.findViewById(R.id.tab1_button_add);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(2, true);
                }
            });
            button = rootView.findViewById(R.id.tab1_button_delete);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSystemCore.removeDevice();
                    final String[] names = mSystemCore.listDevicesReadName();
                    ArrayAdapter fieldList = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, names);
                    mTab1Spinner.setAdapter(fieldList);
                }
            });

            Log.d("section", getString(R.string.section_format));
            Log.d("arg", String.valueOf(getArguments().getInt(ARG_SECTION_INDEX)));

            button = rootView.findViewById(R.id.tab1_button_temp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.loadUrl(mSystemCore.getThinkSpeakUrl(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE));
                    mSystemCore.getThinkSpeakUrlAsync(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE, new IHttpGet() {
                        @Override
                        public int onGet(String response) {
                            mTab1TextMeasure.setText(response);
                            updateNormalImageView(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE, response);
                            return 0;
                        }
                    });
                }
            });
            button = rootView.findViewById(R.id.tab1_button_pH);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.loadUrl(mSystemCore.getThinkSpeakUrl(WaterQualitySystem.THINK_SPEAK_LIQUID_PH7));
                    mSystemCore.getThinkSpeakUrlAsync(WaterQualitySystem.THINK_SPEAK_LIQUID_PH7, new IHttpGet() {
                        @Override
                        public int onGet(String response) {
                            mTab1TextMeasure.setText(response);
                            updateNormalImageView(WaterQualitySystem.THINK_SPEAK_LIQUID_PH7, response);
                            return 0;
                        }
                    });
                }
            });
            button = rootView.findViewById(R.id.tab1_button_NH3);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.loadUrl(mSystemCore.getThinkSpeakUrl(WaterQualitySystem.THINK_SPEAK_LIQUID_PH4));
                    mSystemCore.getThinkSpeakUrlAsync(WaterQualitySystem.THINK_SPEAK_LIQUID_PH4, new IHttpGet() {
                        @Override
                        public int onGet(String response) {
                            mTab1TextMeasure.setText(response);
                            updateNormalImageView(WaterQualitySystem.THINK_SPEAK_LIQUID_PH4, response);
                            return 0;
                        }
                    });
                }
            });
            button = rootView.findViewById(R.id.tab1_button_DO);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.loadUrl(mSystemCore.getThinkSpeakUrl(WaterQualitySystem.THINK_SPEAK_LIQUID_DO));
                    mSystemCore.getThinkSpeakUrlAsync(WaterQualitySystem.THINK_SPEAK_LIQUID_DO, new IHttpGet() {
                        @Override
                        public int onGet(String response) {
                            mTab1TextMeasure.setText(response);
                            updateNormalImageView(WaterQualitySystem.THINK_SPEAK_LIQUID_DO, response);
                            return 0;
                        }
                    });
                }
            });

            mWebView = rootView.findViewById(R.id.tab1_webview);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.loadUrl(mSystemCore.getThinkSpeakUrl(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE));
            return rootView;
        }

        private void updateNormalImageView(int type, String data) {
            boolean isValid = mSystemCore.isValid(WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE, data);
            if (isValid) {
                mTab1ImageViewNormal.setImageResource(R.drawable.screen1_img_normal);
            } else {
                mTab1ImageViewNormal.setImageResource(R.drawable.screen1_img_abnormal_red);
            }
        }
    }

    public static class CalibrationFragment extends Fragment {
        private static final String ARG_SECTION_INDEX = "section_index";
        private Button mTab2ButtonTimer;
        private Button mTab2ButtonSkip;

        private int nextCommand;
        private int nextLiquidName;
        private String[] liquidNames;

        public CalibrationFragment() {
        }

        public static CalibrationFragment newInstance(int sectionNumber) {
            CalibrationFragment fragment = new CalibrationFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_INDEX, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scene2_frg_calibration, container, false);
            liquidNames = getResources().getStringArray(R.array.tab2_text_calibrations);

            mTab2ButtonTimer = rootView.findViewById(R.id.tab2_buttonTimer);
            mTab2ButtonSkip = rootView.findViewById(R.id.tab2_buttonSkip);
            mTab2ButtonTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timer t;
                    Date tNext;
                    Calendar cNext;
                    switch (nextCommand) {
                        // pH7校正, 按下後反白, 使用者等10秒後進行下一步校正
                        case 0:
                            mTab2ButtonTimer.setEnabled(false);
                            mBtSvc.sendPh7Command();
                            t = new Timer(true);
                            tNext = new Date();
                            cNext = Calendar.getInstance();
                            cNext.setTime(tNext);
                            cNext.add(Calendar.SECOND, 10);
                            tNext = cNext.getTime();
                            t.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = nextCommand++;
                                    mTab2Handler.sendMessage(msg);
                                }
                            }, tNext);
                            break;
                        // pH4校正, 按下後反白, 使用者等10秒後進行下一步校正
                        case 1:
                            mTab2ButtonTimer.setEnabled(false);
                            mBtSvc.sendPh4Command();
                            t = new Timer(true);
                            tNext = new Date();
                            cNext = Calendar.getInstance();
                            cNext.setTime(tNext);
                            cNext.add(Calendar.SECOND, 10);
                            tNext = cNext.getTime();
                            t.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = nextCommand++;
                                    mTab2Handler.sendMessage(msg);
                                }
                            }, tNext);
                            break;
                        // DO校正, 按下後反白, 使用者等30秒後結束校正
                        case 2:
                            mTab2ButtonTimer.setVisibility(View.INVISIBLE);
                            mTab2ButtonSkip.setVisibility(View.INVISIBLE);
                            mBtSvc.sendDOCommand();
                            nextCommand++;
                            break;
                        default:
                    }
                }
            });
            mTab2ButtonSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTab2ButtonTimer.setVisibility(View.INVISIBLE);
                    mTab2ButtonSkip.setVisibility(View.INVISIBLE);
                }
            });
            nextCommand = 0;
            nextLiquidName = 0;
            if (mBtSvc.mErrno != 0) {
                if (mBtSvc.mErrno == BtSvc.ERR_BT_DEVICE_FOUND) {
                    Toast.makeText(getContext(), "hihi", Toast.LENGTH_LONG).show();
                }
                mBtSvc.mErrno = 0;
            }

            mTab2Handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    int msgId = msg.what;
                    switch (msgId) {
                        case 0:
                            mTab2ButtonTimer.setEnabled(true);
                            mTab2ButtonTimer.setText(liquidNames[++nextLiquidName]);
                            break;
                        case 1:
                            mTab2ButtonTimer.setEnabled(true);
                            mTab2ButtonSkip.setVisibility(View.VISIBLE);
                            mTab2ButtonTimer.setText(liquidNames[++nextLiquidName]);
                        case 100:
                            Toast.makeText(getContext(), R.string.tab2_toast_connected, Toast.LENGTH_LONG).show();
                            break;
                        case 101:
                            Toast.makeText(getContext(), R.string.tab2_toast_disconnected, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                }
            };
            return rootView;
        }
    }

    public static class DeviceFragment extends PlaceholderFragment {
        private static final String ARG_SECTION_INDEX = "section_index";
        private EditText edTextWifiSSID, edTextWifiPassword, edTextAlias;
        private ImageView mTab3ImgView;

        public DeviceFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DeviceFragment newInstance(int sectionNumber) {
            DeviceFragment fragment = new DeviceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_INDEX, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scene3_frg_device, container, false);

            mTab3ImgView = rootView.findViewById(R.id.tab3_image_view_bluetooth);
            edTextWifiSSID = rootView.findViewById(R.id.tab3_edText_password);
            edTextWifiPassword = rootView.findViewById(R.id.tab3_edText_wifi);
            edTextAlias = rootView.findViewById(R.id.tab3_edText_alias);

            String wifi = getActivity().getSharedPreferences("wqs", MODE_PRIVATE).getString("wifiSSID", "");
            String password = getActivity().getSharedPreferences("wqs", MODE_PRIVATE).getString("wifiPassword", "");

            if (!wifi.equals(""))
                edTextWifiSSID.setText(wifi);
            if (!password.equals(""))
                edTextWifiPassword.setText(password);

            final Button btn = rootView.findViewById(R.id.tab3_button_save);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] idNThinkSpeakId = mBtSvc.getDeviceIds();
                    if(idNThinkSpeakId.length == 0)
                        return;
                    FieldDO field = new FieldDO();
                    field.btMac = idNThinkSpeakId[0];
                    field.id = idNThinkSpeakId[1];
                    field.thinkSpeakId = idNThinkSpeakId[2];
                    field.aliasName = edTextAlias.getText().toString();
                    mSystemCore.addDevice(field);
                    mBtSvc.sendWifiSettingCommand();
//                    btn.setEnabled(false);
                }
            });
            if (mBtSvc.mErrno != 0) {
                if (mBtSvc.mErrno == BtSvc.ERR_BT_DEVICE_FOUND) {
                    mTab3ImgView.setImageResource(R.drawable.scene3_bt_connect);
                    mBtSvc.sendThinkSpeakIdGetCommand();
                    btn.setEnabled(true);
                }
                mBtSvc.mErrno = 0;
            } else {
                mTab3ImgView.setImageResource(R.drawable.scene3_bt_disconnect);
            }

            mTab3Handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    int msgId = msg.what;
                    switch (msgId) {
                        case 200:
                            mTab3ImgView.setImageResource(R.drawable.scene3_bt_connect);
                            mBtSvc.sendThinkSpeakIdGetCommand();
                            btn.setEnabled(true);
                            break;
                        case 201:
                            mTab3ImgView.setImageResource(R.drawable.scene3_bt_disconnect);
                            btn.setEnabled(false);
                            break;
                        default:
                            break;
                    }
                }
            };
            return rootView;
        }
    }

    public static class SettingsFragment extends PlaceholderFragment {
        private static final String ARG_SECTION_INDEX = "section_index";
        private EditText edTextTmpLow, edTextTmpHigh, edTextPHLow, edTextPHHigh, edTextPH4, edTextDO;
        private Switch swAlarm;
        private TextView txtViewDevice;

        public SettingsFragment() {
        }

        public static SettingsFragment newInstance(int sectionNumber) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_INDEX, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scene4_frg_settings, container, false);

            txtViewDevice = rootView.findViewById(R.id.scene4_text_alias);
            edTextTmpLow = rootView.findViewById(R.id.tab4_edText_tmpLow);
            edTextTmpHigh = rootView.findViewById(R.id.tab4_edText_tmpHigh);
            edTextPHLow = rootView.findViewById(R.id.tab4_edText_pHLow);
            edTextPHHigh = rootView.findViewById(R.id.tab4_edText_pHHigh);
            edTextPH4 = rootView.findViewById(R.id.tab4_edText_pH4);
            edTextDO = rootView.findViewById(R.id.tab4_edText_DO);
            swAlarm = rootView.findViewById(R.id.tab4_switch_alarm);

            refresh();

            final Button btn = rootView.findViewById(R.id.tab4_button_save);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LimitDO limits = new LimitDO();
                    limits.btMac = mSystemCore.getDevice();
                    limits.temperatureLow = edTextTmpLow.getText().length() == 0? null : Float.parseFloat(edTextTmpLow.getText().toString());
                    limits.temperatureHigh = edTextTmpHigh.getText().length() == 0? null : Float.parseFloat(edTextTmpHigh.getText().toString());
                    limits.ph7Low = edTextPHLow.getText().length() == 0? null : Float.parseFloat(edTextPHLow.getText().toString());
                    limits.ph7High = edTextPHHigh.getText().length() == 0? null : Float.parseFloat(edTextPHHigh.getText().toString());
                    limits.ph4 = edTextPH4.getText().length() == 0? null : Float.parseFloat(edTextPH4.getText().toString());
                    limits.doo = edTextDO.getText().length() == 0? null : Float.parseFloat(edTextDO.getText().toString());
                    limits.alarm = swAlarm.isChecked();
                    mSystemCore.saveLimits(limits);
                    btn.setEnabled(false);
                }
            });
            return rootView;
        }

        int refresh() {
            LimitDO limits = mSystemCore.getDeviceLimits(mSystemCore.getDevice());

            txtViewDevice.setText(mSystemCore.getDeviceReadName());
            if(limits.temperatureLow != null)
                edTextTmpLow.setText(String.valueOf(limits.temperatureLow));
            if(limits.temperatureHigh != null)
                edTextTmpHigh.setText(String.valueOf(limits.temperatureHigh));
            if(limits.ph7Low != null)
                edTextPHLow.setText(String.valueOf(limits.ph7Low));
            if(limits.ph7High != null)
                edTextPHHigh.setText(String.valueOf(limits.ph7High));
            if(limits.ph4 != null)
                edTextPH4.setText(String.valueOf(limits.ph4));
            if(limits.doo != null)
                edTextDO.setText(String.valueOf(limits.doo));
            swAlarm.setChecked(limits.alarm);
            return 0;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(0 == position) {
                return MonitorFragment.newInstance(position + 1);
            } else if(1 == position) {
                return CalibrationFragment.newInstance(position + 1);
            } else if(2 == position) {
                return DeviceFragment.newInstance(position + 1);
            } else if(3 == position) {
                return SettingsFragment.newInstance(position + 1);
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
