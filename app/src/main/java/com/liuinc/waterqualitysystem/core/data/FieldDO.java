package com.liuinc.waterqualitysystem.core.data;

import com.google.gson.annotations.SerializedName;

public class FieldDO {
    @SerializedName("bt_mac")
    public String btMac = "";

    @SerializedName("id")
    public String id = "";

    @SerializedName("ts_id")
    public String thinkSpeakId = "";

    @SerializedName("alias")
    public String aliasName = "";

    @SerializedName("wifi_id")
    public String wifiId;

    @SerializedName("wifi_pswd")
    public String wifiPassword;
}
