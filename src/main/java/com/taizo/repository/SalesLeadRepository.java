package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateModel;
import com.taizo.model.LeadModel;
import com.taizo.model.SalesLeadModel;

@Repository
public interface SalesLeadRepository extends JpaRepository<SalesLeadModel, Integer> {

}
