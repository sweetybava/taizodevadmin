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
@Table(name = "cfg_emp_job_shift_timings")
@Getter
@Setter
@ToString
public class CfgEmpJobShiftTimings {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name = "shift_timing")
	private String shiftTiming;
	
	@Column(name = "shift_type")
	private String shiftType;

}
