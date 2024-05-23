package com.taizo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WADetails {
	@SerializedName("type")
	@Expose
	public String type;
	@SerializedName("template")
	@Expose
	public WATemplate template;
}
