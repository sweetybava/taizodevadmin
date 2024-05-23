package com.taizo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WAAlert {
	
	@SerializedName("channelId")
	@Expose
	public String channelId;
	@SerializedName("channelType")
	@Expose
	public String channelType;
	@SerializedName("recipient")
	@Expose
	public WARecipient recipient;
	@SerializedName("whatsapp")
	@Expose
	public WADetails whatsapp;
	
	
}
