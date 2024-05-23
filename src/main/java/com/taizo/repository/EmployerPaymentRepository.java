package com.taizo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerPaymentModel;


@Repository
public interface EmployerPaymentRepository extends JpaRepository<EmployerPaymentModel, Long> {
	
	@Query("select j from EmployerPaymentModel j where j.employerId = :employerId")
	List<EmployerPaymentModel> findEmployerPaymentHistory(@Param("employerId") int employerId);

	EmployerPaymentModel findById(int paymentID);

    @Query(value = "SELECT * FROM TaizoDB.employer_payment where employer_id=:employerId and captured is true order by id desc limit 1;", nativeQuery = true)
	EmployerPaymentModel findEmployerPayment(int employerId);

    @Query(value = "SELECT * FROM TaizoDB.employer_payment where captured is true order by id desc limit 1,1;", nativeQuery = true)
	EmployerPaymentModel findByInvoiceId();
  
   
    Page<EmployerPaymentModel> findByEmployerIdOrderByCreatedTimeDesc(int employerId, Pageable pageable);

    Page<EmployerPaymentModel> findAllByOrderByCreatedTimeDesc(Pageable pageable);

    long countByEmployerId(int employerId);

    long count();

    @Query(value = "SELECT * FROM TaizoDB.employer_payment where lead_id=:leadId and captured is true order by id desc limit 1;", nativeQuery = true)
	EmployerPaymentModel findByLeadId(int leadId);

	EmployerPaymentModel findByEmailId(String payments);

	

}
