package com.taizo.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Example {

    @SerializedName("CampaignID")
    @Expose
    private String campaignID;
    @SerializedName("To")
    @Expose
    private To to;
	/*
	 * @SerializedName("Variables")
	 * 
	 * @Expose private Variables variables;
	 */
    @SerializedName("Parameters")
    @Expose
    private Parameters parameters;
    @SerializedName("Callback")
    @Expose
    private Callback callback;

    public String getCampaignID() {
        return campaignID;
    }

    public void setCampaignID(String campaignID) {
        this.campaignID = campaignID;
    }

    public To getTo() {
        return to;
    }

    public void setTo(To to) {
        this.to = to;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

}
