package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "cfg_language")
@ToString
public class LanguagesModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="languages")
	    private String languages;
	    
	    @Column(name="keyword")
	    private String keyword;

		public LanguagesModel(int id, String languages) {
			super();
			this.id = id;
			this.languages = languages;
		}

		public LanguagesModel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}

		public String getLanguages() {
			return languages;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setLanguages(String languages) {
			this.languages = languages;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}	
	    
	    

}
