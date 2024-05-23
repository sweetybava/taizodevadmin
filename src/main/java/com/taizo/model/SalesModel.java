package com.taizo.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_faq_videos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SalesModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@Column(name="admin_id")
	private int adminId;
	
	@Column(name="active")
	private boolean active;
	
	@Column(name = "faq_name")
	private String faqName;
	
	@Column(name = "video_link")
	private String videoLink;

	@Column(name = "video_description")
	private String videoDescription;
	
	@CreationTimestamp
	 @ColumnDefault("created_time")
	 protected Date createdTime;

	@CreationTimestamp
	 @ColumnDefault("updated_time")
	 protected Date updatedTime;

}
