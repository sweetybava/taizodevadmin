package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_countries_cities")
@ToString
public class CountryCitiesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="country_id")
	    private int countryId;
	 
	 @Column(name="city")
	 private String city;

	public CountryCitiesModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public int getCountryId() {
		return countryId;
	}

	public String getCity() {
		return city;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public void setCity(String city) {
		this.city = city;
	}




	 
	 

}
