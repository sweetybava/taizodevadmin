package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgLanguagesModel;
import com.taizo.model.CfgRatingCountsModel;
import com.taizo.model.CfgRatingReasonsModel;
import com.taizo.model.EmployerJobPersonalizationModel;


@Repository
public interface CfgRatingReasonsRepository extends JpaRepository<CfgRatingReasonsModel, Long> {


}
