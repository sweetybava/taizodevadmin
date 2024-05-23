package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CandidateCallModel;
import com.taizo.model.EmployerCallModel;

import java.util.Date;
import java.util.Optional;

public interface CandidateCallRepository extends JpaRepository<CandidateCallModel, Long> {

	 @Query(value = "SELECT u FROM CandidateCallModel u where u.cId=?1 and u.empId=?2 and u.jId=?3 and call_time=(select max(u.callTime) from CandidateCallModel u where u.cId=?1 and u.empId=?2 and u.jId=?3)")
	 Optional<CandidateCallModel> getCallDetails(Integer cId, Integer empId, Integer jId);

	 @Query(value = "SELECT u FROM CandidateCallModel u where u.empId=?1 and u.callTime > ?2")
	 Page<CandidateCallModel> getEmployerCallNotificationCount(Integer empId, Date date, Pageable pageable);

	 @Query( "SELECT u FROM CandidateCallModel u where u.jId=?1 ")
	Page<CandidateCallModel> getCanCallNotificationCount(int id, PageRequest of);

	

	


}
