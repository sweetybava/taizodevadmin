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
import lombok.ToString;

@Entity
@Table(name = "employer_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployerDocumentsModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "emp_lead_id")
    private int empLeadId;

    @Column(name = "emp_id")
    private int empId;

    @Column(name = "doc_link")
    private String docLink;

    @Column(name = "doc_title")
    private String docTitle;

    @Column(name = "doc_key")
    private String docKey;

    @Column(name = "active")
    private boolean active;
    
    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;


}
