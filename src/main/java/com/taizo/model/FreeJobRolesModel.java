package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_freelancer_jobroles")
@ToString
public class FreeJobRolesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="job_roles")
	    private String jobRoles;

		public FreeJobRolesModel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}

		public String getJobRoles() {
			return jobRoles;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setJobRoles(String jobRoles) {
			this.jobRoles = jobRoles;
		}	
	    
	    

}
