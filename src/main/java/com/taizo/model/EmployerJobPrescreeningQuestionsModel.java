package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "emp_job_prescreening_questions")
@ToString
public class EmployerJobPrescreeningQuestionsModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="emp_id")
	    private int empId;	
	    
	    @Column(name="job_id")
	    private int jobId;	 
	    
	    @Column(name="question_id")
	    private int questionId;	 
	    
	    @Column(name="answer")
	    private String answer;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getEmpId() {
			return empId;
		}

		public void setEmpId(int empId) {
			this.empId = empId;
		}

		public int getJobId() {
			return jobId;
		}

		public void setJobId(int jobId) {
			this.jobId = jobId;
		}

		public int getQuestionId() {
			return questionId;
		}

		public void setQuestionId(int questionId) {
			this.questionId = questionId;
		}

		public String getAnswer() {
			return answer;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
		}

	
		
		
}
