package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_states")
@ToString
public class CfgStateModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="country_id")
	 private int countryId;
	 
	 @Column(name="state")
	 private String state;
	 
	    @Column(name="js_order_no")
	    private int jsOrderNo;
	 
	 @Column(name="active")
	 private boolean active;
	 
	 


	public CfgStateModel() {
		super();
		// TODO Auto-generated constructor stub
	}




	public int getId() {
		return id;
	}




	public void setId(int id) {
		this.id = id;
	}




	public int getCountryId() {
		return countryId;
	}




	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}




	public String getState() {
		return state;
	}




	public void setState(String state) {
		this.state = state;
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
