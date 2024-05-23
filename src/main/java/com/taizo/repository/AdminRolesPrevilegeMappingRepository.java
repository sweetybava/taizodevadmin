package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.Admin;
import com.taizo.model.AdminRolesPrevilegeMappingModel;

public interface AdminRolesPrevilegeMappingRepository extends JpaRepository<AdminRolesPrevilegeMappingModel, Long> {

	    List<AdminRolesPrevilegeMappingModel> findByRoleId_Id(Long roleId);

}
