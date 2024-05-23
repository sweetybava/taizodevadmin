package com.taizo.model;
	
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "cfg_admin_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgAdminRolesModel {
	
	
        @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "role_id")
	    private Long id;

	    @Column(name = "role_name")
	    private String roleName;

	    @Column(name = "description")
	    private String description;

	    @JsonIgnore
	    @Column(name = "created_time")
	    private Timestamp createdTime;

	    @JsonIgnore
	    @Column(name = "updated_time")
	    private Timestamp updatedTime;

}
