package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cfg_emp_documents" )
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CfgEmployerDocumentsModel {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    @Column(name = "doc_title")
	    private String docTitle;

	    @Column(name = "doc_key")
	    private String docKey;

	    @Column(name = "active")
	    private Boolean active = true;
}
