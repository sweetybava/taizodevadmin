package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.CanDocuments;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CanRatingsModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.JobCanResponsesModel;

@Repository
public interface JobCanResponsesRepository extends JpaRepository<JobCanResponsesModel, Long>{

	JobCanResponsesModel findById(int id);

	@Query("select c from JobCanResponsesModel c where c.jobId = :jobId and c.canId = :canId")
	JobCanResponsesModel findByCanJobId(int jobId, int canId);

}
