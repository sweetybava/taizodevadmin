package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerFieldLead;
@Repository
public interface EmployerFieldLeadRepository extends JpaRepository<EmployerFieldLead, Long> {

	EmployerFieldLead findByCompanyName(String companyName);

	Optional<EmployerFieldLead> findById(int id);

}
