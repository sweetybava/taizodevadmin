package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.EmployerDocumentsModel;

public interface EmployerDocumentsRepository extends JpaRepository<EmployerDocumentsModel, Long> {

	Optional<EmployerDocumentsModel> findByEmpIdAndDocTitle(int empId, String docTitle);

	List<EmployerDocumentsModel> findByEmpIdAndDocTitleAndActiveTrue(int empId, String docTitle);

	List<EmployerDocumentsModel> findByEmpIdAndActiveTrue(int empId);

}
