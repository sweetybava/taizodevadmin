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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "cfg_can_admin_city_grouping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgCanAdminCityGrouping {
	
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "city_id", nullable = false, columnDefinition = "int default 0")
    private Integer cityId;

    @Column(name = "city_name", length = 255)
    private String cityName;

    @Column(name = "admin_id", nullable = false, columnDefinition = "int default 0")
    private Integer adminId;

    @Column(name = "group_id", nullable = false, columnDefinition = "int default 0")
    private Integer groupId;

    @Column(name = "active", nullable = false, columnDefinition = "tinyint default 0")
    private Boolean active;

    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;

}
