package com.taizo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WAButtonValues {

	@SerializedName("index")
	@Expose
	public Integer index;
	@SerializedName("sub_type")
	@Expose
	public String subType;
	@SerializedName("parameters")
	@Expose
	public WAParameters parameters;
}
