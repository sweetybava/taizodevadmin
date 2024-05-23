package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "wa_campaign_report")
@Getter
@Setter
@ToString
public class CampaignReportModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "job_id")
	private int jobId;

	@Column(name = "matched_count")
	private int matchedCount;

	@Column(name = "related_count")
	private int relatedCount;

	@Column(name = "fresher_count")
	private int fresherCount;

	@Column(name = "lead_fresher_count")
	private int leadFresherCount;

	@Column(name = "lead_exp_count")
	private int leadExpCount;

}
