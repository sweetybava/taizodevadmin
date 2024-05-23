package com.taizo.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.taizo.beans.FullReportRow;


public class FullReportRowMapper implements RowMapper<FullReportRow> {

	@Override
	public FullReportRow mapRow(ResultSet resultSet, int row) throws SQLException {
		
		FullReportRow fullReportRow = new FullReportRow();
		fullReportRow.setJob_id(resultSet.getLong("emp_job_id"));
		fullReportRow.setJobCategory(resultSet.getString("job_category"));
		fullReportRow.setCandidateFirstName(resultSet.getString("first_name"));
		fullReportRow.setCandidateLastName(resultSet.getString("last_name"));
		fullReportRow.setAge(resultSet.getInt("age"));
		fullReportRow.setExperience(resultSet.getInt("exp_in_years"));
		fullReportRow.setCurrentLocation(resultSet.getString("current_city"));
		fullReportRow.setPermLocation(resultSet.getString("per_city"));
		fullReportRow.setUserMobile(resultSet.getString("mobile_number"));
		fullReportRow.setUserEmail(resultSet.getString("email_id"));
		fullReportRow.setEducation(resultSet.getString("qualification"));
		fullReportRow.setSpecialization(resultSet.getString("specialization"));
		fullReportRow.setKeySkills(resultSet.getString("key_skill"));
		fullReportRow.setCertificationCourses(resultSet.getString("certification_courses"));
		fullReportRow.setTime(resultSet.getString("applied_time"));
		fullReportRow.setStatus(resultSet.getString("status"));
		
		return fullReportRow;
	}

}
