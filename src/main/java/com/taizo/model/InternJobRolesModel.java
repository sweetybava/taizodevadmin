package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_intern_jobroles")
@ToString
public class InternJobRolesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="job_roles")
	    private String jobRoles;
	    
	    @Column(name="industry_id")
	    private int industryId;

		public InternJobRolesModel() {
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

		public int getIndustryId() {
			return industryId;
		}

		public void setIndustryId(int industryId) {
			this.industryId = industryId;
		}	
	    
	    

}
