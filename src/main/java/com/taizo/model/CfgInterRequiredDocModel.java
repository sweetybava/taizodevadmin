package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_interview_required_doc")
@ToString
public class CfgInterRequiredDocModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="required_document")
	    private String requiredDoc;
	    
	    @Column(name="active")
	    private boolean active;

		public CfgInterRequiredDocModel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getRequiredDoc() {
			return requiredDoc;
		}

		public void setRequiredDoc(String requiredDoc) {
			this.requiredDoc = requiredDoc;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}





	    
	    

}
