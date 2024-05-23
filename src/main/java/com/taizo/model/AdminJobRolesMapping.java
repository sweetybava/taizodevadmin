package com.taizo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "admin_job_roles_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminJobRolesMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "admin_id")
    private Long adminId;

}
