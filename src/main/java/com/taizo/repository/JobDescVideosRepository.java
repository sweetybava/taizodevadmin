package com.taizo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.taizo.model.JobDescVideosModel;
import com.taizo.model.SampleVideosModel;

@Repository
public interface JobDescVideosRepository extends JpaRepository<JobDescVideosModel, Long>, 
	JpaSpecificationExecutor<JobDescVideosModel> {

}
