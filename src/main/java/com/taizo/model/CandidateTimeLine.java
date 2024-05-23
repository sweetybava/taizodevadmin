	package com.taizo.model;
	
    import java.sql.Timestamp;
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
	
	@Entity
	@Table(name = "candidate_timeline")
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class CandidateTimeLine {
		
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
		
		@Column(name = "can_id")
		private Integer canId;

		@Column(name = "facebook_id")
		private Long facebookId;

		@Column(name = "senior_can_id")
		private Long seniorCanId;

		@Column(name = "senior_can_lead_id")
		private Long seniorCanLeadId;

		@Column(name = "can_lead_id")
		private Integer canLeadId;

		@Column(name = "event_name")
		private String eventName;

		@Column(name = "event_description")
		private String eventDescription;

		@Column(name = "notes")
		private String notes;


		@CreationTimestamp
	    @ColumnDefault("CURRENT_TIMESTAMP")
	    protected Timestamp createdTime;

	
	}
