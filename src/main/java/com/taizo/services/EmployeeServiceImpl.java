package com.taizo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.taizo.models.EmployeeModel;
import com.taizo.repositorys.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	EmployeeRepository employeeRepository;

	public EmployeeModel findByEmailId(String emailId) {
		return employeeRepository.findByEmail(emailId);
	}
}
