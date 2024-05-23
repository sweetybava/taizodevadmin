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
@Table(name = "can_ratings")
@Getter
@Setter
@ToString
public class CanRatingsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "can_id")
	private int canId;

	@Column(name = "rating_id")
	private int ratingId;

	@Column(name = "rating_count")
	private int ratingCount;

	@Column(name = "question")
	private String question;

	@Column(name = "reasons")
	private String reasons;
}
