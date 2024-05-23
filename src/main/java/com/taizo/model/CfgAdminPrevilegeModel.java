package com.taizo.model;
	
	import java.sql.Timestamp;
import java.util.List;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


	@Entity
	@Table(name = "cfg_admin_privileges")
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	public class CfgAdminPrevilegeModel {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;
		
		@Column(name = "privilege_id")
		private String privilegeId;
		
		@Column(name = "privilege_name")
		private String privilegeName;
		
		@Column(name = "active")
		private String active;
		
		@Column  (name = "description")
		private String description;

}
