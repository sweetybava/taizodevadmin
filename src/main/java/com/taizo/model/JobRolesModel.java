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
@Table(name = "cfg_fulltime_jobroles")
@Getter
@Setter
@ToString
public class JobRolesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="job_roles")
	    private String jobRoles;
	    
	    @Column(name="industry_id")
	    private int industryId;
	    @Column(name="active")
	    private boolean active;
	    
	    @Column(name="emp_order_no")
	    private int empOrderNo;
	    
	    @Column(name="js_order_no")
	    private int jsOrderNo;

}
