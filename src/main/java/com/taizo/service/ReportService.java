package com.taizo.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.beans.FullReportRow;
import com.taizo.repository.ReportRepository;


@Service
public class ReportService {
	
	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	
	@Autowired
	private ReportRepository reportRepository;
	
	@Transactional(readOnly = true)
	public List<FullReportRow> getFullReport() {
		
		return reportRepository.getFullReport();
	}
	
	@Transactional(readOnly = true)
	public ByteArrayInputStream downloadFullReport() throws IOException {
		List<FullReportRow> list = getFullReport();
		
		Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Full Report");
		ByteArrayOutputStream out = new ByteArrayOutputStream();


        String[] headers = new String[] {
        		"Job Id", "Job Category", "Candidate First Name", "Candidate Last Name", "Age" ,
        		"Experience", "Current Location","Perm Location", "Mobile Number", 
        		"Education", "Specialization","Keyskills", "Certification Courses", "Time", "Status"
		};        
        
        Row headerRow = sheet.createRow(0);
        for(int i = 0; i < headers.length; i++) {
        	Cell cell = headerRow.createCell(i);
        	cell.setCellValue(headers[i]);
        }
        
        int i = 0;
        for (FullReportRow fullReportRow : list) {
        	i++;
        	int j = 0;
        	
        	
        	Row row = sheet.createRow(i);
        	writeCell(row, j++, "" + fullReportRow.getJob_id());
        	writeCell(row, j++, "" + fullReportRow.getJobCategory());
        	writeCell(row, j++, "" + fullReportRow.getCandidateFirstName());
        	writeCell(row, j++, "" + fullReportRow.getCandidateLastName());
        	writeCell(row, j++, "" + fullReportRow.getAge());
        	writeCell(row, j++, "" + fullReportRow.getExperience());
        	writeCell(row, j++, "" + fullReportRow.getCurrentLocation());

        	writeCell(row, j++, "" + fullReportRow.getPermLocation());
        	writeCell(row, j++, "" + fullReportRow.getUserMobile());
        	//writeCell(row, j++, "" + fullReportRow.getUserEmail());
        	writeCell(row, j++, "" + fullReportRow.getEducation());
        	writeCell(row, j++, "" + fullReportRow.getSpecialization());
        	writeCell(row, j++, "" + fullReportRow.getKeySkills());
        	
        	writeCell(row, j++, "" + fullReportRow.getCertificationCourses());
        	//writeCell(row, j++, "" + fullReportRow.getStudentFirstName() + " " + fullReportRow.getStudentLastName());
        	writeCell(row, j++, "" + fullReportRow.getTime());
        	writeCell(row, j++, "" + fullReportRow.getStatus());
        	
        }

        workbook.write(out);

        // Closing the workbook
        workbook.close();
		return new ByteArrayInputStream(out.toByteArray());
	}
	
	private void writeCell(Row row, int columnNo, String value) {
		Cell cell = row.createCell(columnNo);
		cell.setCellValue(value);
	}

}
