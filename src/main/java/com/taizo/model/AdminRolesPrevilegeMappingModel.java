package com.taizo.model;

import java.sql.Timestamp;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "admin_roles_privilege_mapping")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminRolesPrevilegeMappingModel {
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(cascade = CascadeType.ALL) 
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id")
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private CfgAdminRolesModel roleId;

    @ManyToOne
    @JoinColumn(name = "privilege_id", referencedColumnName = "id")
    private CfgAdminPrevilegeModel privilegeId;

    @Column(name = "`create`")
    private boolean create;

    @Column(name = "`read`")
    private boolean read;

    @Column(name = "`update`")
    private boolean update;

    @Column(name = "`delete`")
    private boolean delete;
    
    
  
    
    

}
