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
@Table(name = "cfg_can_sources")
@Getter
@Setter
@ToString
public class CfgCanSources {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="source")
	    private String source;
	    
	    @Column(name="icon")
	    private String icon;
	    
	    @Column(name="active")
	    private boolean active;

}
