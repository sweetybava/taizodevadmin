package com.taizo.repository;

import java.util.List; 
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.Admin;
import com.taizo.model.AdminCallNotiModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerModel;

import okhttp3.HttpUrl.Builder;

@Repository
public interface AdminCallNotiRepository extends JpaRepository<AdminCallNotiModel, Long>{

	 Page<AdminCallNotiModel> findAll(Pageable pageable);
	 
	 Page<AdminCallNotiModel> findBySidIn(List<String> sid, Pageable pageable);

}
