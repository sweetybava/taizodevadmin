package com.taizo.controller.employer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.taizo.beans.FullReportRow;
import com.taizo.model.EmployerModel;
import com.taizo.repository.EmployerRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.ReportService;
import com.taizo.utils.TupleStore;

@RestController
public class ReportController {

	@Autowired
	private ReportService reportService;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	EmployerRepository employerRepository;

	private Gson gson = new Gson();

	@GetMapping(value = "/dailyReport")
	public ResponseEntity<InputStreamResource> downloadFullReport() throws IOException {

		/*
		 * response.setContentType("application/octet-stream");
		 * response.setHeader("Content-Disposition",
		 * "attachment; filename=customers.xlsx");
		 */

		try {
		//	List<EmployerModel> employers = employerRepository.findByKycStatus();
			//for (EmployerModel employer : employers) {
				boolean sendMail = false;
				boolean isValid = true;
				
				/*
				 * try { EmployerModel activeAccount =
				 * employerRepository.checkAccountStatus(employer.getId()); // check already
				 * activated if (activeAccount == null) { sendMail = true; }
				 * 
				 * } catch (Exception e) { // TODO: handle exception }
				 */

				ByteArrayInputStream workbook = reportService.downloadFullReport(); // creates the workbook

				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Disposition", "attachment; filename=DailyReport.xlsx");

				HashMap<String, String> emailDataHM = new HashMap<>();
				emailDataHM.put("name", "Techie");
				emailDataHM.put("companyName", "Tech World");

				TupleStore tupleStore = new TupleStore();
				tupleStore.setKey("saranyapraba78@gmail.com");
				tupleStore.setValue(gson.toJson(emailDataHM));
				//amazonSESMailUtil.sendEmailWithMultipleAttachments();

				return ResponseEntity.ok().headers(headers).body(new InputStreamResource(workbook));
			//}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

}
