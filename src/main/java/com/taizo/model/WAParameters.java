package com.taizo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WAParameters {

	@SerializedName("type")
	@Expose
	public String type;
	@SerializedName("text")
	@Expose
	public String text;
	@SerializedName("payload")
	@Expose
	public String payload;
	
	@SerializedName("url")
	@Expose
	public String url;

}
