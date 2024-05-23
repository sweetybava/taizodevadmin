package com.taizo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WARecipient {
	@SerializedName("name")
	@Expose
	public String Name;
	@SerializedName("phone")
	@Expose
	public String phone;


}
