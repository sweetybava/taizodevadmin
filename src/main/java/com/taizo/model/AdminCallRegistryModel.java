package com.taizo.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_call_registry")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCallRegistryModel {
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "candidate_id")
    private Integer candidateId;

    @Column(name = "employer_id")
    private Integer employerId;

    @Column(name = "call_time")
    private String callTime;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    protected Timestamp createdTime;

}
