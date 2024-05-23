package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_india_states_cities")
@ToString
public class StateCityModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="state_id")
	    private int stateId;
	 
	 @Column(name="city")
	 private String city;
	 
	    @Column(name="js_order_no")
	    private int jsOrderNo;

	public StateCityModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public int getStateId() {
		return stateId;
	}

	public String getCity() {
		return city;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getJsOrderNo() {
		return jsOrderNo;
	}

	public void setJsOrderNo(int jsOrderNo) {
		this.jsOrderNo = jsOrderNo;
	}



	 
	 

}
