package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.MidSeniorCandidateReportModel;
@Repository
public interface MidSeniorCandidateReportRepository extends JpaRepository<MidSeniorCandidateReportModel, Long> {

	List<MidSeniorCandidateReportModel> findByMidSeniorId(long id);

}
