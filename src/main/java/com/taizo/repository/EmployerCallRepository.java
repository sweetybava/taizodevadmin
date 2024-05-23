package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.CandidateCallModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.EmployerCallModel;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface EmployerCallRepository extends JpaRepository<EmployerCallModel, Long> , JpaSpecificationExecutor<EmployerCallModel>{

	
	@Query(value = "SELECT * ,count(*) as count FROM employer_call_registry u where u.cid=?1 group by u.cid,u.empid,u.jid ORDER BY u.call_time desc LIMIT 30", nativeQuery = true)
	List<EmployerCallModel> getCallDetails(Integer cId);

	 @Query( "SELECT u FROM EmployerCallModel u where u.jId=?1 ")
	Page<EmployerCallModel> getCanCallNotificationCount(int id, PageRequest of);

	 @Query( "SELECT u FROM EmployerCallModel u where u.jId=?1 ")

	Page<EmployerCallModel> findByjId(int jId, Pageable pageable);  
	
}
