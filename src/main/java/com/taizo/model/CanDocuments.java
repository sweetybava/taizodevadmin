package com.taizo.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "candidate_documents")
@Getter
@Setter
@ToString
public class CanDocuments {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;

	@Column(name = "doc_link")
	private String docLink;

	@Column(name = "doc_title")
	private String docTitle;

	@Column(name = "doc_key")
	private String docKey;
	
	@Column(name = "admin_id")
	private long adminId;
	
//	@Column(name = "can_id")
//	private int canId;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "can_id", nullable = true)
	private CandidateModel documents;

	
	
	 @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;


}
