package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_india_states")
@ToString
public class IndiaStateModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="state")
	 private String state;
	 
	 
	 @Column(name="specification")
	 private String specification;
	 
	    @Column(name="js_order_no")
	    private int jsOrderNo;

	public IndiaStateModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public int getJsOrderNo() {
		return jsOrderNo;
	}

	public void setJsOrderNo(int jsOrderNo) {
		this.jsOrderNo = jsOrderNo;
	}





	 
	 

}
