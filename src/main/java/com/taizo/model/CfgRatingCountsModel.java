package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cfg_rating_counts")
@Getter
@Setter
@ToString
public class CfgRatingCountsModel {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	    
	    @Column(name="rating_count")
	    private int ratingCount;
	    
	    @Column(name="question")
	    private String question;
	    
	    @Column(name="module")
	    private String module; 
	    
	    @OneToMany(mappedBy = "reasons", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	    private List<CfgRatingReasonsModel> reasons = new ArrayList<CfgRatingReasonsModel>();
	    	    
}
