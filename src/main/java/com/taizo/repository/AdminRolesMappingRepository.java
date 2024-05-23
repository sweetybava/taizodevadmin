package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import com.taizo.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.AdminRolesMappingModel;

public interface AdminRolesMappingRepository extends JpaRepository<AdminRolesMappingModel, Integer> {


	List<AdminRolesMappingModel> findByAdmin(Admin admin);
	
	List<AdminRolesMappingModel> findByAdminId(Long adminId);



}
