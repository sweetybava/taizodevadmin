package com.taizo.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.EmployerInvoiceModel;

public interface EmployerInvoiceRepository extends JpaRepository<EmployerInvoiceModel, Long> {
	
	
	  @Query("SELECT e FROM EmployerInvoiceModel e " +
	            "WHERE (:empId IS NULL OR e.empId = :empId) " +
	            "AND (:adminId IS NULL OR e.adminId = :adminId) " +
	            "AND (:emailId IS NULL OR e.emailId = :emailId) " +
	            "AND (:invoiceNo IS NULL OR e.invoiceNo = :invoiceNo) " +
	            "AND (:invoiceAmount IS NULL OR e.invoiceAmount = :invoiceAmount) " +
	            "AND (:invoiceDate IS NULL OR e.invoiceDate = :invoiceDate) " +
	            "AND (:dueDate IS NULL OR e.dueDate = :dueDate) " +
	            "AND (:paid IS NULL OR e.paid = :paid) " +
	            "AND (:startDate IS NULL OR e.createdTime >= :startDate) " +
	            "AND (:endDate IS NULL OR e.createdTime <= :endDate)")
	    Page<EmployerInvoiceModel> findByFilter(
	            Integer empId,
	            Long adminId,
	            String emailId,
	            String invoiceNo,
	            Long invoiceAmount,
	            String invoiceDate,
	            String dueDate,
	            Boolean paid,
	            Date startDate,
	            Date endDate,
	            Pageable pageable);
}
