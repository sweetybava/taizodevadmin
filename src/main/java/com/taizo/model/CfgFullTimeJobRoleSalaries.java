package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "cfg_fulltime_jobrole_salaries")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CfgFullTimeJobRoleSalaries {


		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;
		
		@Column(name = "job_roles")
		private String jobRoles;
		
		@Column(name="is_experienced")
		private Boolean isExperienced;
		
		@Column(name="years_of_experience")
		private String yearsOfExperience;
		
		@Column(name="min_salary")
		private String minSalary;

		 @CreationTimestamp
		 @Column(name = "created_time")
		 protected Date createdTime;
}
