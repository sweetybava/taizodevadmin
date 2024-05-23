package com.taizo.beans;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FullReportRow {

	// user
	private Long job_id;
	private String jobCategory;
	private String candidateFirstName;
	private String candidateLastName;
	private int age;
	private int experience;
	private String currentLocation;
	private String permLocation;	
	private String userMobile;
	private String userEmail;
	private String education;
	private String specialization;
	private String keySkills;
	private String certificationCourses;
	private String time;
	private String status;

}
