package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_graduation_courses")
@ToString
public class GraduationCoursesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="courses")
	    private String courses;

		public GraduationCoursesModel() {
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


	    
	    

}
