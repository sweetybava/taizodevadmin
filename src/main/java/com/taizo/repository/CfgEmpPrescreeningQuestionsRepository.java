package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import com.taizo.model.CfgEmpPrescreeningQuestionsModel;
import com.taizo.model.EmployerJobPersonalizationModel;

public interface CfgEmpPrescreeningQuestionsRepository  extends JpaRepository<CfgEmpPrescreeningQuestionsModel, Long> {

	@Query("select c from CfgEmpPrescreeningQuestionsModel c where c.type = :status")
	List<CfgEmpPrescreeningQuestionsModel> finByType(@Param("status")String status);
	

	
	
}
