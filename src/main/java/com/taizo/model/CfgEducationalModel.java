package com.taizo.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cfg_education_qualification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CfgEducationalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "EducationQualification")
    private String educationQualification;
    
    @Column(name = "fb_meta_leads_active")
    private boolean fbMetaLeadsActive;
    
    @Column(name = "active")
    private boolean active;
    
    @Column(name = "category")
    private String category;
}
