package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.BenefitsModel;
import com.taizo.model.CfgInterRequiredDocModel;

public interface InterviewRequiredDocRepository extends JpaRepository<CfgInterRequiredDocModel,Integer>{

	List<CfgInterRequiredDocModel> findAllByActive(boolean b);

}
