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

@Getter
@Setter
@ToString
public class EmpJobStatusCountModel {
	
    private int appliedCount;
    private int savedCount;
    private long callCount;
    private int shortListedCount;
  
	
	}
