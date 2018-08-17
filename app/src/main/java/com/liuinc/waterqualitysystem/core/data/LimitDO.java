package com.liuinc.waterqualitysystem.core.data;

import com.google.gson.annotations.SerializedName;

public class LimitDO {
    @SerializedName("bt_mac")
    public String btMac = "";

    @SerializedName("temperature_high")
    public float temperatureHigh = 0;

    @SerializedName("temperature_low")
    public float temperatureLow = 0;

    @SerializedName("ph7_high")
    public float ph7High = 0;

    @SerializedName("ph7_low")
    public float ph7Low = 0;

    @SerializedName("ph4")
    public float ph4 = 0;

    @SerializedName("do")
    public float doo = 0;

    @SerializedName("alarm")
    public boolean alarm = false;
}
