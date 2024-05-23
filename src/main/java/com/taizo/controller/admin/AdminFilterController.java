package com.taizo.controller.admin;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restfb.json.ParseException;
import com.restfb.util.DateUtils;
import com.taizo.DTO.CandidateAnalyticsFilterDTO;
import com.taizo.DTO.DateRange;
import com.taizo.model.CandidateAnalyticsModel;
import com.taizo.service.CandidateService;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminFilterController {
	
	@Autowired
	CandidateService candidateService;
	
	
	@GetMapping("/candidateFunnel")
	public ResponseEntity<?> getCandidateAnalyticsSummary(
	        @RequestParam(required = false) Long adminId,
	        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
	        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

	    // Set the start date to the start of the day
	    Calendar startCalendar = Calendar.getInstance();
	    startCalendar.setTime(startDate);
	    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
	    startCalendar.set(Calendar.MINUTE, 0);
	    startCalendar.set(Calendar.SECOND, 0);
	    startCalendar.set(Calendar.MILLISECOND, 0);
	    startDate = startCalendar.getTime();

	    // Set the end date to the end of the day
	    Calendar endCalendar = Calendar.getInstance();
	    endCalendar.setTime(endDate);
	    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
	    endCalendar.set(Calendar.MINUTE, 59);
	    endCalendar.set(Calendar.SECOND, 59);
	    endCalendar.set(Calendar.MILLISECOND, 999);
	    endDate = endCalendar.getTime();

	    if (adminId != null) {
	        CandidateAnalyticsFilterDTO summary = candidateService.getAnalyticsSummaryByAdminIdAndDateRange(adminId, startDate, endDate);
	        System.out.println(summary);
	        return ResponseEntity.ok(summary);
	    } else {
	        List<CandidateAnalyticsFilterDTO> summaries = candidateService.getAnalyticsSummaryByDateRange(startDate, endDate);
	        System.out.println(summaries);
	        return ResponseEntity.ok(summaries);
	    }
	}




	



}
