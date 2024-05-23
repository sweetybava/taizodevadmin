package com.taizo.service;

import java.io.ByteArrayInputStream; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.taizo.beans.FullReportRow;
import com.taizo.model.CampaignReportModel;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgFullTimeGroup;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerModel;
import com.taizo.model.Example;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.IndustryModel;
import com.taizo.model.JobsModel;
import com.taizo.model.Parameters;
import com.taizo.model.To;
import com.taizo.repository.CampaignReportRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.CfgEmpPrescreeningQuestionsRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerJobPrescreeningQuestionsRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.FullTimeGroupingRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.repository.PlansRepository;
import com.taizo.repository.ReportRepository;
import com.taizo.utils.TupleStore;

@Service
public class TestNotificationService {

	@Autowired
	JobService jobService;

	@Autowired
	CampaignReportRepository campaignReportRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	EmployerJobPrescreeningQuestionsRepository employerJobPrescreeningQuestionsRepository;

	@Autowired
	CfgEmpPrescreeningQuestionsRepository cfgEmpPrescreeningQuestionsRepository;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;
	
	@Autowired
	WAAlertService waAlertService;

	
	public void sendAdminNotification(JobsModel job, EmployerModel empData) {
		// TODO Auto-generated method stub

		List<CandidateModel> results = null;
		List<CandidateModel> fresherResult = null;

		int industryId = industryRepository.findByIndustry(job.getIndustry());
		List<CfgFullTimeGroup> group = fullTimeGroupingRepository.findByCategoryAndIndustry(job.getJobCategory(),
				industryId);

		List<CfgFullTimeGroup> groups = fullTimeGroupingRepository.findByGroupId(group.get(0).getGroupId(),group.get(0).getId());

		CampaignReportModel crm = new CampaignReportModel();
		crm.setJobId(job.getId());

		if (job.getJobExp() == 0) {

			/*
			 * fresherResult =
			 * candidateRepository.getAdminFresherCandidates(job.getJobLocation());
			 * crm.setFresherCount(fresherResult.size());
			 * campaignReportRepository.save(crm); sendFreshersJobAlert(fresherResult, job,
			 * empData);
			 */

			if (job.getJobMaxExp() >0) {
				results = candidateRepository.getAdminOneYearExperiencedCandidates(job.getIndustry(),
						job.getJobCategory(), job.getJobLocation(),job.getJobMaxExp());
				crm.setMatchedCount(results.size());
				campaignReportRepository.save(crm);

				sendMatchedJobsAlert(results, job, empData);
				sendRelatedJobsAlert(groups, results, job, empData, industryId, crm);
			}

		} else {
			results = candidateRepository.getAdminExperiencedCandidates(job.getIndustry(), job.getJobCategory(),
					job.getJobExp(), job.getJobMaxExp(), job.getJobLocation());

			crm.setMatchedCount(results.size());
			campaignReportRepository.save(crm);

			sendMatchedJobsAlert(results, job, empData);
			sendRelatedJobsAlert(groups, results, job, empData, industryId, crm);
		}

	}

