package com.taizo.controller.employee;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.models.EmployeeModel;
import com.taizo.services.EmployeeService;

@CrossOrigin
@RestController
@RequestMapping("/webEmployee")
public class EmployeeController {
	
	@Autowired
	EmployeeService employeeService;

	 @GetMapping("/employeeLoginDetails")
	 public ResponseEntity<Map<String, Object>> login(@RequestParam String emailId, @RequestParam String password) {
	     try {
	         EmployeeModel employee = employeeService.findByEmailId(emailId);

	         if (employee != null) {
	     
	             String hashedPassword = employee.getPassword();

	             BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	         
	             if (passwordEncoder.matches(password, hashedPassword) || password.equals(hashedPassword)) {
	                 // Passwords match
	                 Map<String, Object> response = new HashMap<>();
	                 response.put("statuscode", 200);
	                 response.put("message", "Login successful");
	                 return ResponseEntity.ok(response);

	             } else {
	                 // Passwords don't match
	                 Map<String, Object> errorResponse = new HashMap<>();
	                 errorResponse.put("statuscode", 400);
	                 errorResponse.put("message", "Login failed: Incorrect password");
	                 return ResponseEntity.badRequest().body(errorResponse);
	             }
	         } else {
	             // Admin not found
	             Map<String, Object> errorResponse = new HashMap<>();
	             errorResponse.put("statuscode", 400);
	             errorResponse.put("message", "Login failed: Employee not found");
	             return ResponseEntity.badRequest().body(errorResponse);
	         }
	     } catch (RuntimeException e) {
	         // Handle exceptions
	         Map<String, Object> errorResponse = new HashMap<>();
	         errorResponse.put("statuscode", 400);
	         errorResponse.put("message", "Login failed: " + e.getMessage());
	         return ResponseEntity.badRequest().body(errorResponse);
	     }
	 }
}
