package com.taizo.model;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
	
	@Entity
	@Table(name = "admin_roles_mapping")
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public class AdminRolesMappingModel {
		
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id")
	    private Long id;

	    @ManyToOne(cascade = CascadeType.ALL) 
	    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id")
	    private Admin admin;

	    @ManyToOne
	    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
	    private CfgAdminRolesModel roleId;

	    @CreationTimestamp
		@Column(name = "created_time", updatable = false)
		protected Timestamp createdTime;

	    @Column(name = "updated_time")
	    private Timestamp updatedTime;
	    

}
