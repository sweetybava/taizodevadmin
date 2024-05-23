package com.taizo.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees", schema = "TaizoTeam")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmployeeModel {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="employee_id")
	private int employeeId;
	
	@Column(name="employee_name")
	private String employeeName;
	
	@Column(name="email")
	private String email;
	
	@Column(name="password")
	private String password;
	
	@Column(name = "hire_date")
	@CreationTimestamp
	protected Date hireDate;

		@Column(name="department")
		private String department;
		
		@Column(name="position")
		private String position;
}
