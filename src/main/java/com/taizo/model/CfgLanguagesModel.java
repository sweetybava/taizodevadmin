package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_languages_labels")
@ToString
public class CfgLanguagesModel {


	@Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id",unique = true, nullable = false)
	    private int id;
	
	   @Column(name="languages_name")
	   private String languagesName;
	   
	   @Column(name="languages")
	   private String languages;
	   
	   
	   @Column(name="keyword")
	   private String keyword;
	   
	   @Column(name="icon")
	   private String icon;
	   
	   @Column(name="label1")
	   private String label1;
	   
	   @Column(name="label2")
	   private String label2;
	   
	   @Column(name="label3")
	   private String label3;
	   
	   @Column(name="label4")
	   private String label4;
	   
	   @Column(name="label5")
	   private String label5;
	   
	   @Column(name="active")
	   private boolean active;

	public CfgLanguagesModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public String getLanguagesName() {
		return languagesName;
	}

	public String getLanguages() {
		return languages;
	}

	public String getLabel1() {
		return label1;
	}

	public String getLabel2() {
		return label2;
	}

	public String getLabel3() {
		return label3;
	}

	public String getLabel4() {
		return label4;
	}

	public String getLabel5() {
		return label5;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLanguagesName(String languagesName) {
		this.languagesName = languagesName;
	}

	public void setLanguages(String languages) {
		this.languages = languages;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

	public void setLabel4(String label4) {
		this.label4 = label4;
	}

	public void setLabel5(String label5) {
		this.label5 = label5;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	   
	   
	   
	   
}
