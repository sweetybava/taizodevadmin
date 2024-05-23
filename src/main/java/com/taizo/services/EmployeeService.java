package com.taizo.services;

import com.taizo.models.EmployeeModel;

public interface EmployeeService {

	EmployeeModel findByEmailId(String emailId);

	

}
