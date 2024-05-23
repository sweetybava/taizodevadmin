
package com.taizo.controller.admin;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp; 
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.taizo.controller.admin.AdminCandidateController.CandidateFilterResponse;
import com.taizo.controller.admin.AdminEmployerController.EmployerFieldLeadFilterResponse;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.*;
import com.taizo.utils.FreeMarkerUtils;
import io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/admin")

public class AdminDashboardController {

	@Autowired
	CanInterviewRepository caninterviewrepository;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanDetailsRepository;
	
	@Autowired
	AdminService adminService;
	
	@Autowired
	private FreeMarkerUtils freeMarkerUtils;
	
	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;
	
	@PersistenceContext
	EntityManager em;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerService employerservice;

	@Autowired
	EmployerPaymentRepository employerPaymentRepository;
	
	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	private UserService userService;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	JobService jobservice;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	CandidateService candidateservice;

	@Autowired
	InterviewAddressRepository interviewAddressRepository;

	@Autowired
	InterviewRepository interviewRepository;

	@Autowired
	EmployerApplicationRepository employerApplicationRepository;

	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;

	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JobLeadService jobLeadService;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	WAAlertService waAlertService;

	@Autowired
	private DraftJobsService draftJobsService;

	@Autowired
	private DraftJobsRepository draftJobsRepository;
	
	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;

	@Autowired
	CanLanguagesRepository canlanguagesRepository;
	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Value("${gallabox.channel.id}")
	private String channelId;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;
	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;
	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;
	
