package com.taizo.repositorys;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateModel;
import com.taizo.models.EmployeeModel;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeModel, Long>,JpaSpecificationExecutor<EmployeeModel> {
	
	EmployeeModel findByEmail(String emailId);

}
