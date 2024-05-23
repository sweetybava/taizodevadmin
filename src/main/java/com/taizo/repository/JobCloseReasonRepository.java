package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CityModel;
import com.taizo.model.JobCloseReasonModel;

public interface JobCloseReasonRepository extends JpaRepository<JobCloseReasonModel,Long>{

}
