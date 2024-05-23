package com.taizo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "employer_timeline")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_id")
    private int empId;

    @Column(name = "emp_lead_id")
    private int empLeadId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "notes")
    private  String notes;
    
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    protected Timestamp createdTime;



}
