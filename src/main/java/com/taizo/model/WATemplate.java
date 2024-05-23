package com.taizo.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WATemplate {
	
	@SerializedName("templateName")
	@Expose
	public String templateName;
	@SerializedName("bodyValues")
	@Expose
	public WABodyValues bodyValues;
	@SerializedName("buttonValues")
	@Expose
	public List<WAButtonValues> buttonValues = null;
}
