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
@Table(name = "cfg_can_admin_area_grouping")
@Getter
@Setter
@ToString
public class CfgCanAdminArea {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="areas")
	 private String areas;
	 
	 @Column(name="city_id")
	 private int cityId;
	 
	 @Column(name="assigned_to_admin_id")
	 private int assingnedToAdminId;
	 
	 @Column(name="active")
	 private boolean active;
}
