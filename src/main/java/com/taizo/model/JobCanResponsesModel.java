package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "job_can_responses")
@Getter
@Setter
@ToString
public class JobCanResponsesModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "can_id")
	private int canId;

	@Column(name = "job_id")
	private int jobId;

	@Column(name = "response_count")
	private int responseCount;

	@Column(name = "response")
	private String response;
}
