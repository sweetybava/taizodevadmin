package com.taizo.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.ToString;

@Entity
@Table(name = "candidate_languages")
@ToString
public class CanLanguageModel {
	

	@Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id",unique = true, nullable = false)
	    private int id;
	
	   @Column(name="language_id",nullable=true)
	   private int languageId;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="candidate_id",nullable=true)
	private CandidateModel candidate;

	public CanLanguageModel(int id, int languageId, CandidateModel candidate) {
		super();
		this.id = id;
		this.languageId = languageId;
		this.candidate = candidate;
	}

	public CanLanguageModel() {
		// TODO Auto-generated constructor stub
	}

	public CanLanguageModel(List<CanLanguageModel> lang) {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public int getLanguageId() {
		return languageId;
	}
	
	public CandidateModel getCandidate() {
		return candidate;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}
	
	public void setCandidate(CandidateModel candidate) {
		this.candidate = candidate;
	}
	
	
}
