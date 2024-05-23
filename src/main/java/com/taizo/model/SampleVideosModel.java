package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "Taizo_Videos")
@ToString
public class SampleVideosModel {
	@Id
	@Column(name="vid")
	private String vid;

	@Column(name = "v_link")
	private String v_link;

	@Column(name = "v_title")
	private String v_title;
	
	@Column(name = "v_desc")
	private String v_desc;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "created_at")
	private String createdAt;

	public String getId() {
		return vid;
	}

	public void setId(String id) {
		this.vid = id;
	}

	public String getV_link() {
		return v_link;
	}

	public void setV_link(String v_link) {
		this.v_link = v_link;
	}

	public String getV_title() {
		return v_title;
	}

	public void setV_title(String v_title) {
		this.v_title = v_title;
	}

	public String getV_desc() {
		return v_desc;
	}

	public void setV_desc(String v_desc) {
		this.v_desc = v_desc;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}