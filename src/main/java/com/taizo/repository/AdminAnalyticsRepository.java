package com.taizo.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.AdminAnalyticsModel;

public interface AdminAnalyticsRepository extends JpaRepository<AdminAnalyticsModel, Long> {
	
	List<AdminAnalyticsModel> findByAdminId(Long adminId);
	
	 List<AdminAnalyticsModel> findByAdminIdAndModuleAndCreatedOnBetween(
		        Long adminId,
		        String module,
		        Timestamp startDate,
		        Timestamp endDate, Sort sort
		    );


	AdminAnalyticsModel findByAdminIdAndModuleAndCreatedOn(Long adminId, String module, Timestamp timestamp);

}
