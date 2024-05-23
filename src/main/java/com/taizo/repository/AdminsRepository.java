package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.taizo.model.Admin;

public interface AdminsRepository extends JpaRepository<Admin, Long>, JpaSpecificationExecutor<Admin>  {
	
	Admin findById(String adminId);

}
