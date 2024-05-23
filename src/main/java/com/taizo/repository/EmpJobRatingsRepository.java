package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.JobLeadModel;

@Repository
public interface EmpJobRatingsRepository extends JpaRepository<EmpJobRatingsModel, Long>{

    @Query(value = "SELECT e FROM EmpJobRatingsModel e where e.empId = ?1 and e.jobId = ?2 ")
    EmpJobRatingsModel findByJobIdandEmpId(int empId, int jobId);

    List<EmpJobRatingsModel> findByRatingCount(int ratingCount);
	
	 Page<EmpJobRatingsModel> findByRatingCount (int ratingCount, Pageable pageable);
}
