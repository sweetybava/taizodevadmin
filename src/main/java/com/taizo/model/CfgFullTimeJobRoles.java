package com.taizo.model;


import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "cfg_fulltime_jobroles")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CfgFullTimeJobRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_roles", columnDefinition = "VARCHAR_IGNORE CASE")
    private String jobRoles;

    @Column(name = "category")
    private String category;

    @Column(name = "industry_id")
    private int industryId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "emp_order_no")
    private int empOrderNo;

    @Column(name = "js_order_no")
    private int jsOrderNo;

    @Column(name = "group_id")
    private int groupId;





}