	private void sendFreshersJobAlert(List<CandidateModel> fresherResult, JobsModel job, EmployerModel empData) {

		if (!fresherResult.isEmpty()) {
			String jobLink = getJobLink(job.getId());
			for (CandidateModel cm : fresherResult) {

				String salary = String.valueOf(job.getMaxSalary());
				String sal = getSalaryByLanKey(cm.getLanguageKey(),salary);

				
				String loc = job.getJobLocation();
				if (job.getArea() != null) {
					loc = job.getArea() + ", " + job.getJobLocation();
				}
				
				String newstring = getPostedDate(job.getJobPostedTime());
				
				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(cm.getWhatsappNumber()));
				d.put("name", cm.getFirstName());
				d.put("jobCategory", job.getJobCategory());
				d.put("city", loc);
				d.put("salary", sal);
				d.put("companyName", job.getCompanyName());
				d.put("exp", String.valueOf(job.getJobExp()));
				d.put("industry", job.getIndustry());
				d.put("postedDate", newstring);
				d.put("jobLink", jobLink);
				d.put("languageKey", cm.getLanguageKey());
				
				waAlertService.sendJobAlert(d);
			}
		}
	}

	private void sendRelatedJobsAlert(List<CfgFullTimeGroup> groups, List<CandidateModel> matchedJobs, JobsModel job,
			EmployerModel empData, int industryId, CampaignReportModel crm) { // TODO Auto-generated method stub
		List<CandidateModel> result = null;
		ArrayList<CandidateModel> results = new ArrayList<CandidateModel>();

		if (!groups.isEmpty()) {

			for (CfgFullTimeGroup c : groups) {

				Optional<IndustryModel> indModel = industryRepository.findById(c.getIndustryId());

				result = candidateRepository.getAdminExperiencedRelatedCandidates(indModel.get().getIndustry(),
						c.getGroupName(), job.getJobLocation(),job.getJobExp());

				results.addAll(result);
			}
			crm.setRelatedCount(results.size());
			campaignReportRepository.save(crm);

			if (!results.isEmpty()) {

				String jobLink = getJobLink(job.getId());

				for (CandidateModel cm : results) {


					String salary = String.valueOf(job.getMaxSalary());
					String sal = getSalaryByLanKey(cm.getLanguageKey(),salary);
					
					
					String loc = job.getJobLocation();
					if (job.getArea() != null) {
						loc = job.getArea() + ", " + job.getJobLocation();
					}
					
					String newstring = getPostedDate(job.getJobPostedTime());
					
					HashMap<String, String> d = new HashMap<>();
					d.put("mn", "91" + String.valueOf(cm.getWhatsappNumber()));
					d.put("name", cm.getFirstName());
					d.put("jobCategory", job.getJobCategory());
					d.put("city", loc);
					d.put("salary", sal);
					d.put("companyName", job.getCompanyName());
					d.put("exp", String.valueOf(job.getJobExp()));
					d.put("industry", job.getIndustry());
					d.put("postedDate", newstring);
					d.put("jobLink", jobLink);
					d.put("languageKey", cm.getLanguageKey());
					
					waAlertService.sendJobAlert(d);
				}
			}
		}

	}

	public List<Integer> getValuesForGivenKey(List<CandidateModel> matchedresults, String key) {
		JSONArray jsonArray = new JSONArray(matchedresults);
		return IntStream.range(0, jsonArray.length()).mapToObj(index -> ((JSONObject) jsonArray.get(index)).optInt(key))
				.collect(Collectors.toList());
	}

	private void sendMatchedJobsAlert(List<CandidateModel> results, JobsModel job, EmployerModel empData) {

		if (!results.isEmpty()) {
			String jobLink = getJobLink(job.getId());

			for (CandidateModel cm : results) {
				String salary = String.valueOf(job.getMaxSalary());
				String sal = getSalaryByLanKey(cm.getLanguageKey(),salary);
				
				String loc = job.getJobLocation();
				if (job.getArea() != null) {
					loc = job.getArea() + ", " + job.getJobLocation();
				}
				
				String newstring = getPostedDate(job.getJobPostedTime());
							
				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(cm.getWhatsappNumber()));
				d.put("name", cm.getFirstName());
				d.put("jobCategory", job.getJobCategory());
				d.put("city", loc);
				d.put("salary", sal);
				d.put("companyName", job.getCompanyName());
				d.put("exp", String.valueOf(job.getJobExp()));
				d.put("industry", job.getIndustry());
				d.put("postedDate", newstring);
				d.put("jobLink", jobLink);
				d.put("languageKey", cm.getLanguageKey());
				
				waAlertService.sendJobAlert(d);
				
			}
		}

	}
	
	private String getPostedDate(Date date) {
		// TODO Auto-generated method stub
		String nDate = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
			nDate = dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nDate;
	}


	private String getSalaryByLanKey(String languageKey, String sal) {
		// TODO Auto-generated method stub
		if (languageKey == null) {
			sal = sal + " Maximum";
		} else if (languageKey.equalsIgnoreCase("ta")) {
			sal = sal + " வரை";
		} else if (languageKey.equalsIgnoreCase("hi")) {
			sal = sal + " तक";
		} else {
			sal = sal + " Maximum";
		}
		return sal;
	}

	public String getJobLink(int jobId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
					+ jobId + "&apn=" + firebaseJSPackage);

			DeeplinkSuffix c = new DeeplinkSuffix();
			c.setOption("UNGUESSABLE");

			String json = new com.google.gson.Gson().toJson(dl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

			RestTemplate restTemp = new RestTemplate();

			FirebaseShortLink response = null;
			try {
				response = restTemp.postForObject(url, req, FirebaseShortLink.class);
			} catch (Exception e) {

			}
		return response.getShortLink();
		
	}

}