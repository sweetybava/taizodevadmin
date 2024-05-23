package com.taizo.model;

import java.sql.Timestamp;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "job_lead")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString


public class JobLeadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "employer_id")
    private int employerId;

    @Column(name = "job_industry", nullable = false)
    private String jobIndustry;

    @Column(name = "job_category", nullable = false)
    private String jobCategory;

    @Column(name = "no_of_openings", nullable = false)
    private int noOfOpenings;

    @Column(nullable = false)
    private int amount;
    
    @Column(name = "assign_to")
    private int assignTo;
    
    @Column( name = "is_experienced")
    private Boolean Experienced;

    @Column(name="min_salary")
    private int minSalary;

    @Column(name="max_salary")
    private int maxSalary;

    @Column(name="job_min_exp")
    private int jobMinExp;

    @Column(name="job_max_exp")
    private int jobMaxExp;

    @Column(name="work_hours")
    private String workHours;

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdTime;

    @Transient
    private String Experience;



}