	 private final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);


	@GetMapping("/allAdmins")
	public List<Admin> getAllAdmins() {
		// Call the service to get all admin details
		return adminService.getAllAdmins();
	}

	@GetMapping("/checkMobileNo")
	public ResponseEntity<Map<String, Object>> checkMobileNumberPresence(@RequestParam long mobileNumber) {
		EmployerModel employer = employerRepository.findByMobileNumber(mobileNumber);

		HashMap<String, Object> responseMap = new HashMap<>();
		if (employer != null) {
			responseMap.put("status", "success");
			responseMap.put("message", "Mobile number is present.");
			responseMap.put("code", HttpStatus.OK.value());
			responseMap.put("data", employer);
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else {
			responseMap.put("status", "error");
			responseMap.put("code", HttpStatus.NOT_FOUND.value());
			responseMap.put("message", "Mobile number is not present.");
			return new ResponseEntity<>(responseMap, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/recentActivities")
	public ResponseEntity<?> getEmpDashboardDetails(@RequestParam("start_date") final String startDate,
			@RequestParam("end_date") final String endDate, @RequestParam("page") int start,
			@RequestParam("size") int length) {

		int page = start / length; // Calculate page number

		Pageable pageable = PageRequest.of(start, length, new Sort(Sort.Direction.DESC, "created_time"));

		Page<EmployerActivityModel> activities = empActivityRepository.getEmpAdminRecentActivity(startDate, endDate,
				pageable);
		if (activities.hasContent()) {

			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", activities.getContent());
			hm.put("code", 200);
			hm.put("start", start);
			hm.put("recordsTotal", activities.getTotalElements());
			hm.put("recordsFiltered", activities.getTotalElements());
			return new ResponseEntity<>(hm, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Recent Activities");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/dashboardCount")
	public ResponseEntity<?> getCountDetails() {

		Map<String, Object> details = adminRepository.findCount();

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/empDetail")
	public ResponseEntity<?> getAllEmpDetails(@RequestParam("pageNo") int start, @RequestParam("pageSize") int length,
			EmployerModel model) {

		Pageable pageable = PageRequest.of(start, length);

		Page<EmployerModel> details = employerRepository.findAllEmpDetails(pageable);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put("data", details.getContent());
		hm.put("start", start);
		hm.put("recordsTotal", details.getTotalElements());
		hm.put("recordsFiltered", details.getTotalElements());
		return new ResponseEntity<>(hm, HttpStatus.OK);
	}

	@PutMapping(path = "/updateEmployerDetails", consumes = "application/json")
	public ResponseEntity<?> updateEmployereDetails(@RequestBody EmployerModel model)

	{
		Optional<EmployerModel> details = employerRepository.findById(model.getId());

		if (details.isPresent()) {

			EmployerModel emp = details.get();
			emp.setCompanyName(model.getCompanyName());
			employerRepository.save(emp);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/canDetails")
	public ResponseEntity<?> getAllcanDetails(@RequestParam("pageNo") int start, @RequestParam("pageSize") int length) {

		Pageable pageable = PageRequest.of(start, length);

		Page<CandidateModel> details = candidateRepository.findAllcanDetails(pageable);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put("data", details.getContent());
		hm.put("start", start);
		hm.put("recordsTotal", details.getTotalElements());
		hm.put("recordsFiltered", details.getTotalElements());
		return new ResponseEntity<>(hm, HttpStatus.OK);
	}

	@PutMapping(path = "/updateCandidateDetails", consumes = "application/json")
	public ResponseEntity<?> updateCandidateDetails(@RequestBody CandidateModel model)

	{
		Optional<CandidateModel> details = candidateRepository.findById(model.getId());

		if (details.isPresent()) {

			CandidateModel can = details.get();
			can.setFirstName(model.getFirstName());
			candidateRepository.save(can);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/jobFilter")
	public ResponseEntity<List<Map<String, Object>>> filterJobs(@RequestBody JobsModel jobModel) {
		List<Map<String, Object>> filteredJobs = jobservice.filterJobs(jobModel.getPriority(),jobModel.getEmployerId(), jobModel.getGender(),jobModel.getCompanyName(),
				jobModel.getJobLocation(), jobModel.getArea(), jobModel.getIndustry(), jobModel.getJobCategory(),
				jobModel.getBenefits(), jobModel.getKeyskills(), jobModel.getQualification(),jobModel.getAssignTo(), jobModel.getSalary(),
				jobModel.getMaxSalary(), jobModel.getJobExp(), jobModel.getJobMaxExp(), jobModel.getPages(),
				jobModel.getSize(), jobModel.getCreatedTime(), jobModel.getEndDate());
		return ResponseEntity.ok(filteredJobs);
	}
	


	@PostMapping("/candidateFilter")
	public ResponseEntity<?> filtercandidate(@RequestBody CandidateModel candidatemod) {
		List<Map<String, Object>> candidate = candidateservice.filtercandidate(candidatemod.getGender(),candidatemod.getEligibility(),candidatemod.getAssignTo(),candidatemod.getMobileNumber(),
				candidatemod.getIndustry(), candidatemod.getJobCategory(),candidatemod.getSpecification(), candidatemod.getQualification(),
				candidatemod.getCandidateType(), candidatemod.getSkills(), candidatemod.getPrefLocation(),
				candidatemod.getPassed_out_year(), candidatemod.getExperience(), candidatemod.getMaxExperience(),
				candidatemod.getPages(), candidatemod.getSize(), candidatemod.getCreatedTime(),
				candidatemod.getEndDate());
		return ResponseEntity.ok(candidate);
	}

	
	
	@PostMapping("/employerFilter")
	public ResponseEntity<?> filteremployer(@RequestBody EmployerModel employermod) {
		List<Map<String, Object>> employer = employerservice.filteremployer(employermod.getIndustry(),
				employermod.getNoOfEmployees(), employermod.getCity(), employermod.getPlan());
		return ResponseEntity.ok(employer);
	}
 
	@GetMapping("/adminJobFilterOptions")
	public ResponseEntity<List<Map<String, Object>>> getJobConfigData() {
		List<Map<String, Object>> jobConfigData = jobservice.getJobConfigDataAsObjects();
		return ResponseEntity.ok(jobConfigData);
	}

	
	@PostMapping("/adminCandidateInterviewScheduled")
	public ResponseEntity<?> candidateInterview(@RequestParam("can_id") int canId, @RequestParam("job_id") int jobId,
			@RequestParam("documents") String doc, @RequestParam("interview_date") String interviewdate) {
		Optional<CandidateModel> candidateDetails = candidateRepository.findById(canId);
		Optional<JobsModel> jobDetails = jobRepository.findById(jobId);

		if (candidateDetails.isPresent() && jobDetails.isPresent()) {
			CandidateModel candidate = candidateDetails.get();
			JobsModel job = jobDetails.get();

			CanInterviewsModel canInterview = new CanInterviewsModel();

			canInterview.setCanId(canId);
			canInterview.setJobId(jobId);
			canInterview.setInterviewScheduledDt(canInterview.getInterviewScheduledDt());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date interviewDate = null;
			try {
				interviewDate = dateFormat.parse(interviewdate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			canInterview.setInterviewDate(interviewdate);
			canInterview.setInterviewTime(canInterview.getInterviewTime());
			canInterview.setStatus("I");
			canInterview.setActive(true);
			canInterview.setDocuments(doc);
			canInterview.setCity(canInterview.getCity());
			canInterview.setArea(canInterview.getArea());

			long contactNumber = Long.parseLong(job.getMobileNumber());
			canInterview.setContactNumber(contactNumber);
			canInterview.setContactPersonName(job.getContactPersonName());
			canInterview.setCompanyName(job.getCompanyName());
			canInterview.setCity(job.getJobLocation());
			canInterview.setArea(job.getArea());

			caninterviewrepository.save(canInterview);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Scheduled Successfully");
			return ResponseEntity.ok(response);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", 400);
		response.put("message", "id already exists");
		return ResponseEntity.badRequest().body(response);
	}
	@GetMapping("candidate/{idOrNumber}")
	public ResponseEntity<?> getCandidateDetailsByIdOrNumber(@PathVariable("idOrNumber") String idOrNumber) {
		try {
			// Try parsing the input as an integer (ID)
			int id = Integer.parseInt(idOrNumber);
			CandidateModel candidate = candidateservice.getCandidateDetailsById(id);
			List<CanLanguageModel> details = canLanguagesRepository.findByCandidateId(candidate.getId());
			if (candidate != null) {
					List<LanguagesModel> persons = null;
					Set<Integer> list = new HashSet();

					int j = 0;

					for (CanLanguageModel s : details) {

						j = s.getLanguageId();
						list.add(j);
					}
					  if (list.isEmpty()) {
			                // Handle the case where the list is empty, e.g., return an empty result.
			                persons = new ArrayList<>(); // Empty list
			            } else {
			                persons = em.createQuery("SELECT j FROM LanguagesModel j WHERE j.id IN :ids")
			                    .setParameter("ids", list)
			                    .getResultList();
			            }

					
					Map<String, Object> response = new HashMap<>();
					response.put("statusCode", 200);
					response.put("message", "Candidate details");
					response.put("response", candidate);
					response.put("response1", persons);
					return ResponseEntity.ok(response);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Candidate Not Found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} catch (NumberFormatException e) {
			// If parsing as an integer fails, try parsing as a long (number)
			try {
				long number = Long.parseLong(idOrNumber);
				CandidateModel candidate = candidateservice.getCandidateDetailsByNumber(number);
				if (candidate != null) {
					return ResponseEntity.ok(candidate);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Candidate Not Found");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			} catch (NumberFormatException ex) {
				// If both parsing attempts fail, return a 400 Bad Request response with error
				// message
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "id or Number Not Found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}
	}


	@GetMapping("/employers")
	public ResponseEntity<Map<String, Object>> getEmployers(@RequestParam(required = false) String input,
			@RequestParam(required = false) String companyName, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Map<String, Object> response = new HashMap<>();

		if (input != null && !input.isEmpty()) {
			try {
				long inputNumber = Long.parseLong(input);
				Optional<EmployerModel> employer = employerservice
						.findByIdOrMobileNumberOrWhatsappNumber((int) inputNumber, inputNumber, inputNumber);
				if (employer.isPresent()) {
					response.put("status", "success");
					response.put("message", "success");
					response.put("code", 200);
					response.put("data", new ArrayList<>(Arrays.asList(employer.get())));
					response.put("currentPage", 0);
					response.put("totalPages", 0);
					response.put("totalEmployersCount", 1);
					return ResponseEntity.ok(response);
				} else {
					response.put("code", 400);
					response.put("message", "Employer Not Found");
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
				}
			} catch (NumberFormatException ex) {
				// If the input is not a valid number, return an error response
				response.put("code", 400);
				response.put("message",
						"Invalid input format. Please provide a valid employer ID or mobile/WhatsApp number.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

		}

		else if (companyName != null && !companyName.isEmpty()) {
			// Search by companyName
			Page<EmployerModel> employerPage = employerservice.findByCompanyNameContainingIgnoreCase(companyName,
					pageable);
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", employerPage.getContent());
			map.put("currentPage", employerPage.getNumber());
			map.put("totalPages", employerPage.getTotalPages());
			map.put("totalEmployersCount", employerPage.getTotalElements());
			return ResponseEntity.ok(map);
		} else {
			// If no input is provided, return all employers using pagination
			Page<EmployerModel> employerPage = employerservice.getAllEmployersOrderedByCreatedTimeDesc(pageable);
			response.put("status", "success");
			response.put("message", "success");
			response.put("code", 200);
			response.put("data", employerPage.getContent());
			response.put("currentPage", employerPage.getNumber());
			response.put("totalPages", employerPage.getTotalPages());
			response.put("totalEmployersCount", employerPage.getTotalElements());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/employerPayments")
	public ResponseEntity<HashMap<String, Object>> getPaymentsByEmployerId(
			@RequestParam(required = false) Integer employerId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

		if (employerId != null) {
			long totalEmployerPaymentCount = employerservice.getTotalEmployerPaymentCount(employerId);
			Page<EmployerPaymentModel> payments = employerservice.getPaymentsByEmployerId(employerId, pageable);
			if (payments.isEmpty()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "error");
				map.put("message", "No payments found for the provided employer ID.");
				map.put("code", 400);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("message", "success");
				map.put("code", 200);
				map.put("data", new PageImpl<>(payments.getContent(), pageable, totalEmployerPaymentCount));
				return ResponseEntity.ok(map);
			}
		} else {
			long totalPaymentCount = employerservice.getTotalPaymentCount();
			Page<EmployerPaymentModel> payments = employerservice.getAllPayments(pageable);
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", new PageImpl<>(payments.getContent(), pageable, totalPaymentCount));
			return ResponseEntity.ok(map);
		}
	}

	 @GetMapping("/canLead")
	    public ResponseEntity<HashMap<String, Object>> getCanLeadModels(
	            @RequestParam(value = "profilePageNo", required = false) Integer profilePageNo,
	            @RequestParam(required = false) String fromSource,
	            @RequestParam(required = false) String jobCategory,
	            @RequestParam(value = "mobile_number", required = false) Long mobileNumber,
	            @RequestParam(required = false) Integer expYearsMin,
	            @RequestParam(required = false) Integer expYearsMax,
	            @RequestParam(required = false) String status,
	           // @RequestParam(required = false) String scheduledBy,
	            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeStart,
	            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeEnd,
	            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
	            @RequestParam(value = "size", defaultValue = "50") int pageSize) {

	        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdTime"));

	        Specification<CanLeadModel> spec = Specifications.where(null);

	        if (profilePageNo != null) {
	            spec = spec.and((root, query, builder) -> builder.equal(root.get("profilePageNo"), profilePageNo));
	        }

	        if (fromSource != null) {
	            spec = spec.and((root, query, builder) ->
	                    builder.or(
	                            builder.and(builder.equal(root.get("fromApp"), true), builder.equal(builder.literal("fromApp"), fromSource)),
	                            builder.and(builder.equal(root.get("fromWA"), true), builder.equal(builder.literal("fromWA"), fromSource)),
	                            builder.and(builder.equal(root.get("fromAdmin"), true), builder.equal(builder.literal("fromAdmin"), fromSource)),
	                            builder.and(builder.equal(root.get("fromFbMetaLeadAd"), true), builder.equal(builder.literal("fromFbMetaLeadAd"), fromSource))
	                    )
	            );
	        }

	        if (jobCategory != null) {
	            spec = spec.and((root, query, builder) -> builder.equal(root.get("jobCategory"), jobCategory));
	        }

	        if (mobileNumber != null) {
	            spec = spec.and((root, query, builder) ->
	                    builder.or(
	                            builder.equal(root.get("mobileNumber"), mobileNumber),
	                            builder.equal(root.get("whatsappNumber"), mobileNumber),
	                            builder.equal(root.get("contactNumber"), mobileNumber)
	                    )
	            );
	        }

	        if (expYearsMin != null && expYearsMax != null) {
	            spec = spec.and((root, query, builder) -> builder.between(root.get("expYears"), expYearsMin, expYearsMax));
	        }

	        if (status != null) {
	            if ("Qualified".equals(status)) {
	                spec = spec.and((root, query, builder) -> builder.equal(root.get("qualified"), true));
	            } else if ("NotQualified".equals(status)) {
	                spec = spec.and((root, query, builder) -> builder.equal(root.get("notQualified"), true));
	            } else {
	                spec = spec.and((root, query, builder) ->
	                        builder.and(
	                                builder.equal(root.get("notQualified"), false),
	                                builder.equal(root.get("qualified"), false)
	                        )
	                );
	            }
	        }
//	        if (scheduledBy != null) {
//	            try {
//	                Integer scheduledAdminId = Integer.parseInt(scheduledBy);
//	                spec = spec.and((root, query, builder) -> {
//	                    Join<CanLeadModel, Admin> adminJoin = root.join("assignTo", JoinType.INNER);
//	                    query.distinct(true); // To ensure distinct results
//
//	                    return builder.equal(adminJoin.get("id"), scheduledAdminId);
//	                });
//	            } catch (NumberFormatException e) {
//	                e.printStackTrace(); // Handle the case where "scheduledBy" is not a valid Integer
//	            }
//	        }



	        if (createdTimeStart != null && createdTimeEnd != null) {
	            LocalDate startDate = LocalDate.parse(createdTimeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	            LocalDate endDate = LocalDate.parse(createdTimeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

	            Date startDateTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	            Date endDateTime = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	            spec = spec.and((root, query, builder) ->
	                    builder.between(root.get("createdTime"), startDateTime, endDateTime)
	            );
	        }

	        Page<CanLeadModel> canLeadModels = candidateservice.getCanLeadModels(spec, pageable);

	        if (canLeadModels.hasContent()) {
	            HashMap<String, Object> map = new HashMap<>();
	            map.put("status", "success");
	            map.put("message", "success");
	            map.put("code", 200);
	            map.put("data", canLeadModels.getContent());
	            map.put("currentPage", canLeadModels.getNumber());
	            map.put("totalElements", canLeadModels.getTotalElements());
	            map.put("totalPages", canLeadModels.getTotalPages());
	            return ResponseEntity.ok(map);
	        } else {
	            HashMap<String, Object> map = new HashMap<>();
	            map.put("code", 404);
	            map.put("message", "Can Lead Model Not Found");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
	        }
	    }
	


	@GetMapping("/candidateCallLogs")
	public ResponseEntity<HashMap<String, Object>> getCallsByJidWithPagination(
			@RequestParam(required = false) Integer jid, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		Page<CandidateCallsModel> calls;

		if (jid == null) {
			calls = candidateservice
					.getAllCallsWithPagination(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "callTime")));
		} else {
			calls = candidateservice.getCallsByJidWithPagination(jid,
					PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "callTime")));
		}

		if (!calls.isEmpty()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", calls);
			return ResponseEntity.ok(map);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Data Not Found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
		}
	}

	public class CanInterviewFilterResponse {
	    private long totalCount;
	    private List<Map<String, Object>> canInterviewList;

	    public CanInterviewFilterResponse(long totalCount, List<Map<String, Object>> canInterviewList) {
	        this.totalCount = totalCount;
	        this.canInterviewList = canInterviewList;
	    }

	    public long getTotalCount() {
	        return totalCount;
	    }

	    public List<Map<String, Object>> getcanInterviewList() {
	        return canInterviewList;
	    }
	}
	
	@PostMapping("/canInterviews")
	public ResponseEntity<CanInterviewFilterResponse> getCanInterviews(@RequestBody CanInterviewsModel canmod){
	        List<Map<String,Object>> canint=candidateservice.filterCanInterview(canmod.getJobId(),canmod.getContactNumber(),
	        		canmod.getAdminId(),canmod.getInterviewDate(),canmod.getCompanyName(),canmod.getInterviewEndDate(),
	        		canmod.getPage(),canmod.getSize(),canmod.getCreatedTime(),canmod.getEndDate(),canmod.getCandidateMobileNumber(),canmod.getJobCategory(),canmod.getCity(),canmod.getArea(),canmod.getInterviewCurrentStatus());
	        long totalCount = candidateservice.filterCanInterviewCount(canmod.getJobId(),canmod.getContactNumber(),
	        		canmod.getAdminId(),canmod.getInterviewDate(),canmod.getCompanyName(),canmod.getInterviewEndDate(),canmod.getCreatedTime(),canmod.getEndDate(),canmod.getCandidateMobileNumber(),canmod.getJobCategory(),canmod.getCity(),canmod.getArea(),canmod.getInterviewCurrentStatus());
	        CanInterviewFilterResponse response = new CanInterviewFilterResponse(totalCount, canint);
	    return ResponseEntity.ok(response);
	}

	@GetMapping("/jobLeads")
	public ResponseEntity<Map<String, Object>> getJobLeads(
			@RequestParam(name = "employerId", required = false) Integer employerId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<JobLeadModel> result;
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

		if (employerId != null) {
			result = jobLeadService.findByEmployerId(employerId, page, size);
		} else {
			result = jobLeadService.getAllJobLeads(pageable);
		}

		if (result.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", 404);
			errorResponse.put("message", "Employer Not Found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("status", "success");
		successResponse.put("message", "success");
		successResponse.put("code", 200);
		successResponse.put("data", result);
		return ResponseEntity.ok(successResponse);
	}

	@GetMapping("/empRatingsCount")
	public ResponseEntity<Map<String, Object>> getRatingCount(
			@RequestParam(name = "rating_count", required = false) Integer ratingCount,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		Page<EmpJobRatingsModel> result;
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

		if (ratingCount != null) {
			result = employerservice.findByRatingCount(ratingCount, page, size);
		} else {
			result = employerservice.getAllRatingCount(pageable);
		}

		if (result.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", 404);
			errorResponse.put("message", "JobCount Not Found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("status", "success");
		successResponse.put("message", "success");
		successResponse.put("code", 200);
		successResponse.put("data", result);
		return ResponseEntity.ok(successResponse);
	}

	@GetMapping("/empCallRegistry")
	public ResponseEntity<Map<String, Object>> getRegistry(@RequestParam(name = "jid", required = false) Integer jId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		Page<EmployerCallModel> result;
		Pageable pageable = PageRequest.of(page, size, Sort.by("callTime").descending());

		if (jId != null) {
			result = employerservice.findByJid(jId, page, size);

		} else {
			result = employerservice.getAllCallRegistry(pageable);
		}

		if (result.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", 400);
			errorResponse.put("message", "CallRegistry Not Found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("status", "success");
		successResponse.put("message", "success");
		successResponse.put("code", 200);
		successResponse.put("data", result);
		return ResponseEntity.ok(successResponse);
	}

	@GetMapping(value = "/unPublishedJob")
	public ResponseEntity<Map<String, Object>> getDraftJobsByEmployerId(
			@RequestParam(name = "employer_id", required = false) Integer employerId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<EmpPlacementPlanDetailsModel> result;
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

		if (employerId != null) {
			result = employerservice.getUnPublishedJobsByEmployer(employerId, page, size);

		} else {
			result = employerservice.getAllunpublishedjob(pageable);
		}

		if (result.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", 400);
			errorResponse.put("message", "UnPublished Job Not Found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
		
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("status", "success");
		successResponse.put("message", "success");
		successResponse.put("code", 200);
		successResponse.put("data", result);
		return ResponseEntity.ok(successResponse);
	}

	

	@GetMapping("/empCheckMobileNo")
	public ResponseEntity<Map<String, Object>> checkMobileNumberPresent(@RequestParam long mobileNumber) {
		EmployerModel employer = employerRepository.findByMobileNumber(mobileNumber);
		List<LeadModel> emplead = leadRepository.findByMobileNumberList(mobileNumber);

		HashMap<String, Object> responseMap = new HashMap<>();
		if (employer != null) {
			responseMap.put("status", "success");
			responseMap.put("message", "Mobile number is present in employer table");
			responseMap.put("code", HttpStatus.OK.value());
			responseMap.put("data", employer);
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else if (!emplead.isEmpty()) {
			responseMap.put("status", "success");
			responseMap.put("message", "Mobile number is present in employerLead table");
			responseMap.put("code", HttpStatus.OK.value());
			responseMap.put("data", emplead);
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}

		else {
			responseMap.put("status", "error");
			responseMap.put("code", HttpStatus.NOT_FOUND.value());
			responseMap.put("message", "Mobile number is not present.");
			return new ResponseEntity<>(responseMap, HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(path = "/draftJobs")
	public ResponseEntity<?> createDraftJobs(@RequestBody DraftJobsModel draftJob) {
		try {

			int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

			draftJob.setEmpJobId(String.valueOf(jobUniqID));

			EmployerModel employer = employerRepository.findById(draftJob.getEmployerId()).orElse(null);
			if (employer != null) {
				draftJob.setAssignTo(employer.getAssignTo());
			}
			// Set the createdTime
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			draftJob.setCreatedTime(currentTime);

			// Save the job lead to the database
			DraftJobsModel savedDraftJob = draftJobsRepository.save(draftJob);

			// Prepare the response
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "draftJobs created successfully");
			response.put("jobLeadId", savedDraftJob.getId());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			// Handle any exceptions that occurred during the process
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 500);
			response.put("message", "An error occurred while creating the draft job");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/dashboardAnalytics")
	public ResponseEntity<Map<String, Object>> getAnalytics(
	    @RequestParam String time,
	    @RequestParam(required = false) String module
	) {
	    List<?> analyticsData;

	    if ("Employer".equalsIgnoreCase(module)) {
	        analyticsData = adminRepository.findByEmpAnalytics(time);
	    } else if ("Jobseeker".equalsIgnoreCase(module)) {
	        analyticsData = adminRepository.findByJSAnalytics(time);
	    } else if ("Jobs".equalsIgnoreCase(module)) {
	        analyticsData = adminRepository.findByJobsAnalytics(time);
	    } else {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("code", 400);
	        errorResponse.put("message", "Invalid module specified");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	    }

	    if (analyticsData.isEmpty()) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("code", 400);
	        errorResponse.put("message", "Analytics data not found");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	    }

	    Map<String, Object> successResponse = new HashMap<>();
	    successResponse.put("status", "success");
	    successResponse.put("message", "success");
	    successResponse.put("code", 200);
	    successResponse.put("data", analyticsData);

	    return ResponseEntity.ok(successResponse);
	}


	 @GetMapping("/adminAnalytics")
	    public ResponseEntity<List<Map<String, Object>>> getAdminAnalytics(
	            @RequestParam(name = "adminId", required = false) Long adminId,
	            @RequestParam(name = "module", required = false) String module,
	            @RequestParam(name = "createdOn", required = false) Timestamp createdOn,
	            @RequestParam(name = "dateFilter", required = false) String dateFilter) {

	        try {
	            List<Map<String, Object>> result = adminService.filterAdminAnalyticsList(adminId, module, createdOn, dateFilter);
	            return new ResponseEntity<>(result, HttpStatus.OK);
	        } catch (Exception e) {
	            // Handle exceptions appropriately
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	 
	 public class CanLeadFilterResponse {
		    private long totalCount;
		    private List<Map<String, Object>> canLeadList;

		    public CanLeadFilterResponse(long totalCount, List<Map<String, Object>> canLeadList) {
		        this.totalCount = totalCount;
		        this.canLeadList = canLeadList;
		    }

		    public long getTotalCount() {
		        return totalCount;
		    }

		    public List<Map<String, Object>> getcanLeadList() {
		        return canLeadList;
		    }
		}
	 
	 @PostMapping("/canLeadfilter")
	 public ResponseEntity<CanLeadFilterResponse> filterCanLead(@RequestBody CanLeadModel filterRequest) {
	     List<Map<String, Object>> canLeadList = candidateservice.filterCanLead(
	             filterRequest.getProfilePageNo(),
	             filterRequest.getFromSource(),
	             filterRequest.getJobCategory(),
	             filterRequest.getMobileNumber(),
	             filterRequest.getExpYears(),
	             filterRequest.getMaxExperience(),
	             filterRequest.getStatus(),
	             filterRequest.getScheduledBy(),
	             filterRequest.getCreatedTime(),
	             filterRequest.getEndDate(),
	             filterRequest.getPage(),
	             filterRequest.getSize()
	     );

	     long totalCount = candidateservice.filterCanLeadCount(
	             filterRequest.getProfilePageNo(),
	             filterRequest.getFromSource(),
	             filterRequest.getJobCategory(),
	             filterRequest.getMobileNumber(),
	             filterRequest.getExpYears(),
	             filterRequest.getMaxExperience(),
	             filterRequest.getStatus(),
	             filterRequest.getScheduledBy(),
	             filterRequest.getCreatedTime(),
	             filterRequest.getEndDate()
	     );

	     CanLeadFilterResponse response = new CanLeadFilterResponse(totalCount, canLeadList);
	    
	         return ResponseEntity.ok(response);
	    
	 }

	}

