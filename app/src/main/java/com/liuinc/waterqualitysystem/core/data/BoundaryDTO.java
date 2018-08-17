package com.liuinc.waterqualitysystem.core.data;

import java.io.Serializable;

public class BoundaryDTO implements Serializable {
    /**
     * Auto generate
     */
    private static final long serialVersionUID = -5053412967314724078L;

    private String btMac;

    private String thinkSpeakId;

    private float temperatureHigh = 0;

    private float temperatureLow = 0;

    private float ph7High = 0;

    private float ph7Low = 0;

    private float ph4 = 0;

    private float doo = 0;

    public BoundaryDTO(String btMac, String thinkSpeakId) {
        this.btMac = btMac;
        this.thinkSpeakId = thinkSpeakId;
    }

    public String getBtMac() {
        return btMac;
    }

    public void setBtMac(String btMac) {
        this.btMac = btMac;
    }

    public String getThinkSpeakId() {
        return thinkSpeakId;
    }

    public void setThinkSpeakId(String thinkSpeakId) {
        this.thinkSpeakId = thinkSpeakId;
    }

    public float getTemperatureHigh() {
        return temperatureHigh;
    }

    public void setTemperatureHigh(float temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }

    public float getTemperatureLow() {
        return temperatureLow;
    }

    public void setTemperatureLow(float temperatureLow) {
        this.temperatureLow = temperatureLow;
    }

    public float getPh7High() {
        return ph7High;
    }

    public void setPh7High(float ph7High) {
        this.ph7High = ph7High;
    }

    public float getPh7Low() {
        return ph7Low;
    }

    public void setPh7Low(float ph7Low) {
        this.ph7Low = ph7Low;
    }

    public float getPh4() {
        return ph4;
    }

    public void setPh4(float ph4) {
        this.ph4 = ph4;
    }

    public float getDoo() {
        return doo;
    }

    public void setDoo(float doo) {
        this.doo = doo;
    }
}
