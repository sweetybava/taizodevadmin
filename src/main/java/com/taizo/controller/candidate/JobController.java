package com.taizo.controller.candidate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.amazonaws.services.iot.model.Job;
import com.google.gson.Gson;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.*;
import com.taizo.utils.TupleStore;

import freemarker.template.TemplateException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public class JobController {


	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	JobRepository jobRepository;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	JobApplicationService jobApplicationService;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CanInterviewRepository canInterviewRepository;

	@Autowired
	JobService jobService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	UserRepository userRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	PartTimeGroupingRepository partTimeGroupingRepository;

	@Autowired
	JobCanResponsesRepository jobCanResponsesRepository;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	WAAlertService waAlertService;

	private Gson gson = new Gson();

	private static final Logger logger = LoggerFactory.getLogger(JobController.class);

	@GetMapping(path = "/matchedJobDetails")
	public ResponseEntity<?> getNewCanMatchedJobs(@RequestParam(value = "city", required = false) final String city,
			@RequestParam("candidate_id") final int canId, @RequestParam("start") int start) {

		Page<JobsModel> results = null;
		List<String> cityList = null;
		int length = 10;

		Optional<CandidateModel> canDetails = candidateRepository.findById(canId);

		if (city != null && !city.isEmpty()) {
			String[] elements = city.split(",");
			cityList = Arrays.asList(elements);
		}

		int page = start / 1; // Calculate page number

		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "jobPostedTime"));

		String canType = canDetails.get().getCandidateType();

		List<JobApplicationModel> canJobs = null;
		List<Integer> jobIDs = new ArrayList();
		List<JobsModel> empJobs = new ArrayList();
		List<Integer> empJobIDs = new ArrayList();

		canJobs = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
				.setParameter("ids", canId).getResultList();

		if (canJobs.size() > 0) {
			canJobs.forEach(job -> {
				jobIDs.add(job.getJobId());

				Optional<JobsModel> j1 = jobRepository.findById(job.getJobId());
				List<JobsModel> jobList = jobRepository.findByJobDetails(j1.get().getEmployerId(),
						j1.get().getJobCategory());
				jobList.forEach(job1 -> {
					empJobIDs.add(job1.getId());
				});
			});
			empJobIDs.forEach(jobid -> {

				if (!jobIDs.contains(jobid)) {
					jobIDs.add(jobid);
				}

			});

		} else {
			jobIDs.add(0);
		}

		if (canType == null) {
			if (city != null && !city.isEmpty()) {
				String quali = canDetails.get().getQualification();
				results = jobRepository.findCanAllFilteredJobs(jobIDs, cityList, pageable);

			} else {
				results = jobRepository.findCanAllMatchedJobs(jobIDs, pageable);
			}

		} else {

			if (canType.equalsIgnoreCase("Experienced")) {
				String jobRole = canDetails.get().getJobCategory();
				String industry = canDetails.get().getIndustry();

				int exp = canDetails.get().getExperience();
				String cities = canDetails.get().getCity();

				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}

				results = jobRepository.findCanAllExperiencedJobs(jobIDs, exp, jobRole, industry, cityList, pageable);

				List<JobsModel> finalList = new ArrayList<JobsModel>();
				if (results != null && results.hasContent()) {
					finalList.addAll(results.getContent());
				}

				List<JobsModel> relList = null;
				List<String> roleList = new ArrayList<>();

				int industryId = industryRepository.findByIndustry(industry);
				List<CfgFullTimeGroup> group = fullTimeGroupingRepository.findByCategoryAndIndustry(jobRole,
						industryId);

				List<CfgFullTimeGroup> groups = fullTimeGroupingRepository.findByGroupId(group.get(0).getGroupId(),
						group.get(0).getId());

				if (!groups.isEmpty()) {
					groups.forEach(name -> {
						roleList.add(name.getGroupName());
					});

					List<Integer> g = getValuesForGivenKey(finalList, "id");
					jobIDs.addAll(g);

					relList = jobRepository.findNewExperiencedRelatedJobs(roleList, jobIDs, cityList, exp, pageable);

					finalList.addAll(relList);

					results = new PageImpl<>(finalList);

				}

			} else {
				String cities = canDetails.get().getCity();
				// String quali = canDetails.get().getQualification();

				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}
				results = jobRepository.findCanAllFilteredJobs(jobIDs, cityList, pageable);

			}
		}

		if (results != null && !results.isEmpty()) {

			for (JobsModel j : results) {
				EmployerModel emp = employerRepository.findById(j.getEmployerId()).get();
				String image = emp.getCompanyLogo();
				if (image != null && !image.isEmpty()) {
					j.setJobPic(image);
				} else {
					j.setJobPic(
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
				}

			}

			HashMap<String, Object> hm = new HashMap<>();

			hm.put("statuscode", 200);
			hm.put("message", "success");
			hm.put("results", results.getContent());
			hm.put("nextPage", start + 1);
			hm.put("start", start);
			hm.put("recordsTotal", results.getTotalElements());
			hm.put("recordsFiltered", results.getSize());
			hm.put("Pageable", results.getPageable());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		} else {
			/*
			 * List<CanInterviewsModel> existIn =
			 * canInterviewRepository.findByCanIdandJobId(canDetails.get().getId(),1140);
			 * 
			 * if(existIn.isEmpty()) { int age =
			 * Integer.parseInt(canDetails.get().getAge()); List<JobsModel> staff =
			 * jobRepository.findByAppStaffingMatchedJobs(age,
			 * canDetails.get().getState(),canDetails.get().getCity(),canDetails.get().
			 * getPassed_out_year(),
			 * canDetails.get().getQualification(),canDetails.get().getSpecification());
			 * 
			 * if(!staff.isEmpty()) { results = new PageImpl<>(staff); if (results != null
			 * && !results.isEmpty()) {
			 * 
			 * for (JobsModel j : results) { EmployerModel emp =
			 * employerRepository.findById(j.getEmployerId()).get(); String image =
			 * emp.getCompanyLogo(); if (image != null && !image.isEmpty()) {
			 * j.setJobPic(image); } else { j.setJobPic(
			 * "https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png"
			 * ); }
			 * 
			 * }
			 * 
			 * HashMap<String, Object> hm = new HashMap<>();
			 * 
			 * hm.put("statuscode", 200); hm.put("message", "success"); hm.put("results",
			 * results.getContent()); hm.put("nextPage", start + 1); hm.put("start", start);
			 * hm.put("recordsTotal", results.getTotalElements()); hm.put("recordsFiltered",
			 * results.getSize()); hm.put("Pageable", results.getPageable()); return new
			 * ResponseEntity<>(hm, HttpStatus.OK); } } }
			 */

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Matched Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping(path = "/matchedJobs")
	public ResponseEntity<?> getMatchedJobs(@RequestParam("experience") final int experience,
			@RequestParam("job_type") final String jobType, @RequestParam("industry") final String industry,
			@RequestParam("candidate_location") final String d, @RequestParam("city") final String domLoc,
			@RequestParam("job_category") final List<String> jobCategory,
			@RequestParam("candidate_id") final int userId) {

		String status = "O";

		List<JobsModel> results = null;

		CandidateModel details = candidateRepository.finduser(userId);
		int canId = details.getId();

		/*
		 * details.setCity(domLoc); candidateRepository.save(details);
		 */

		int industryId = industryRepository.findByIndustry(industry);

		if (d.equalsIgnoreCase("Domestic")) {

			String[] elements = domLoc.split(",");
			List<String> fixedLenghtList = Arrays.asList(elements);

			results = em.createQuery("SELECT e FROM JobsModel e "
					+ "left join EmployerModel emp on e.employerId = emp.id "
					+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
					+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
					+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
					+ "AND e.jobLocation IN :city AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and "
					+ "cji.id = :indId").setParameter("jobCategory", jobCategory)
					.setParameter("minExperience", experience).setParameter("city", fixedLenghtList)
					.setParameter("status", status).setParameter("indId", industryId).getResultList();

		}

		if (results != null && !results.isEmpty()) {

			List<JobApplicationModel> Detail = null;

			Detail = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
					.setParameter("ids", canId).getResultList();

			if (Detail != null && !Detail.isEmpty()) {

				int id = 0;
				List<Integer> list = new ArrayList();

				List<JobsModel> persons = null;

				for (JobApplicationModel s : Detail) {
					id = s.getJobId();
					list.add(id);
				}

				if (d.equalsIgnoreCase("Domestic")) {
					String[] elements = domLoc.split(",");
					List<String> fixedLenghtList = Arrays.asList(elements);

					persons = em.createQuery("SELECT e FROM JobsModel e "
							+ "left join EmployerModel emp on e.employerId = emp.id "
							+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
							+ "left join IndustryModel cji "
							+ "on cji.id = cfg.industryId and cji.industry = e.industry "
							+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
							+ "AND e.jobLocation IN :city AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and "
							+ "cji.id = :indId").setParameter("jobCategory", jobCategory)
							.setParameter("minExperience", experience).setParameter("city", fixedLenghtList)
							.setParameter("status", status).setParameter("indId", industryId).getResultList();

				} else if (d.equalsIgnoreCase("International")) {
					String count1 = details.getOverseasLocation();
					String[] countryArray1 = count1.split(",");
					List<String> country1 = Arrays.asList(countryArray1);

					persons = em.createQuery(
							"SELECT e FROM JobsModel e where e.jobCategory = :jobCategory AND e.jobStatus = :status"
									+ " AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE AND e.id NOT IN :id")
							.setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
							.setParameter("status", status).setParameter("id", list).getResultList();

				} else {

					String[] elements = domLoc.split(",");
					List<String> fixedLenghtList = Arrays.asList(elements);

					persons = em.createQuery("SELECT e FROM JobsModel e "
							+ "left join EmployerModel emp on e.employerId = emp.id "
							+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
							+ "left join IndustryModel cji "
							+ "on cji.id = cfg.industryId and cji.industry = e.industry "
							+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
							+ "AND e.jobLocation IN :city AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and "
							+ "cji.id = :indId").setParameter("jobCategory", jobCategory)
							.setParameter("minExperience", experience).setParameter("city", fixedLenghtList)
							.setParameter("status", status).setParameter("indId", industryId).getResultList();
				}

				if (persons != null && !persons.isEmpty()) {

					for (JobsModel j : persons) {
						int empId = j.getEmployerId();
						Optional<EmployerModel> optional = employerRepository.findById(empId);
						EmployerModel existing = optional.get();

						String image = existing.getCompanyLogo();

						if (image != null && !image.isEmpty()) {

							j.setJobPic(image);
						} else {
							j.setJobPic(
									"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
						}
					}

					if (!persons.isEmpty()) {

						Collections.reverse(persons);

						Collections.sort(persons, new Comparator<JobsModel>() {
							public int compare(JobsModel o1, JobsModel o2) {
								if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
									return -1;
								} else {
									return 1;
								}
							}
						});

						ArrayList<JobsModel> temp_rem = new ArrayList<>();
						for (int jobId : list) {
							for (JobsModel j : persons) {
								if (j.getId() == jobId) {
									temp_rem.add(j);
								}
							}
						}

						persons.removeAll(temp_rem);

						HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 200);
						map.put("message", "success");
						map.put("results", persons);
						return new ResponseEntity<>(map, HttpStatus.OK);
					} else {
						HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 400);
						map.put("message", "No Matched Jobs");
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

					}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "No Matched Jobs");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

				}
			} else {
				for (JobsModel j : results) {
					int empId = j.getEmployerId();
					Optional<EmployerModel> optional = employerRepository.findById(empId);
					EmployerModel existing = optional.get();

					String image = existing.getCompanyLogo();

					if (image != null && !image.isEmpty()) {

						j.setJobPic(image);
					} else {
						j.setJobPic(
								"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
					}

				}

				if (!results.isEmpty()) {

					List<Integer> list = new ArrayList();

					for (JobApplicationModel s : Detail) {
						list.add(s.getJobId());
					}

					Collections.reverse(results);

					Collections.sort(results, new Comparator<JobsModel>() {
						public int compare(JobsModel o1, JobsModel o2) {
							if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
								return -1;
							} else {
								return 1;
							}
						}
					});

					ArrayList<JobsModel> temp_rem = new ArrayList<>();
					for (int jobId : list) {
						for (JobsModel j : results) {
							if (j.getId() == jobId) {
								temp_rem.add(j);
							}
						}
					}

					results.removeAll(temp_rem);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "success");
					map.put("results", results);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "No Matched Jobs");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Matched Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/userMatchedJobs")
	public ResponseEntity<?> getUserMatchedJobs(@RequestParam("experience") final int experience,
			@RequestParam("industry") final String industry,
			@RequestParam("job_category") final List<String> jobCategory, @RequestParam("city") final String city) {
		String status = "O";

		List<JobsModel> results = null;

		int industryId = industryRepository.findByIndustry(industry);

		String[] elements = city.split(",");
		List<String> fixedLenghtList = Arrays.asList(elements);

		results = em.createQuery("SELECT e FROM JobsModel e " + "left join EmployerModel emp on e.employerId = emp.id "
				+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
				+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
				+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
				+ "AND e.jobLocation IN :city AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and "
				+ "cji.id = :indId").setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
				.setParameter("city", fixedLenghtList).setParameter("status", status).setParameter("indId", industryId)
				.getResultList();

		if (results != null && !results.isEmpty()) {

			for (JobsModel j : results) {
				EmployerModel emp = employerRepository.findById(j.getEmployerId()).get();
				String image = emp.getCompanyLogo();
				if (image != null && !image.isEmpty()) {
					j.setJobPic(image);
				} else {
					j.setJobPic(
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
				}

			}

			Collections.reverse(results);

			Collections.sort(results, new Comparator<JobsModel>() {
				public int compare(JobsModel o1, JobsModel o2) {
					if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", results);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Matched Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/newMatchedJobs")
	public ResponseEntity<?> getNewMatchedJobs(@RequestParam(value = "city", required = false) final String city,
			@RequestParam("candidate_id") final int canId, @RequestParam("start") int start) {

		Page<JobsModel> results = null;
		List<String> cityList = null;
		start = 0;
		int length = 1000;

		Optional<CandidateModel> canDetails = candidateRepository.findById(canId);

		if (city != null && !city.isEmpty()) {
			String[] elements = city.split(",");
			cityList = Arrays.asList(elements);
		}

		int page = start / 1; // Calculate page number

		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "id"));

		String canType = canDetails.get().getCandidateType();

		List<JobApplicationModel> canJobs = null;
		List<Integer> canlist = new ArrayList();

		canJobs = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
				.setParameter("ids", canId).getResultList();

		if (canJobs.size() > 0) {
			int id = 0;
			for (JobApplicationModel s : canJobs) {
				id = s.getJobId();
				canlist.add(id);
			}
		} else {
			canlist.add(0);
		}

		if (canType == null) {
			if (city != null && !city.isEmpty()) {
				String quali = canDetails.get().getQualification();
				results = jobRepository.findCanAllFilteredJobs(canlist, cityList, pageable);
			} else {
				results = jobRepository.findCanAllMatchedJobs(canlist, pageable);
			}

		} else {
			if (canType.equalsIgnoreCase("Experienced")) {
				String jobRole = canDetails.get().getJobCategory();
				int exp = canDetails.get().getExperience();
				String cities = canDetails.get().getCity();

				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}

				// results = jobRepository.findCanAllExperiencedJobs(canlist, exp, jobRole,
				// cityList, pageable);

			} else {
				String cities = canDetails.get().getCity();
				String quali = canDetails.get().getQualification();

				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}
				results = jobRepository.findCanAllFilteredJobs(canlist, cityList, pageable);
			}
		}

		if (results != null && !results.isEmpty()) {

			for (JobsModel j : results) {
				EmployerModel emp = employerRepository.findById(j.getEmployerId()).get();
				String image = emp.getCompanyLogo();
				if (image != null && !image.isEmpty()) {
					j.setJobPic(image);
				} else {
					j.setJobPic(
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
				}

			}

			HashMap<String, Object> hm = new HashMap<>();

			hm.put("statuscode", 200);
			hm.put("message", "success");
			hm.put("results", results.getContent());
			hm.put("nextPage", start + 1);
			hm.put("start", start);
			hm.put("recordsTotal", results.getTotalElements());
			hm.put("recordsFiltered", results.getSize());
			hm.put("Pageable", results.getPageable());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Matched Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	@GetMapping(path = "/relatedJobs")
	public ResponseEntity<?> getRelatedJobs(@RequestParam("job_type") final String jobType,
			@RequestParam("candidate_id") final int userId, @RequestParam("candidate_location") final String d,
			@RequestParam("industry") final String industry,
			@RequestParam("job_category") final List<String> jobCategory, @RequestParam("city") final String domLoc,
			@RequestParam("experience") final int experience, @RequestParam("job_id") final List<Integer> jobID) {

		List<JobsModel> results = null;
		List<JobsModel> results1 = null;

		String status = "O";

		CandidateModel details = candidateRepository.finduser(userId);
		int canId = details.getId();
		int industryId = industryRepository.findByIndustry(industry);
		String count = details.getPrefCountry();
		String[] countryArray = count.split(",");
		List<String> country = Arrays.asList(countryArray);

		String[] elements = domLoc.split(",");
		List<String> fixedLenghtList = Arrays.asList(elements);

		List<JobApplicationModel> Detail = null;

		Detail = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
				.setParameter("ids", details.getId()).getResultList();

		int id = 0;
		List<Integer> list = new ArrayList();

		for (JobApplicationModel s : Detail) {
			id = s.getJobId();
			list.add(id);
		}

		results = em.createQuery("SELECT e FROM JobsModel e " + "left join EmployerModel emp on e.employerId = emp.id "
				+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
				+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
				+ "where emp.kycStatus IN ('V','U') and e.jobCategory NOT IN :jobCategory AND e.jobStatus = :status "
				+ "AND ((e.jobExp != 0 and :minExperience>0 )or(e.jobExp = 0 and e.jobExp =:minExperience)) AND e.expiryDate >= CURRENT_DATE and "
				+ "cji.id = :indId").setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
				.setParameter("status", status).setParameter("indId", industryId).getResultList();

		results1 = em.createQuery("SELECT e FROM JobsModel e " + "left join EmployerModel emp on e.employerId = emp.id "
				+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
				+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
				+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
				+ "AND e.jobLocation NOT IN :city AND ((e.jobExp != 0 and :minExperience>0 )or(e.jobExp = 0 and e.jobExp =:minExperience)) AND e.expiryDate >= CURRENT_DATE and "
				+ "cji.id = :indId").setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
				.setParameter("city", fixedLenghtList).setParameter("status", status).setParameter("indId", industryId)
				.getResultList();

		results.addAll(results1);

		if (results != null && !results.isEmpty()) {

			if (!Detail.isEmpty()) {

				results = em.createQuery("SELECT e FROM JobsModel e "
						+ "left join EmployerModel emp on e.employerId = emp.id "
						+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
						+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
						+ "where emp.kycStatus IN ('V','U') and e.jobCategory NOT IN :jobCategory AND e.jobStatus = :status "
						+ "AND ((e.jobExp != 0 and :minExperience>0 )or(e.jobExp = 0 and e.jobExp =:minExperience)) AND e.expiryDate >= CURRENT_DATE and "
						+ "cji.id = :indId").setParameter("jobCategory", jobCategory)
						.setParameter("minExperience", experience).setParameter("status", status)
						.setParameter("indId", industryId).getResultList();

				results1 = em.createQuery("SELECT e FROM JobsModel e "
						+ "left join EmployerModel emp on e.employerId = emp.id "
						+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
						+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
						+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
						+ "AND e.jobLocation NOT IN :city AND ((e.jobExp != 0 and :minExperience>0 )or(e.jobExp = 0 and e.jobExp =:minExperience)) AND e.expiryDate >= CURRENT_DATE and "
						+ "cji.id = :indId").setParameter("jobCategory", jobCategory)
						.setParameter("minExperience", experience).setParameter("city", fixedLenghtList)
						.setParameter("status", status).setParameter("indId", industryId).getResultList();

				results.addAll(results1);

				if (results != null && !results.isEmpty()) {
					for (JobsModel j : results) {
						int empId = j.getEmployerId();

						Optional<EmployerModel> optional = employerRepository.findById(empId);
						EmployerModel existing = optional.get();

						String image = existing.getCompanyLogo();

						if (image != null && !image.isEmpty()) {

							j.setJobPic(image);
						} else {
							j.setJobPic(
									"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
						}
					}
					Collections.reverse(results);

					Collections.sort(results, new Comparator<JobsModel>() {
						public int compare(JobsModel o1, JobsModel o2) {
							if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
								return -1;
							} else {
								return 1;
							}
						}
					});

					ArrayList<JobsModel> temp_rem = new ArrayList<>();
					for (int jobId2 : list) {
						for (JobsModel j : results) {
							if (j.getId() == jobId2) {
								temp_rem.add(j);
							}
						}
					}

					results.removeAll(temp_rem);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", results);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "No Related Jobs");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			} else {
				Collections.reverse(results);

				Collections.sort(results, new Comparator<JobsModel>() {
					public int compare(JobsModel o1, JobsModel o2) {
						if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
							return -1;
						} else {
							return 1;
						}
					}
				});
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", results);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("statuscode", 400);
		map.put("message", "No Related Jobs");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

	}

	@GetMapping(path = "/newRelatedJobs")
	public ResponseEntity<?> getNewRelatedJobs(@RequestParam("start") int start,
			@RequestParam("candidate_id") final int canId) {
		Page<JobsModel> results = null;
		List<JobsModel> matchedresults = null;
		List<Integer> canlist = new ArrayList();

		int length = 1000;
		start = 0;

		int page = start / 1; // Calculate page number

		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "id"));

		Optional<CandidateModel> canDetails = candidateRepository.findById(canId);
		String canType = canDetails.get().getCandidateType();
		List<JobApplicationModel> canJobs = null;

		canJobs = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
				.setParameter("ids", canId).getResultList();

		if (canJobs.size() > 0) {

			int id = 0;

			List<JobsModel> persons = null;

			for (JobApplicationModel s : canJobs) {
				id = s.getJobId();
				canlist.add(id);
			}

		} else {
			canlist.add(0);
		}

		if (canType == null) {

		} else {
			if (canType.equalsIgnoreCase("Experienced")) {
				String jobRole = canDetails.get().getJobCategory();

				int exp = canDetails.get().getExperience();
				String cities = canDetails.get().getCity();

				List<String> cityList = null;
				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}

				matchedresults = jobRepository.findCanMatchedExperiencedJobs(canlist, exp, jobRole, cityList);

				List<Integer> g = getValuesForGivenKey(matchedresults, "id");
				canlist.addAll(g);
				results = jobRepository.findExperiencedRelatedJobs(jobRole, canlist, exp, pageable);
			}
		}

		if (results != null && !results.isEmpty()) {

			for (JobsModel j : results) {
				EmployerModel emp = employerRepository.findById(j.getEmployerId()).get();
				String image = emp.getCompanyLogo();
				if (image != null && !image.isEmpty()) {
					j.setJobPic(image);
				} else {
					j.setJobPic(
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
				}

			}
			HashMap<String, Object> hm = new HashMap<>();

			hm.put("statuscode", 200);
			hm.put("message", "success");
			hm.put("results", results.getContent());
			hm.put("nextPage", start + 1);
			hm.put("start", start);
			hm.put("recordsTotal", results.getTotalElements());
			hm.put("recordsFiltered", results.getSize());
			hm.put("Pageable", results.getPageable());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Related Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	public List<Integer> getValuesForGivenKey(List<JobsModel> matchedresults, String key) {
		JSONArray jsonArray = new JSONArray(matchedresults);
		return IntStream.range(0, jsonArray.length()).mapToObj(index -> ((JSONObject) jsonArray.get(index)).optInt(key))
				.collect(Collectors.toList());
	}

	@GetMapping(path = "/userRelatedJobs")
	public ResponseEntity<?> getUserRelatedJobs(@RequestParam("job_id") final List<Integer> jobIDs,
			@RequestParam("experience") final int experience, @RequestParam("industry") final String industry,
			@RequestParam("job_category") final List<String> jobCategory, @RequestParam("city") final String city) {
		List<JobsModel> results = null;
		List<JobsModel> results1 = null;

		String status = "O";

		String[] elements = city.split(",");
		List<String> fixedLenghtList = Arrays.asList(elements);

		int industryId = industryRepository.findByIndustry(industry);

		results = em.createQuery("SELECT e FROM JobsModel e " + "left join EmployerModel emp on e.employerId = emp.id "
				+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
				+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
				+ "where emp.kycStatus IN ('V','U') and e.jobCategory NOT IN :jobCategory AND e.jobStatus = :status "
				+ "AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and " + "cji.id = :indId")
				.setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
				.setParameter("status", status).setParameter("indId", industryId).getResultList();

		results1 = em.createQuery("SELECT e FROM JobsModel e " + "left join EmployerModel emp on e.employerId = emp.id "
				+ "left join CfgFullTimeGroup cfg on trim(lower(cfg.groupName)) = trim(lower(e.jobCategory)) "
				+ "left join IndustryModel cji " + "on cji.id = cfg.industryId and cji.industry = e.industry "
				+ "where emp.kycStatus IN ('V','U') and e.jobCategory IN :jobCategory AND e.jobStatus = :status "
				+ "AND e.jobLocation NOT IN :city AND e.jobExp <= :minExperience AND e.expiryDate >= CURRENT_DATE and "
				+ "cji.id = :indId").setParameter("jobCategory", jobCategory).setParameter("minExperience", experience)
				.setParameter("city", fixedLenghtList).setParameter("status", status).setParameter("indId", industryId)
				.getResultList();

		results.addAll(results1);

		ArrayList<JobsModel> temp_rem = new ArrayList<>();
		for (int jobId2 : jobIDs) {
			for (JobsModel j : results) {
				if (j.getId() == jobId2) {
					temp_rem.add(j);
				}
			}
		}

		results.removeAll(temp_rem);

		if (results != null && !results.isEmpty()) {

			for (JobsModel j : results) {
				EmployerModel emp = employerRepository.findById(j.getEmployerId()).get();
				String image = emp.getCompanyLogo();
				if (image != null && !image.isEmpty()) {
					j.setJobPic(image);
				} else {
					j.setJobPic(
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/defaultcompanylogo.png");
				}

			}
			Collections.reverse(results);

			Collections.sort(results, new Comparator<JobsModel>() {
				public int compare(JobsModel o1, JobsModel o2) {
					if (o1.getCreatedTime().compareTo(o2.getCreatedTime()) > 0) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", results);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Related Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("/jobDetails")
	public ResponseEntity<?> getJobById(@RequestParam("job_id") final int id) {
		Optional<JobsModel> jobs = jobRepository.findById(id);

		if (!jobs.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Jobs Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			JobsModel jobDetails = jobs.get();

			String isCPN = jobDetails.getIsViewContactPersonName();
			String isMN = jobDetails.getIsViewMobileNumber();
			String isEmail = jobDetails.getIsViewEmailId();

			try {
				if (isCPN.equalsIgnoreCase("false")) {
					jobDetails.setContactPersonName("");
				}
				if (isMN.equalsIgnoreCase("false")) {
					jobDetails.setMobileNumber("0");
				}
				if (isEmail.equalsIgnoreCase("false")) {
					jobDetails.setEmailId("");
				}
			} catch (Exception e) {

			}

			Optional<EmployerModel> emp = employerRepository.findById(jobDetails.getEmployerId());
			if (emp.get().getCompanyLogo() != null) {
				jobDetails.setJobPic(emp.get().getCompanyLogo());
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", jobDetails);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@GetMapping(path = "/userPaymentStatus")
	public ResponseEntity<?> getIsUserPaid(@RequestParam("user_id") final int userId,
			@RequestParam("job_id") final int id) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			String status = "Paid";
			CandidateModel paymentStatus = candidateRepository.findByPaymentStatus(userId, status);

			if (paymentStatus != null) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "success");
				map.put("results", jobRepository.getById(id));

				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Payment Required");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(path = "/interestedJobs")
	public ResponseEntity<?> setInterestedJobs(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("job_id") final int jobId, @RequestParam("status") final String status,
			@RequestParam(value = "device_token", required = false) String deviceToken) throws IOException {
		JobApplicationModel user = new JobApplicationModel();

		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobId(candidateId, jobId);

		Optional<JobsModel> jobsModel = jobRepository.findById(jobId);

		Optional<EmployerModel> emp = employerRepository.findById(jobsModel.get().getEmployerId());

		if (!details.isPresent()) {

			user.setCandidateId(candidateId);
			user.setJobId(jobId);
			user.setStatus(status);

			if (status.equalsIgnoreCase("I")) {

				Optional<CandidateModel> optional = candidateRepository.findById(candidateId);
				if (optional.isPresent()) {
					CandidateModel candidateDet = optional.get();

					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();

					String date = dtf.format(now);
					Date currentDate = null;

					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						user.setAppliedTime(currentDate);

					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.error(
								"error [" + e1.getMessage() + "] occurred while creating [" + date + "] applied time");

						CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
						logEventModel.setType("Job");
						logEventModel.setMessage("failure");
						logEventModel.setDescription(
								"error [" + e1.getMessage() + "] occurred while creating [" + date + "] applied time");

						try {
							cloudWatchLogService.cloudLogFailure(logEventModel, "C");
						} catch (Exception e) {

						}

					}

					jobApplicationRepository.save(user);

					candidateRepository.save(candidateDet);

					JobCanResponsesModel res = jobCanResponsesRepository.findByCanJobId(jobId, candidateId);
					if (res == null) {
						JobCanResponsesModel r = new JobCanResponsesModel();
						r.setJobId(jobId);
						r.setCanId(candidateId);
						r.setResponse("I");
						r.setResponseCount(1);
						jobCanResponsesRepository.save(r);

						JobsModel j = jobsModel.get();
						if (j.getCanResponseCount() == 1) {

							j.setCanResCompletedOn(currentDate);

							if (activeProfile.equalsIgnoreCase("prod")) {
								if (jobsModel.get().isFreetrialJob()) {
									HashMap<String, String> d = new HashMap<>();
									d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
									d.put("webLink", "https://web.taizo.in/console/pricing#employer");
									d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

									/* waAlertService.sendFreeJobLimitOverAlertToEmployer(d); */
								} else {
									HashMap<String, String> d = new HashMap<>();
									d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
									d.put("jobCategory", jobsModel.get().getJobCategory());
									d.put("webLink", "https://web.taizo.in/console/manage-jobs");
									d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

									waAlertService.sendJobLimitOverAlertToEmployer(d);
								}
							}
						}
						j.setCanResponseCount(j.getCanResponseCount() - 1);
						jobRepository.save(j);

					} else {
						res.setResponseCount(res.getResponseCount() + 1);
						jobCanResponsesRepository.save(res);
					}

					sendJobNoti(emp, jobsModel, candidateId, candidateDet);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "successfully saved");
					return new ResponseEntity<>(map, HttpStatus.OK);

				}
			}

			jobApplicationRepository.save(user);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "successfully saved");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 403);
			map.put("message", "Already applied for this job");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@SuppressWarnings("unchecked")
	@PostMapping(path = "/jobApplicationStatus")
	public ResponseEntity<?> applyJob(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("job_id") final int jobId, @RequestParam("status") final String status,
			@RequestParam(value = "device_token", required = false) String deviceToken) throws IOException {
		JobApplicationModel user = new JobApplicationModel();

		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobId(candidateId, jobId);

		Optional<JobsModel> jobsModel = jobRepository.findById(jobId);

		Optional<EmployerModel> emp = employerRepository.findById(jobsModel.get().getEmployerId());

		if (!details.isPresent()) {

			user.setCandidateId(candidateId);
			user.setJobId(jobId);
			user.setStatus(status);

			Optional<CandidateModel> optional = candidateRepository.findById(candidateId);
			CandidateModel candidateDet = optional.get();

			if (status.equalsIgnoreCase("I")) {

				int amount = candidateDet.getAmount() + candidateDet.getDiscountAmount();
				if (amount >= 2) {

					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();

					String date = dtf.format(now);
					Date currentDate = null;

					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						user.setAppliedTime(currentDate);

					} catch (ParseException e1) {
						e1.printStackTrace();
					}

					jobApplicationRepository.save(user);

					if (candidateDet.getAmount() >= 2) {
						int remainingAmnt = candidateDet.getAmount() - 2;
						candidateDet.setAmount(remainingAmnt);
					} else {
						int remainingAmnt = candidateDet.getDiscountAmount() - 2;
						candidateDet.setDiscountAmount(remainingAmnt);
					}

					candidateRepository.save(candidateDet);

					JobCanResponsesModel res = jobCanResponsesRepository.findByCanJobId(jobId, candidateId);
					if (res == null) {
						JobCanResponsesModel r = new JobCanResponsesModel();
						r.setJobId(jobId);
						r.setCanId(candidateId);
						r.setResponse("I");
						r.setResponseCount(1);
						jobCanResponsesRepository.save(r);

						JobsModel j = jobsModel.get();
						if (j.getCanResponseCount() == 1) {
							j.setCanResCompletedOn(currentDate);
	
							if (activeProfile.equalsIgnoreCase("prod")) {
								String jobStatus = "Paid";
								String eventName = "Job Response Limit Reached";
								if (jobsModel.get().isFreetrialJob()) {
									jobStatus = "Free Trial";
									eventName = "Free Job Response Limit Reached";
								}
								HashMap<String, String> data1 = new HashMap<>();
								data1.put("Event Name", "Job Alert");
								data1.put("Event Type", eventName);
								data1.put("Type", "Job");
								data1.put("Company Name", j.getCompanyName());
								data1.put("Contact Person Name", j.getContactPersonName());
								data1.put("Position", j.getJobCategory());
								data1.put("Experience",
										String.valueOf(j.getJobExp()) + " to " + String.valueOf(j.getJobMaxExp()));
								data1.put("Source", "App");
								data1.put("Mobile Number", String.valueOf(j.getMobileNumber()));
								data1.put("Job Status", jobStatus);
								data1.put("ID Type", "Job ID");
								data1.put("ID", String.valueOf(j.getId()));

								exotelCallController.connectToAgent("+91" + String.valueOf(j.getMobileNumber()), "Emp",
										data1);
								if (jobsModel.get().isFreetrialJob()) {
									HashMap<String, String> d = new HashMap<>();
									d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
									d.put("webLink", "https://web.taizo.in/console/pricing#employer");
									d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

									/* waAlertService.sendFreeJobLimitOverAlertToEmployer(d); */
								} else {
									HashMap<String, String> d = new HashMap<>();
									d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
									d.put("jobCategory", jobsModel.get().getJobCategory());
									d.put("webLink", "https://web.taizo.in/console/manage-jobs");
									d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

									waAlertService.sendJobLimitOverAlertToEmployer(d);
								}
							}
						}
						j.setCanResponseCount(j.getCanResponseCount() - 1);
						jobRepository.save(j);

					} else {
						res.setResponseCount(res.getResponseCount() + 1);
						jobCanResponsesRepository.save(res);
					}

					sendJobNoti(emp, jobsModel, candidateId, candidateDet);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "successfully saved");
					return new ResponseEntity<>(map, HttpStatus.OK);

				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "You have insufficient balance");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

				}

			}

			jobApplicationRepository.save(user);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "successfully saved");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Already applied for this job");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@Async
	private void sendJobNoti(Optional<EmployerModel> emp, Optional<JobsModel> jobsModel, int candidateId,
			CandidateModel candidateDet) {
		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String jdate = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/" + emp.get().getId()
				+ "/" + candidateId + "/" + jobsModel.get().getId() + "&apn=" + firebaseEmpPackage);

		// System.out.println(ex.getLongDynamicLink());
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

		if (activeProfile.equalsIgnoreCase("prod")) {
			String edu = candidateDet.getQualification();
			if (candidateDet.getSpecification() != null && !candidateDet.getSpecification().isEmpty()) {
				edu = candidateDet.getSpecification();
			}
			HashMap<String, String> d = new HashMap<>();
			d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
			d.put("name", candidateDet.getFirstName());
			d.put("jobCategory", jobsModel.get().getJobCategory());
			d.put("exp", String.valueOf(candidateDet.getExperience()));
			d.put("expMonths", String.valueOf(candidateDet.getExpMonths()));
			d.put("qualification", edu);
			d.put("keySkills", candidateDet.getKeySkill());
			d.put("webLink", "https://web.taizo.in/console/candidates");
			d.put("appLink", response.getShortLink());

			waAlertService.sendApplyAlertToEmployer(d);

			if (jobsModel.get().getAlternateMobileNumber() != null
					&& !jobsModel.get().getAlternateMobileNumber().isEmpty()) {
				HashMap<String, String> d1 = new HashMap<>();
				d1.put("mn", "91" + String.valueOf(jobsModel.get().getAlternateMobileNumber()));
				d1.put("name", candidateDet.getFirstName());
				d1.put("jobCategory", jobsModel.get().getJobCategory());
				d1.put("exp", String.valueOf(candidateDet.getExperience()));
				d1.put("expMonths", String.valueOf(candidateDet.getExpMonths()));
				d1.put("qualification", edu);
				d1.put("keySkills", candidateDet.getKeySkill());
				d1.put("webLink", "https://web.taizo.in/console/candidates");
				d1.put("appLink", response.getShortLink());

				waAlertService.sendApplyAlertToEmployer(d1);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@PutMapping(path = "/interestedJobs1")
	public ResponseEntity<?> setInterestedJobs1(@RequestParam("candidate_id") final int candidateId,
			@RequestPart(name = "job_video", required = false) MultipartFile video,
			@RequestParam("job_id") final int jobId, @RequestParam("status") final String status,
			@RequestParam(value = "device_token", required = false) String deviceToken) throws IOException {

		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobId(candidateId, jobId);

		Optional<JobsModel> jobsModel = jobRepository.findById(jobId);

		Optional<EmployerModel> emp = employerRepository.findById(jobsModel.get().getEmployerId());

		if (details.isPresent()) {

			JobApplicationModel existing = details.get();

			existing.setStatus(status);

			if (video != null && !video.isEmpty()) {

				String jobvideo = existing.getJobVideo();

				if (jobvideo != null && !jobvideo.isEmpty()) {

					boolean imageResult = candidateService.deleteImage(jobvideo);

				}

			}

			if (video != null && !video.isEmpty()) {
				String Skillvideo = candidateService.uploadJobFile(video, candidateId, video.getBytes());
				if (Skillvideo != null && !Skillvideo.isEmpty()) {

					existing.setJobVideo(Skillvideo);
				}
			}

			if (status.equalsIgnoreCase("Applied") || status.equalsIgnoreCase("I")) {

				Optional<CandidateModel> optional = candidateRepository.findById(candidateId);
				if (optional.isPresent()) {
					CandidateModel candidateDet = optional.get();

					/*
					 * int amount = candidateDet.getAmount(); if (amount >= 5) { int remainingAmnt =
					 * amount - 5; candidateDet.setAmount(remainingAmnt);
					 */

					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();

					String date = dtf.format(now);
					Date currentDate;

					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						existing.setAppliedTime(currentDate);

					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.error(
								"error [" + e1.getMessage() + "] occurred while creating [" + date + "] applied time");

						CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
						logEventModel.setType("Job");
						logEventModel.setMessage("failure");
						logEventModel.setDescription(
								"error [" + e1.getMessage() + "] occurred while creating [" + date + "] applied time");

						try {
							cloudWatchLogService.cloudLogFailure(logEventModel, "C");
						} catch (Exception e) {

						}

					}

					jobApplicationRepository.save(existing);

					candidateRepository.save(candidateDet);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "successfully saved");
					return new ResponseEntity<>(map, HttpStatus.OK);

					/*
					 * } else { HashMap<String, Object> map = new HashMap<>(); map.put("statuscode",
					 * 400); map.put("message", "You have insufficient balance"); return new
					 * ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
					 * 
					 * }
					 */

				}
			}

			jobApplicationRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "successfully saved");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(path = "/appliedJobs")
	public ResponseEntity<?> getAppliedandSavedJobs(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("status") final String status) {

		if (candidateId != 0) {

			List<JobApplicationModel> persons1 = null;
			List<JobsModel> jobs = new ArrayList<JobsModel>();

			ArrayList<Integer> time = new ArrayList<Integer>();
			List<JobsModel> persons = new ArrayList<JobsModel>();

			persons1 = em.createQuery(
					"select j from JobApplicationModel j where j.candidateId = :candidateId and j.status = :status")
					.setParameter("candidateId", candidateId).setParameter("status", status).getResultList();

			if (!persons1.isEmpty()) {

				int ut = 0;

				for (JobApplicationModel s : persons1) {

					Date date = s.getUpdatedTime();
					if (date == null) {
						s.setUpdatedTime(s.getCreatedTime());

					}

				}

				persons1.sort(Comparator.comparing(o -> o.getUpdatedTime()));

				for (JobApplicationModel s : persons1) {

					ut = s.getJobId();
					Optional<JobsModel> job = jobRepository.findById(ut);
					if (job.isPresent() && job != null) {
						time.add(ut);

					}

				}

				persons = em.createQuery("SELECT j FROM JobsModel j WHERE j.id IN :ids ORDER BY FIELD(j.id,:ids)")
						.setParameter("ids", time).getResultList();

				if (!persons.isEmpty()) {

					for (JobsModel j1 : persons) {

						int empId = j1.getEmployerId();

						Optional<EmployerModel> optional = employerRepository.findById(empId);
						EmployerModel existing = optional.get();

						if (existing.getCompanyLogo() != null) {
							j1.setJobPic(existing.getCompanyLogo());
						} else {
							j1.setJobPic(
									"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/job-image-default.png");
						}

						Optional<JobApplicationModel> appl = jobApplicationRepository
								.findByCandidateIdAndJobId(candidateId, j1.getId());

						JobApplicationModel j = appl.get();
						Date date = j.getUpdatedTime();
						if (date == null) {
							j1.setUpdatedTime(j.getCreatedTime());

						} else {
							j1.setUpdatedTime(j.getUpdatedTime());

						}

					}
				}
			}
			List<CanInterviewsModel> list = canInterviewRepository.findByCanIdandStatus(candidateId);
			list.forEach(interview -> {
				Optional<JobsModel> job = jobRepository.findById(interview.getJobId());
				jobs.add(job.get());
			});
			if (!jobs.isEmpty()) {
				persons.addAll(jobs);
			}

			Collections.reverse(persons);

			if (!persons.isEmpty()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "success");
				map.put("results", persons);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "No Jobs");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
//				} else {
//					HashMap<String, Object> map = new HashMap<>();
//					map.put("statuscode", 400);
//					map.put("message", "No Jobs");
//					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
//				}

		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("statuscode", 400);
		map.put("message", "User id is Not Found");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}

	@Transactional
	@DeleteMapping(path = "/deleteNotInterested")
	public ResponseEntity<?> empcandidateStatus(@RequestParam("can_id") final int canId,
			@RequestParam("job_id") final int jobId, @RequestParam("status") final String status) {
		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobIdAndStatus(canId,
				jobId, status);

		if (details.isPresent()) {

			jobApplicationRepository.deleteById(details.get().getId());

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job status is empty");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/jobStatus")
	public ResponseEntity<?> canJobStatus(@RequestParam("can_id") final int canId,
			@RequestParam("job_id") final int jobId) {
		Optional<JobApplicationModel> details = jobApplicationRepository.findTopByCandidateIdAndJobId(canId, jobId);

		if (details.isPresent()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job status is empty");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(path = "/jobCallUpdate")
	public ResponseEntity<?> canCallStatus(@RequestParam("can_id") final int canId,
			@RequestParam("job_id") final int jobId) {

		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 200);
		map.put("message", "success");
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

}
