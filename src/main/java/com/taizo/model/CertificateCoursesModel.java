package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_certificate_courses")
@ToString
public class CertificateCoursesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="courses")
	    private String courses;
	    
	    @Column(name="js_order_no")
	    private int jsOrderNo;

		public CertificateCoursesModel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}


		public void setId(int id) {
			this.id = id;
		}

		public String getCourses() {
			return courses;
		}

		public void setCourses(String courses) {
			this.courses = courses;
		}

		public int getJsOrderNo() {
			return jsOrderNo;
		}

		public void setJsOrderNo(int jsOrderNo) {
			this.jsOrderNo = jsOrderNo;
		}


	    
	    

}
