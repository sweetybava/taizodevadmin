package com.taizo.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Admin {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="admin_id")
	private Long id;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "module")
	private String module;
	
	@Column(name = "profile_pic")
	private String profilePic;
	 
	@Column(name = "email_id")
	private String emailId;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "mobile_no")
	private String mobileNo;
	
	@Column(name = "mobile_country_code")
	private String mobileCountryCode;
	
	@Column(name = "is_available")
	private boolean isAvailable;
	
	@Column(name = "is_deactivate")
	private boolean isDeactivate;
	
	@Column(name = "intro_video_url")
	private String introVideoUrl;
	
	@Column(name = "intro_gif_url")
	private String introGifUrl;
	
	@Column(name = "email_signature ")
	private String emailSignature ;
	
	 @CreationTimestamp
	 @ColumnDefault("created_on")
	 protected Date createdOn;
	 
	 @CreationTimestamp
	 @Column(name = "created_time", updatable = false)
	 protected Timestamp createdTime; 
	
	@JsonIgnore
	@OneToMany(mappedBy = "admin")
    private List<AdminRolesMappingModel> roleMappings = new ArrayList<>();

	
	

}
