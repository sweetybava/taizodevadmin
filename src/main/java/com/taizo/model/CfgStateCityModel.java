package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_indian_states_cities")
@ToString
public class CfgStateCityModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="state_id")
	    private int stateId;
	 
	 @Column(name="city")
	 private String city;
	 
	 @Column(name="active")
	 private boolean active;
	 
	    @Column(name="js_order_no")
	    private int jsOrderNo;

	public CfgStateCityModel() {
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getJsOrderNo() {
		return jsOrderNo;
	}

	public void setJsOrderNo(int jsOrderNo) {
		this.jsOrderNo = jsOrderNo;
	}

 

}
