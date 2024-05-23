package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_key_skills")
@ToString
public class KeySkillsModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="skill")
	 private String skill;
	 
	    @Column(name="emp_order_no")
	    private int empOrderNo;
	    
	    @Column(name="js_order_no")
	    private int jsOrderNo;

	public KeySkillsModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public int getEmpOrderNo() {
		return empOrderNo;
	}

	public void setEmpOrderNo(int empOrderNo) {
		this.empOrderNo = empOrderNo;
	}

	public int getJsOrderNo() {
		return jsOrderNo;
	}

	public void setJsOrderNo(int jsOrderNo) {
		this.jsOrderNo = jsOrderNo;
	}

	


	 
	 

}
