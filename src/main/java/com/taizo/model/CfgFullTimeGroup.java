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
@Table(name = "cfg_fulltime_group")
@Getter
@Setter
@ToString
public class CfgFullTimeGroup {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;

	@Column(name="industry_id")
	private int industryId;
	
	@Column(name = "group_id")
	private int groupId;
	
	@Column(name = "group_name")
	private String groupName;
	
	@Column(name = "active")
	private boolean active;


}
