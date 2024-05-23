package com.taizo.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.Admin;
import com.taizo.model.CfgAdminRolesModel;

public interface CfgAdminRolesRepository extends JpaRepository<CfgAdminRolesModel,Integer> {

    CfgAdminRolesModel findByRoleName(String roleName);

	//Optional<Admin> findById(Long roleId);
	
	 Optional<CfgAdminRolesModel> findById(Long roleId);

	


}
