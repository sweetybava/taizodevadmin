package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CampaignReportModel;
import com.taizo.model.EmployerJobPersonalizationModel;
import com.taizo.model.PlansModel;

@Repository
public interface CampaignReportRepository extends JpaRepository<CampaignReportModel, Long>, 
	JpaSpecificationExecutor<CampaignReportModel> {

}
