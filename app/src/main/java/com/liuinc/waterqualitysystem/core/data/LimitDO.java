package com.liuinc.waterqualitysystem.core.data;

import com.google.gson.annotations.SerializedName;

public class LimitDO {
    @SerializedName("bt_mac")
    public String btMac = "";

    @SerializedName("temperature_high")
    public Float temperatureHigh;

    @SerializedName("temperature_low")
    public Float temperatureLow;

    @SerializedName("ph7_high")
    public Float ph7High;

    @SerializedName("ph7_low")
    public Float ph7Low;

    @SerializedName("ph4")
    public Float ph4;

    @SerializedName("do")
    public Float doo;

    @SerializedName("alarm")
    public boolean alarm = false;
}
