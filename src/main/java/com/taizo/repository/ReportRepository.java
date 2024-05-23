package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.taizo.beans.FullReportRow;
import com.taizo.repository.mapper.FullReportRowMapper;


@Repository
public class ReportRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(ReportRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<FullReportRow> getFullReport() {
		
		String sql = "SELECT  j.emp_job_id,j.job_category,c.first_name,c.last_name,c.age,c.exp_in_years,c.current_city,c.per_city, " + 
				"c.mobile_number,c.email_id,c.qualification,c.specialization,c.key_skill,c.certification_courses,ja.applied_time,ja.status " + 
				"FROM jobs j " + 
				"JOIN employer e " + 
				"ON  j.employer_id =e.id and e.id=12 and j.job_status=\"O\" " + 
				"left join job_application ja ON j.job_id = ja.job_id and ja.status=\"I\" and ja.applied_time >= CURDATE() " + 
				"join candidate c on ja.candidate_id = c.candidate_id " + 
				"union " + 
				"SELECT  j.emp_job_id,j.job_category,c.first_name,c.last_name,c.age,c.exp_in_years,c.current_city,c.per_city, " + 
				"c.mobile_number,c.email_id,c.qualification,c.specialization,c.key_skill,c.certification_courses, " + 
				"ccr.call_time,\"Called\" as status " + 
				"FROM jobs j " + 
				"JOIN employer e " + 
				"ON  j.employer_id =e.id and e.id=12 and j.job_status=\"O\" " + 
				"left join candidate_call_registry ccr ON j.job_id = ccr.jid and ccr.empid=e.id and ccr.call_time >= CURDATE() " + 
				"join candidate c on ccr.cid = c.candidate_id;";
		
		logger.debug("Sql : {}", sql);
		List<FullReportRow> list = jdbcTemplate.query(sql, new FullReportRowMapper());
		logger.debug("Result size: {}", list.size());
		
		return list;
	}

}
