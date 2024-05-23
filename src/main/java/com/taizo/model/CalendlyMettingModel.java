package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "calendly_meeting_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class CalendlyMettingModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
	
	 @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	 
	 @CreationTimestamp
	    @ColumnDefault("updated_time")
	    protected Date updatedTime;
	 
	 @Column(name = "user_email")
	    private String userEmail;
	 
	 @Column(name = "join_url")
	    private String joinUrl;
	 
	 @Column(name = "meeting_start_time")
	    private String meetingStartTime;
	 
	 @Column(name = "user_name")
	    private String userName;
	 
	 @Column(name = "owner_name")
	    private String ownerName;
	 
	 @Column(name = "owner_email")
	    private String ownerEmail;
	 
	 @Column(name = "event_name")
	    private String eventName;

}
