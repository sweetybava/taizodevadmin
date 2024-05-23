package com.taizo.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GallaboxContactUpdate {

	public String name;
	public List<String> phone;
	public GallboxContactFields fieldValues;

}
