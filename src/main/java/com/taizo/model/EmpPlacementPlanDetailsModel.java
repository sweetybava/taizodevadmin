package com.taizo.model;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "emp_placement_plan_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmpPlacementPlanDetailsModel {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "employer_id", nullable = false)
	    private int employerId;

	    @Column(name = "payment_id")
	    private int paymentId;
	    
	    @Column(name = "plan_id")
	    private int planId;
	    
	    @Column (name = "industry")
	    private String industry;

	    @Column(name = "job_category", length = 255)
	    private String jobCategory;

	    @Column(name = "no_of_openings", nullable = false)
	    private int  noOfOpenings;

	    @Column(name = "is_experienced", nullable = false)
	    private Boolean isExperienced;
	    
	    @Column(name = "from_source")
	    private String fromSource;
	    
	    @Column(name = "active")
	    private Boolean active;

		@Column(name="min_salary")
	    private int minSalary;

	   @Column(name="max_salary")
	   private int maxSalary;

	   @Column(name="job_min_exp")
	   private int jobMinExp;

	   @Column(name="work_hours")
	   private String workHours;


	   @CreationTimestamp
	   @Column(name = "created_time", updatable = false)
	   protected Timestamp createdTime;


}
