package com.liuinc.waterqualitysystem.core.data;

import java.io.Serializable;

public class BoundaryDTO implements Serializable {
    /**
     * Auto generate
     */
    private static final long serialVersionUID = -5053412967314724078L;

    private String btMac;

    private String thinkSpeakId;

    private Float temperatureHigh;

    private Float temperatureLow;

    private Float ph7High;

    private Float ph7Low;

    private Float ph4;

    private Float doo;

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

    public Float getTemperatureHigh() {
        return temperatureHigh;
    }

    public void setTemperatureHigh(Float temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }

    public Float getTemperatureLow() {
        return temperatureLow;
    }

    public void setTemperatureLow(Float temperatureLow) {
        this.temperatureLow = temperatureLow;
    }

    public Float getPh7High() {
        return ph7High;
    }

    public void setPh7High(Float ph7High) {
        this.ph7High = ph7High;
    }

    public Float getPh7Low() {
        return ph7Low;
    }

    public void setPh7Low(Float ph7Low) {
        this.ph7Low = ph7Low;
    }

    public Float getPh4() {
        return ph4;
    }

    public void setPh4(Float ph4) {
        this.ph4 = ph4;
    }

    public Float getDoo() {
        return doo;
    }

    public void setDoo(Float doo) {
        this.doo = doo;
    }
}
