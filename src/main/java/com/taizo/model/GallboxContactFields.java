package com.taizo.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GallboxContactFields {
	
	public String exp_in_years;
	public String language_key;
	public String city;
	public String exp_in_manufacturing;
	public String industry;
	public String job_role;
}
