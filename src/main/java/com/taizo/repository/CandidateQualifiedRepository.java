package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateQualifiedModel;
@Repository
public interface CandidateQualifiedRepository extends JpaRepository<CandidateQualifiedModel, Long> {

	Optional<CandidateQualifiedModel> findByCanLeadId(int leadid);


}
