package com.taizo.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "web_jobs")
@Getter
@Setter
@ToString
public class WebJobsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_id")
	private int id;

	@Column(name = "industry")
	private String industry;
	@Column(name = "job_category")
	private String jobCategory;
	@Column(name = "salary_currency")
	private String salaryCurrency;
	@Column(name = "min_salary")
	private int minSalary;
	@Column(name = "max_salary")
	private int maxSalary;
	@Column(name = "job_min_exp")
	private int jobExp;
	@Column(name = "company_name")
	private String companyName;
	@Column(name = "job_country")
	private String jobCountry;
	@Column(name = "state")
	private String state;
	@Column(name = "job_city")
	private String jobCity;
	@Column(name = "area")
	private String area;
	@Column(name = "job_latitude")
	private String jobLatitude;
	@Column(name = "job_longitude")
	private String jobLongitude;
	@Column(name = "job_location")
	private String jobLocationAddr;
	@Column(name = "employer_personalization")
	private String personalization;
	@Column(name = "contact_person_name")
	private String contactPersonName;
	@Column(name = "mobile_number")
	private String mobileNumber;
	@Column(name = "email_id")
	private String emailId;
	@Column(name = "start_date")
	private String startDate;

	@Column(name = "end_date")
	private String endDate;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	@Column(name = "doc_required")
	private String docRequired;

	@Column(name = "walkin_address")
	private String waddress;

	@Column(name = "walkin_latitude")
	private String walkinLatitude;

	@Column(name = "walkin_longitude")
	private String walkinLongitude;
	@Column(name = "whatsapp_notification")
	private boolean whatsappNoti;

	@Column(name = "employer_id")
	private int employerId;

	@CreationTimestamp
	@Column(name = "created_time", updatable = false)
	protected Date createdTime;

	@UpdateTimestamp
	@ColumnDefault("updated_time")
	protected Date updatedTime;

}
