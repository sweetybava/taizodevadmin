package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.EmpEnquiryModel;
import com.taizo.model.EmployerModel;

public interface EmpEnquiryRepository extends JpaRepository<EmpEnquiryModel, Long> {


}
