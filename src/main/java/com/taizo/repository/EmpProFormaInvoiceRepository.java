package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.EmpProformaInvoiceModel;

public interface EmpProFormaInvoiceRepository extends JpaRepository<EmpProformaInvoiceModel, Integer> {

	EmpProformaInvoiceModel findByPaymentReferenceId(String paymentReferenceId);

}
