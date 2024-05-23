package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.LeadModel;
import com.taizo.model.MidSeniorLevelCandidateLeadModel;
@Repository
public interface MidSeniorLevelCandidateLeadRepository extends JpaRepository<MidSeniorLevelCandidateLeadModel, Long> {

	MidSeniorLevelCandidateLeadModel findByMobileNumber(String mobileNumber);

	@Query("SELECT c FROM MidSeniorLevelCandidateLeadModel c WHERE c.mobileNumber = :mobileNumber OR c.whatsappNumber = :whatsappNumber")
	MidSeniorLevelCandidateLeadModel findByMobileNumberAndWhatsappNumber(String mobileNumber, String whatsappNumber);

	Page<MidSeniorLevelCandidateLeadModel> findAll(Specification<MidSeniorLevelCandidateLeadModel> spec, Pageable pageable);

}
