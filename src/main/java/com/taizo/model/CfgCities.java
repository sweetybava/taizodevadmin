package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cfg_cities")
@Getter
@Setter
@ToString
public class CfgCities {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="emp_order_no")
	    private int empOrderNo;
	 
	 @Column(name="js_order_no")
	    private int jsOrderNo;
	 
	 @Column(name="admin_id")
	    private long adminId;
	 
	 @Column(name="city")
	 private String city;
	 
	 @Column(name="active")
	 private boolean active;
	 
	 @Column(name="category")
	 private String category;
}
