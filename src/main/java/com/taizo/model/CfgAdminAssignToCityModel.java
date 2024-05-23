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

@Entity
@Table(name = "cfg_admin_assign_to_cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgAdminAssignToCityModel {
	
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "city_id")
    private Integer cityId;

}
