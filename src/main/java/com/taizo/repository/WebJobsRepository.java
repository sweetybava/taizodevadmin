package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.BenefitsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.WebJobsModel;

public interface WebJobsRepository extends JpaRepository<WebJobsModel,Long>{

	Optional<WebJobsModel> findById(int id);

}
