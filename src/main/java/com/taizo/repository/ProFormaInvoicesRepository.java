package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.EmpProformaInvoiceModel;

public interface ProFormaInvoicesRepository extends JpaRepository<EmpProformaInvoiceModel, Long> {

}
