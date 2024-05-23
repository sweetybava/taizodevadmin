package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "emp_job_ratings")
@Getter
@Setter
@ToString
public class EmpJobRatingsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "emp_id")
	private int empId;

	@Column(name = "job_id")
	private int jobId;

	@Column(name = "rating_id")
	private int ratingId;

	@Column(name = "rating_count")
	private int ratingCount;

	@Column(name = "question")
	private String question;

	@Column(name = "reasons")
	private String reasons;

	@Column(name = "from_web")
	private boolean fromWeb;

	@Column(name = "from_app")
	private boolean fromApp;
	
	@CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
}
