package com.taizo.controller.employer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.gson.Gson;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.*;
import com.taizo.utils.TupleStore;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/employer")
public class EmployerJobController {

	private static final Logger logger = LogManager.getLogger(EmployerJobController.class);

	@Autowired
	JobRepository jobRepository;

	@Autowired
	CandidateCallRepository candidateCallRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	@Lazy
	NotificationService notificationService;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	JobService jobService;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	JobApplicationService jobApplicationService;

	@Autowired
	EmployerApplicationService employerApplicationService;

	@Autowired
	private AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	IndiaStateRepository indiaStateRepository;

	@Autowired
	JobDescVideosRepository jobDescVideosRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	IndustryRepository industryRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JobCloseReasonRepository jobCloseReasonRepository;

	@Autowired
	EmployerJobPersonalizationRepository employerJobPersonalizationRepository;

	@Autowired
	CfgEmpJobSalaryRepository cfgEmpJobSalaryRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	EmpJobShiftTimingsRepository empJobShiftTimingsRepository;

	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;

	@Autowired
	EmployerApplicationRepository employerApplicationRepository;

	@Autowired
	AdminService adminService;

	private String EMPLOYERJOBVIDEOPREFIX = "employer_job_video_";

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@GetMapping(path = "/employerDetails")
	public ResponseEntity<?> getEmployerDetails(@RequestParam("employer_id") final int employerId) {

		Optional<EmployerModel> details = employerRepository.findById(employerId);

		if (details != null && details.isPresent()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/updateEmployerNotificationRead")
	public ResponseEntity<?> storeRead(@RequestParam("eid") final int eid) {
		Optional<EmployerModel> employerModelOpt = employerRepository.findById(eid);
		if (employerModelOpt.isPresent()) {
			EmployerModel emp = employerModelOpt.get();
			emp.setNotificationLastReadDt(new Date());
			employerRepository.save(emp);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "failed");
		map.put("message", "failed");
		map.put("code", 400);
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}

	@GetMapping(path = "/getEmployerNotificationCount")
	public ResponseEntity<?> getNotifReadCount(@RequestParam("eid") final int eid) {
		Optional<EmployerModel> employerModelOpt = employerRepository.findById(eid);
		Page<CandidateCallModel> candidateCallModels = candidateCallRepository.getEmployerCallNotificationCount(eid,
				employerModelOpt.get().getNotificationLastReadDt(), PageRequest.of(0, 100));
		Page<EmpInterviewNotificationModel> empInterviewNotifi = empInterviewNotificationRepository
				.getEmployerInterviewNotificationCount(eid, employerModelOpt.get().getNotificationLastReadDt(),
						PageRequest.of(0, 100));
		Page<JobApplicationModel> jobApllication = jobApplicationRepository.getJobApplicationCount(eid,
				employerModelOpt.get().getNotificationLastReadDt(), PageRequest.of(0, 100));

		HashMap<String, Long> count = new HashMap<>();
		count.put("employerNotificationCount", candidateCallModels.getTotalElements()
				+ empInterviewNotifi.getTotalElements() + jobApllication.getTotalElements());

		ArrayList<HashMap<String, Long>> al = new ArrayList<>();
		al.add(count);
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", al);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@GetMapping(path = "/candidateDetails")
	public ResponseEntity<?> getCandidateDetails(@RequestParam("candidate_id") final int candidateId) {

		Optional<CandidateModel> details = candidateRepository.findById(candidateId);

		if (details.isPresent()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/jobs")
	public ResponseEntity<?> getEmployerJobs(@RequestParam("employer_id") final int employerId) {

		String status = "O";

		List<JobsModel> jobs = jobRepository.findEmployerJobs(employerId, status);

		if (!jobs.isEmpty()) {
			int count = jobs.size();

			Optional<EmployerModel> emp = employerRepository.findById(employerId);
			EmployerModel existing = emp.get();

			String kycStatus = existing.getKycStatus();

			for (JobsModel j : jobs) {

				String jobVideo = j.getJobVideo();

				if (jobVideo != null && !jobVideo.isEmpty()) {
					j.setVideoStatus("Yes");
				} else {
					j.setVideoStatus("No");

				}

				j.setKycStatus(kycStatus);

				if (existing.getProfilePic() != null) {
					j.setJobPic(existing.getProfilePic());
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", jobs);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Jobs Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/job", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<?> createJob(@ModelAttribute JobsModel job,
			@RequestPart(name = "job_video", required = false) MultipartFile video,
			@RequestParam(value = "device_token", required = false) String deviceToken)
			throws IOException, ParseException {

		int id = job.getEmployerId();

		Optional<EmployerModel> emp = employerRepository.findById(id);

		if (emp != null && emp.isPresent()) {

			EmployerModel empData = emp.get();
			int planId = empData.getPlan();

			if (planId != 0) {
				Optional<PlansModel> empPlan = plansRepository.findById(planId);
				PlansModel plan1 = empPlan.get();

				String date = empData.getExpiryDate();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				Date expiry = sdf.parse(date);

				String cDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

				Date curDate = sdf.parse(cDate);

				boolean expired = expiry.before(curDate);

				if (expired == true) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Plan Expired");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				} else {
					int openCount = empData.getPlanJobCount();

					if (openCount > 0) {

						if (video != null && !video.isEmpty()) {

							String jobVideo = jobService.uploadVideo(video, id, video.getBytes());
							if (jobVideo != null && !jobVideo.isEmpty()) {

								job.setJobVideo(jobVideo);

							} else {
								HashMap<String, Object> map = new HashMap<>();
								map.put("code", 400);
								map.put("message", "Video Not Saved");
								return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

							}
						}

						int jobPostValidity = plan1.getJobPostValidity();

						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
						Calendar c = Calendar.getInstance();
						c.setTime(new Date());
						c.add(Calendar.DATE, jobPostValidity);
						String output = sdf1.format(c.getTime());

						job.setExpiryDate(output);

						if (empData.getContactPersonName() == null) {
							empData.setContactPersonName(job.getContactPersonName());
							employerRepository.save(empData);
						}

						if (empData.getMobileNumber() == 0) {
							empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
							empData.setMobileCountryCode("91");
							employerRepository.save(empData);
							Optional<LeadModel> em = leadRepository
									.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
							if (em.isPresent()) {
								LeadModel l = em.get();
								l.setRegisteredInApp(true);
								leadRepository.save(l);
							}
						}

						String jImg = job.getJobPic();
						if (jImg != null && !jImg.isEmpty()) {
							job.setJobPic(jImg);
						} else {
							job.setJobPic(
									"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/job-image-default.png");
						}

						int ex = job.getJobExp();
						int maxEx = job.getJobMaxExp();
						int max = 0;
						if (ex >= 1 && ex <= 3) {
							max = 5;
						} else if (ex == 4) {
							max = 8;
						} else if (ex >= 5 && ex <= 8) {
							max = 10;
						} else if (ex == 9) {
							max = 15;
						} else if (ex >= 10) {
							max = 20;
						} else if (ex == 0 && maxEx == 1) {
							max = 1;
						} else {
						}

						job.setJobMaxExp(max);

						jobRepository.save(job);

						int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

						job.setEmpJobId(String.valueOf(jobUniqID));
						job = jobRepository.save(job);

						if (video != null && !video.isEmpty()) {

							try {
								adminService.uploadJobVideoFileToS3Bucket(video,
										EMPLOYERJOBVIDEOPREFIX + String.valueOf(job.getId()) + ".mp4");

							} catch (Exception e) {
								logger.error("error [" + e.getMessage() + "] occurred while uploading [" + video
										+ "] job video");
							}
						}

						EmployerActivityModel EA = new EmployerActivityModel();
						EA.setEmpId(job.getEmployerId());
						EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
						empActivityRepository.save(EA);

						Integer jobUpdateCount = emp.get().getPlanJobCount();
						jobUpdateCount -= 1;
						emp.get().setPlanJobCount(jobUpdateCount);
						employerRepository.save(emp.get());

						if (activeProfile.equalsIgnoreCase("prod")) {
							notificationService.sendNotification(job, empData);
						}

						DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
						String pdate = formatter.format(new Date());
						SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
						Calendar cal = Calendar.getInstance();
						String time = simpleDateFormat1.format(cal.getTime());

			
						HashMap<String, Object> map = new HashMap<>();
						map.put("code", 200);
						map.put("status", "success");
						map.put("message", "success");
						map.put("data", job);

						return new ResponseEntity<>(map, HttpStatus.OK);

					} else {
						HashMap<String, Object> map = new HashMap<>();
						map.put("code", 400);
						map.put("message", "Active Jobs Limit is exceeded");
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
					}
				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Plan is not selected");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/job/{id}")
	public ResponseEntity<?> getJobById(@PathVariable int id) {
		JobsModel job = jobRepository.findById(id).get();
		EmployerModel emp = employerRepository.findById(job.getEmployerId()).get();
		job.setJobPic(emp.getCompanyLogo());

		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", job);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@PostMapping(path = "/matchingProfiles")
	public ResponseEntity<?> getEmployerMatchingProfile(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("job_status") final String jobStatus1) {

		JobsModel jobs = jobRepository.findByIdAndEmployerId(jobId, employerId, jobStatus1);
		if (jobs != null) {

			String status = "I";

			List<JobApplicationModel> details = jobApplicationRepository.findByJobId(jobId);

			if (!details.isEmpty()) {
				List<JobApplicationModel> persons1 = null;
				List<EmployerApplication> empAppl = null;

				persons1 = em
						.createQuery(
								"select j from JobApplicationModel j where j.jobId = :jobId and j.status = :status")
						.setParameter("jobId", jobId).setParameter("status", status).getResultList();

				if (!persons1.isEmpty()) {

					List<CandidateModel> persons = null;
					List<Integer> list = new ArrayList();
					List<Integer> list1 = new ArrayList();

					int j = 0;
					int k = 0;

					for (JobApplicationModel s : persons1) {

						j = s.getCandidateId();
						list.add(j);
					}

					empAppl = em.createQuery(
							"select e from EmployerApplication e where e.employerId = :employerId and e.jobId = :jobId")
							.setParameter("employerId", employerId).setParameter("jobId", jobId).getResultList();

					for (EmployerApplication s : empAppl) {

						k = s.getCandidateId();
						list1.add(k);
					}

					if (!list1.isEmpty()) {

						persons = em
								.createQuery("SELECT c FROM CandidateModel c WHERE c.id IN :ids and c.id NOT IN :ids1")
								.setParameter("ids", list).setParameter("ids1", list1).getResultList();
						if (!persons.isEmpty()) {
							List<CandidateProfileViewModel> sListt = new ArrayList<CandidateProfileViewModel>();
							;

							int id;
							int userID;
							String firstName, lastName, jobType, student, prefCountry, prefLocation, candidateLocation,
									domesticLocation, overseasLocation, jobCategory, dateOfBirth, gender;
							String profilePic, currentCountry, currentState, currentCity, perCountry, perState, perCity,
									emailId, qualification, specification, candidateType;
							long mobileNumber;
							long whatsappNumber;
							Integer experience;
							Integer overseasExp;
							Integer overseasExpMonths;
							String expCertificate;
							String license;
							String paymentStatus;
							String skills;
							String certificationCourses;
							String certificationSpecialization;
							Integer experienceMonths;
							String age;
							String skillVideoType;
							String certificateType, licenseType;
							for (CandidateModel c : persons) {
								CandidateProfileViewModel tm = new CandidateProfileViewModel();

								id = c.getId();
								userID = c.getUserId();
								firstName = c.getFirstName();
								lastName = c.getLastName();
								jobType = c.getJobType();
								student = c.getStudent();
								prefCountry = c.getPrefCountry();
								prefLocation = c.getPrefLocation();
								candidateLocation = c.getCandidateLocation();
								domesticLocation = c.getDomesticLocation();
								overseasLocation = c.getOverseasLocation();
								jobCategory = c.getJobCategory();
								dateOfBirth = c.getDateOfBirth();
								gender = c.getGender();
								age = c.getAge();
								currentCountry = c.getCurrentCountry();
								currentState = c.getCurrentState();
								currentCity = c.getCurrentCity();
								perCountry = c.getPerCountry();
								perState = c.getPerState();
								perCity = c.getPerCity();
								mobileNumber = c.getMobileNumber();
								whatsappNumber = c.getWhatsappNumber();
								emailId = c.getEmailId();
								qualification = c.getQualification();
								certificationCourses = c.getCertificationCourses();
								certificationSpecialization = c.getCertificationSpecialization();
								specification = c.getSpecification();
								candidateType = c.getCandidateType();
								experience = c.getExperience();
								experienceMonths = c.getExpMonths();
								overseasExp = c.getOverseasExp();
								overseasExpMonths = c.getOverseasExpMonths();
								expCertificate = c.getExpCertificate();
								certificateType = c.getCertificateType();
								license = c.getLicense();
								licenseType = c.getLicenseType();
								paymentStatus = c.getPaymentStatus();
								skills = c.getSkills();
								skillVideoType = c.getSkillVideoType();

								List<CanLanguageModel> details1 = canLanguagesRepository.findByCandidateId(id);
								if (!details1.isEmpty()) {

									List<LanguagesModel> persons11 = null;
									Set<Integer> listt = new HashSet();

									int j1 = 0;

									for (CanLanguageModel s : details1) {

										j1 = s.getLanguageId();
										listt.add(j1);
									}

									persons11 = em
											.createQuery("SELECT j.languages FROM LanguagesModel j WHERE j.id IN :ids")
											.setParameter("ids", listt).getResultList();

									tm.setLanguages(persons11);
								}

								List<JobApplicationModel> appTime = null;

								appTime = em.createQuery(
										"select j from JobApplicationModel j where j.jobId = :jobId and j.candidateId = :candidateId and j.status = :status")
										.setParameter("jobId", jobId).setParameter("status", status)
										.setParameter("candidateId", c.getId()).getResultList();

								if (!appTime.isEmpty()) {
									for (JobApplicationModel c1 : appTime) {
										Date appliedTime = c1.getAppliedTime();
										tm.setAppliedTime(appliedTime);
									}
								}

								List<UserModel> user = userRepository.findUserId(id);

								for (UserModel u : user) {

									profilePic = u.getProfilePic();

									tm.setId(id);
									tm.setUserId(userID);
									tm.setFirstName(firstName);
									tm.setLastName(lastName);
									tm.setJobType(jobType);
									tm.setStudent(student);
									tm.setAge(age);
									tm.setPrefCountry(prefCountry);
									tm.setPrefLocation(prefLocation);
									tm.setCandidateLocation(candidateLocation);
									tm.setDomesticLocation(domesticLocation);
									tm.setOverseasLocation(overseasLocation);
									tm.setJobCategory(jobCategory);
									tm.setDateOfBirth(dateOfBirth);
									tm.setGender(gender);
									tm.setCurrentCountry(currentCountry);
									tm.setCurrentState(currentState);
									tm.setCurrentCity(currentCity);
									tm.setPerCountry(perCountry);
									tm.setPerState(perState);
									tm.setPerCity(perCity);
									tm.setMobileNumber(mobileNumber);
									tm.setWhatsappNumber(whatsappNumber);
									tm.setEmailId(emailId);
									tm.setQualification(qualification);
									tm.setCertificationCourses(certificationCourses);
									tm.setCertificationSpecialization(certificationSpecialization);
									tm.setSpecification(specification);
									tm.setCandidateType(candidateType);
									tm.setProfilePic(profilePic);

									tm.setExperience(experience);
									tm.setExperienceMonths(experienceMonths);
									tm.setOverseasExp(overseasExp);
									tm.setOverseasExpMonths(overseasExpMonths);

									List<String> expcer = null;
									List<String> expcerType = null;
									List<String> licen = null;
									List<String> licenType = null;
									if (expCertificate != null && !expCertificate.isEmpty()) {
										expcer = Arrays.asList(expCertificate.split("\\s*,\\s*"));
									}
									if (certificateType != null && !certificateType.isEmpty()) {
										expcerType = Arrays.asList(certificateType.split("\\s*,\\s*"));
									}
									if (license != null && !license.isEmpty()) {
										licen = Arrays.asList(license.split("\\s*,\\s*"));
									}
									if (licenseType != null && !licenseType.isEmpty()) {
										licenType = Arrays.asList(licenseType.split("\\s*,\\s*"));
									}

									if (candidateCallRepository.getCallDetails(tm.getId(), employerId, jobId)
											.isPresent()) {
										tm.setCallTime(candidateCallRepository
												.getCallDetails(tm.getId(), employerId, jobId).get().getCallTime());
									}
									tm.setExpCertificate(expcer);
									tm.setExpCertificateType(expcerType);
									tm.setLicense(licen);
									tm.setLicenseType(licenType);
									tm.setPaymentStatus(paymentStatus);
									tm.setSkills(skills);
									tm.setSkillVideoType(skillVideoType);

									sListt.add(tm);
								}

							}

							Collections.reverse(sListt);

							HashMap<String, Object> map = new HashMap<>();
							map.put("status", "success");
							map.put("message", "success");
							map.put("code", 200);
							map.put("data", sListt);
							return new ResponseEntity<>(map, HttpStatus.OK);

						} else {
							HashMap<String, Object> map = new HashMap<>();
							map.put("code", 400);
							map.put("message", "Candidates not found");
							return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
						}

					} else {
						persons = em.createQuery("SELECT c FROM CandidateModel c WHERE c.id IN :ids")
								.setParameter("ids", list).getResultList();
						if (!persons.isEmpty()) {
							List<CandidateProfileViewModel> sListt = new ArrayList<CandidateProfileViewModel>();
							;

							int id;
							int userID;
							String firstName, lastName, jobType, student, prefCountry, prefLocation, candidateLocation,
									domesticLocation, overseasLocation, jobCategory, dateOfBirth, gender;
							String profilePic, currentCountry, currentState, currentCity, perCountry, perState, perCity,
									emailId, qualification, specification, candidateType;
							long mobileNumber;
							long whatsappNumber;
							Integer experience;
							Integer overseasExp;
							Integer overseasExpMonths;
							String expCertificate;
							String license;
							String paymentStatus;
							String skills;
							String certificationCourses;
							String certificationSpecialization;
							Integer experienceMonths;
							String age;
							String skillVideoType;
							String certificateType, licenseType;
							for (CandidateModel c : persons) {
								CandidateProfileViewModel tm = new CandidateProfileViewModel();

								id = c.getId();
								userID = c.getUserId();
								firstName = c.getFirstName();
								lastName = c.getLastName();
								jobType = c.getJobType();
								student = c.getStudent();
								prefCountry = c.getPrefCountry();
								prefLocation = c.getPrefLocation();
								candidateLocation = c.getCandidateLocation();
								domesticLocation = c.getDomesticLocation();
								overseasLocation = c.getOverseasLocation();
								jobCategory = c.getJobCategory();
								dateOfBirth = c.getDateOfBirth();
								gender = c.getGender();
								age = c.getAge();
								currentCountry = c.getCurrentCountry();
								currentState = c.getCurrentState();
								currentCity = c.getCurrentCity();
								perCountry = c.getPerCountry();
								perState = c.getPerState();
								perCity = c.getPerCity();
								mobileNumber = c.getMobileNumber();
								whatsappNumber = c.getWhatsappNumber();
								emailId = c.getEmailId();
								qualification = c.getQualification();
								certificationCourses = c.getCertificationCourses();
								certificationSpecialization = c.getCertificationSpecialization();
								specification = c.getSpecification();
								candidateType = c.getCandidateType();
								experience = c.getExperience();
								experienceMonths = c.getExpMonths();
								overseasExp = c.getOverseasExp();
								overseasExpMonths = c.getOverseasExpMonths();
								expCertificate = c.getExpCertificate();
								certificateType = c.getCertificateType();
								license = c.getLicense();
								licenseType = c.getLicenseType();
								paymentStatus = c.getPaymentStatus();
								skills = c.getSkills();
								skillVideoType = c.getSkillVideoType();

								List<CanLanguageModel> details1 = canLanguagesRepository.findByCandidateId(id);
								if (!details1.isEmpty()) {

									List<LanguagesModel> persons11 = null;
									Set<Integer> listt = new HashSet();

									int j1 = 0;

									for (CanLanguageModel s : details1) {

										j1 = s.getLanguageId();
										listt.add(j1);
									}

									persons11 = em
											.createQuery("SELECT j.languages FROM LanguagesModel j WHERE j.id IN :ids")
											.setParameter("ids", listt).getResultList();

									tm.setLanguages(persons11);
								}

								List<JobApplicationModel> appTime = null;

								appTime = em.createQuery(
										"select j from JobApplicationModel j where j.jobId = :jobId and j.candidateId = :candidateId and j.status = :status")
										.setParameter("jobId", jobId).setParameter("status", status)
										.setParameter("candidateId", c.getId()).getResultList();

								if (!appTime.isEmpty()) {
									for (JobApplicationModel c1 : appTime) {
										Date appliedTime = c1.getAppliedTime();
										tm.setAppliedTime(appliedTime);
									}
								}

								List<UserModel> user = userRepository.findUserId(id);

								for (UserModel u : user) {

									profilePic = u.getProfilePic();

									tm.setId(id);
									tm.setUserId(userID);
									tm.setFirstName(firstName);
									tm.setLastName(lastName);
									tm.setJobType(jobType);
									tm.setStudent(student);
									tm.setAge(age);
									tm.setPrefCountry(prefCountry);
									tm.setPrefLocation(prefLocation);
									tm.setCandidateLocation(candidateLocation);
									tm.setDomesticLocation(domesticLocation);
									tm.setOverseasLocation(overseasLocation);
									tm.setJobCategory(jobCategory);
									tm.setDateOfBirth(dateOfBirth);
									tm.setGender(gender);
									tm.setCurrentCountry(currentCountry);
									tm.setCurrentState(currentState);
									tm.setCurrentCity(currentCity);
									tm.setPerCountry(perCountry);
									tm.setPerState(perState);
									tm.setPerCity(perCity);
									tm.setMobileNumber(mobileNumber);
									tm.setWhatsappNumber(whatsappNumber);
									tm.setEmailId(emailId);
									tm.setQualification(qualification);
									tm.setCertificationCourses(certificationCourses);
									tm.setCertificationSpecialization(certificationSpecialization);
									tm.setSpecification(specification);
									tm.setCandidateType(candidateType);
									tm.setProfilePic(profilePic);
									if (candidateCallRepository.getCallDetails(tm.getId(), employerId, jobId)
											.isPresent()) {
										tm.setCallTime(candidateCallRepository
												.getCallDetails(tm.getId(), employerId, jobId).get().getCallTime());
									}
									tm.setExperience(experience);
									tm.setExperienceMonths(experienceMonths);
									tm.setOverseasExp(overseasExp);
									tm.setOverseasExpMonths(overseasExpMonths);
									List<String> expcer = null;
									List<String> expcerType = null;
									List<String> licen = null;
									List<String> licenType = null;
									if (expCertificate != null && !expCertificate.isEmpty()) {
										expcer = Arrays.asList(expCertificate.split("\\s*,\\s*"));
									}
									if (certificateType != null && !certificateType.isEmpty()) {
										expcerType = Arrays.asList(certificateType.split("\\s*,\\s*"));
									}
									if (license != null && !license.isEmpty()) {
										licen = Arrays.asList(license.split("\\s*,\\s*"));
									}
									if (licenseType != null && !licenseType.isEmpty()) {
										licenType = Arrays.asList(licenseType.split("\\s*,\\s*"));
									}
									tm.setExpCertificate(expcer);
									tm.setExpCertificateType(expcerType);
									tm.setLicense(licen);
									tm.setLicenseType(licenType);
									tm.setPaymentStatus(paymentStatus);
									tm.setSkills(skills);
									tm.setSkillVideoType(skillVideoType);

									sListt.add(tm);
								}

							}

							Collections.reverse(sListt);

							HashMap<String, Object> map = new HashMap<>();
							map.put("status", "success");
							map.put("message", "success");
							map.put("code", 200);
							map.put("data", sListt);
							return new ResponseEntity<>(map, HttpStatus.OK);

						} else {
							HashMap<String, Object> map = new HashMap<>();
							map.put("code", 400);
							map.put("message", "Candidates not found");
							return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
						}
					}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Candidates Not Found");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Applied Candidates Not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Related Jobs");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@GetMapping("/ClosedJobs")
	public ResponseEntity<?> getEmployerClosedJobs(@RequestParam("employer_id") final int employerId) {

		List<JobsModel> jobs = jobRepository.findEmployerClosedJobs(employerId);
		int count = jobs.size();

		if (!jobs.isEmpty()) {
			Collections.reverse(jobs);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", jobs);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Jobs Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(path = "/interestedCandidateUpdate")
	public ResponseEntity<?> setInterestedJobs(@RequestParam("employer_id") final int employerId,
			@RequestParam("candidate_id") final int candidateId, @RequestParam("job_id") final int jobId,
			@RequestParam("status") final String status) {

		EmployerApplication details = employerApplicationRepository.findByEmployerIdAndStatus(employerId, candidateId,
				jobId);

		try {
			if (details != null) {
				details.setStatus(status);
				employerApplicationRepository.save(details);

				/*
				 * if (status.equalsIgnoreCase("L")) {
				 * 
				 * Optional<EmployerModel> empModel = employerRepository.findById(employerId);
				 * Optional<JobsModel> jobsModel = jobRepository.findById(jobId);
				 * Optional<CandidateModel> candidateModel =
				 * candidateRepository.findById(candidateId); CandidateModel can =
				 * candidateModel.get();
				 * 
				 * EmployerActivityModel EA = new EmployerActivityModel();
				 * EA.setEmpId(employerId); EA.setActivity(can.getFirstName() +
				 * can.getJobCategory() + " interview has been scheduled!");
				 * empActivityRepository.save(EA);
				 */

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "Successfully saved");
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				EmployerApplication e = new EmployerApplication();

				e.setEmployerId(employerId);
				e.setCandidateId(candidateId);
				e.setJobId(jobId);
				e.setStatus(status);
				employerApplicationRepository.save(e);

				/*
				 * if (status.equalsIgnoreCase("L")) {
				 * 
				 * Optional<EmployerModel> empModel = employerRepository.findById(employerId);
				 * Optional<JobsModel> jobsModel = jobRepository.findById(jobId);
				 * Optional<CandidateModel> candidateModel =
				 * candidateRepository.findById(candidateId);
				 * 
				 */

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "Successfully saved");
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} catch (Exception e) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("status", "failure");
			map.put("message", "Already Updated");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/shorlistedCandidateUpdate")
	public ResponseEntity<?> setInterestedCandidate(@RequestParam("employer_id") final int employerId,
			@RequestParam("candidate_id") final int candidateId, @RequestParam("job_id") final int jobId,
			@RequestParam("status") final String status) {

		Optional<EmployerApplication> details = employerApplicationRepository.findByEmployerIdAndJobId(employerId,
				candidateId, jobId);

		if (details.isPresent()) {
			employerApplicationService.updateStatus(employerId, candidateId, jobId, status);

			if (status.equalsIgnoreCase("L")) {

				Optional<EmployerModel> empModel = employerRepository.findById(employerId);
				Optional<JobsModel> jobsModel = jobRepository.findById(jobId);
				Optional<CandidateModel> candidateModel = candidateRepository.findById(candidateId);

			} else {
				Optional<EmployerModel> empModel = employerRepository.findById(employerId);
				Optional<JobsModel> jobsModel = jobRepository.findById(jobId);
				Optional<CandidateModel> candidateModel = candidateRepository.findById(candidateId);

			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "successfully updated");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("status", "failure");
			map.put("message", "Data Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping(path = "/interestedCandidate")
	public ResponseEntity<?> getAppliedCandidates(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("status") final String status) {

		if (employerId != 0) {

			EmployerApplication details = employerApplicationRepository.findTopByEmployerId(employerId);
			if (details != null) {
				List<EmployerApplication> persons1 = null;

				persons1 = em.createQuery(
						"select j from EmployerApplication j where j.employerId = :employerId and j.jobId = :jobId and j.status = :status")
						.setParameter("employerId", employerId).setParameter("jobId", jobId)
						.setParameter("status", status).getResultList();

				if (!persons1.isEmpty()) {

					List<CandidateModel> persons = null;
					List<Integer> list = new ArrayList();

					int j = 0;

					for (EmployerApplication s : persons1) {

						j = s.getCandidateId();
						list.add(j);
					}

					persons = em.createQuery("SELECT j FROM CandidateModel j WHERE j.id IN :ids")
							.setParameter("ids", list).getResultList();

					if (!persons.isEmpty()) {
						List<CandidateProfileViewModel> sListt = new ArrayList<CandidateProfileViewModel>();
						;

						int id;
						int userID;
						String firstName, lastName, jobType, student, prefCountry, prefLocation, candidateLocation,
								domesticLocation, overseasLocation, jobCategory, dateOfBirth, gender;
						String profilePic, currentCountry, currentState, currentCity, perCountry, perState, perCity,
								emailId, qualification, specification, candidateType;
						long mobileNumber;
						long whatsappNumber;
						Integer experience;
						Integer overseasExp;
						Integer overseasExpMonths;
						String expCertificate;
						String license;
						String paymentStatus;
						String skills;
						String certificationCourses;
						String certificationSpecialization;
						Integer experienceMonths;
						String age;
						String skillVideoType;
						String certificateType, licenseType;
						for (CandidateModel c : persons) {
							CandidateProfileViewModel tm = new CandidateProfileViewModel();

							id = c.getId();
							userID = c.getUserId();
							firstName = c.getFirstName();
							lastName = c.getLastName();
							jobType = c.getJobType();
							student = c.getStudent();
							prefCountry = c.getPrefCountry();
							prefLocation = c.getPrefLocation();
							candidateLocation = c.getCandidateLocation();
							domesticLocation = c.getDomesticLocation();
							overseasLocation = c.getOverseasLocation();
							jobCategory = c.getJobCategory();
							dateOfBirth = c.getDateOfBirth();
							gender = c.getGender();
							age = c.getAge();
							currentCountry = c.getCurrentCountry();
							currentState = c.getCurrentState();
							currentCity = c.getCurrentCity();
							perCountry = c.getPerCountry();
							perState = c.getPerState();
							perCity = c.getPerCity();
							mobileNumber = c.getMobileNumber();
							whatsappNumber = c.getWhatsappNumber();
							emailId = c.getEmailId();
							qualification = c.getQualification();
							certificationCourses = c.getCertificationCourses();
							certificationSpecialization = c.getCertificationSpecialization();
							specification = c.getSpecification();
							candidateType = c.getCandidateType();
							experience = c.getExperience();
							experienceMonths = c.getExpMonths();
							overseasExp = c.getOverseasExp();
							overseasExpMonths = c.getOverseasExpMonths();
							expCertificate = c.getExpCertificate();
							certificateType = c.getCertificateType();
							license = c.getLicense();
							licenseType = c.getLicenseType();
							paymentStatus = c.getPaymentStatus();
							skills = c.getSkills();
							skillVideoType = c.getSkillVideoType();
							// createdTime = c.getCreatedTime();
							// updatedTime = c.getUpdatedTime();

							List<CanLanguageModel> details1 = canLanguagesRepository.findByCandidateId(id);
							if (!details1.isEmpty()) {

								List<LanguagesModel> persons11 = null;
								Set<Integer> listt = new HashSet();

								int j1 = 0;

								for (CanLanguageModel s : details1) {

									j1 = s.getLanguageId();
									listt.add(j1);
								}

								persons11 = em
										.createQuery("SELECT j.languages FROM LanguagesModel j WHERE j.id IN :ids")
										.setParameter("ids", listt).getResultList();

								tm.setLanguages(persons11);
							}

							List<JobApplicationModel> appTime = null;

							String appliedStatus = "Applied";

							appTime = em.createQuery(
									"select j from JobApplicationModel j where j.jobId = :jobId and j.candidateId = :candidateId and j.status = :status")
									.setParameter("jobId", jobId).setParameter("status", appliedStatus)
									.setParameter("candidateId", c.getId()).getResultList();

							if (!appTime.isEmpty()) {
								for (JobApplicationModel c1 : appTime) {
									Date appliedTime = c1.getAppliedTime();
									tm.setAppliedTime(appliedTime);
								}
							}

							List<UserModel> user = userRepository.findUserId(id);

							for (UserModel u : user) {

								profilePic = u.getProfilePic();

								tm.setId(id);
								tm.setUserId(userID);
								tm.setFirstName(firstName);
								tm.setLastName(lastName);
								tm.setJobType(jobType);
								tm.setStudent(student);
								tm.setAge(age);
								tm.setPrefCountry(prefCountry);
								tm.setPrefLocation(prefLocation);
								tm.setCandidateLocation(candidateLocation);
								tm.setDomesticLocation(domesticLocation);
								tm.setOverseasLocation(overseasLocation);
								tm.setJobCategory(jobCategory);
								tm.setDateOfBirth(dateOfBirth);
								tm.setGender(gender);
								tm.setCurrentCountry(currentCountry);
								tm.setCurrentState(currentState);
								tm.setCurrentCity(currentCity);
								tm.setPerCountry(perCountry);
								tm.setPerState(perState);
								tm.setPerCity(perCity);
								tm.setMobileNumber(mobileNumber);
								tm.setWhatsappNumber(whatsappNumber);
								tm.setEmailId(emailId);
								tm.setQualification(qualification);
								tm.setCertificationCourses(certificationCourses);
								tm.setCertificationSpecialization(certificationSpecialization);
								tm.setSpecification(specification);
								tm.setCandidateType(candidateType);
								tm.setProfilePic(profilePic);

								tm.setExperience(experience);
								tm.setExperienceMonths(experienceMonths);
								tm.setOverseasExp(overseasExp);
								tm.setOverseasExpMonths(overseasExpMonths);
								List<String> expcer = null;
								List<String> expcerType = null;
								List<String> licen = null;
								List<String> licenType = null;
								if (expCertificate != null && !expCertificate.isEmpty()) {
									expcer = Arrays.asList(expCertificate.split("\\s*,\\s*"));
								}
								if (certificateType != null && !certificateType.isEmpty()) {
									expcerType = Arrays.asList(certificateType.split("\\s*,\\s*"));
								}
								if (license != null && !license.isEmpty()) {
									licen = Arrays.asList(license.split("\\s*,\\s*"));
								}
								if (licenseType != null && !licenseType.isEmpty()) {
									licenType = Arrays.asList(licenseType.split("\\s*,\\s*"));
								}

								tm.setExpCertificate(expcer);
								tm.setExpCertificateType(expcerType);
								tm.setLicense(licen);
								tm.setLicenseType(licenType);
								tm.setPaymentStatus(paymentStatus);
								tm.setSkills(skills);
								tm.setSkillVideoType(skillVideoType);

								sListt.add(tm);
							}

						}

						Collections.reverse(sListt);

						HashMap<String, Object> map = new HashMap<>();
						map.put("status", "success");
						map.put("message", "success");
						map.put("code", 200);
						map.put("data", sListt);
						return new ResponseEntity<>(map, HttpStatus.OK);

					}

				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("status", "failure");
					map.put("message", "Candidates not found");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("status", "failure");
				map.put("message", "Employer Not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 400);
		map.put("status", "failure");
		map.put("message", "Employer id is Not Found");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

	}

	@PutMapping(path = "/closeJob")
	public ResponseEntity<?> closeJob(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("expiry_date") final String expiryDate,
			@RequestParam(value = "reason", required = false) final String reason) {

		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);
		if (jobs != null) {
			Optional<JobsModel> optional = jobRepository.findById(jobId);
			JobsModel existing = optional.get();
			existing.setExpiryDate(expiryDate);
			existing.setJobStatus("C");
			existing.setReasonForClose(reason);
			jobRepository.save(existing);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(employerId);
			EA.setActivity("Your job " + "<b>" + existing.getJobCategory() + "</b>" + " has been closed!");
			empActivityRepository.save(EA);

			Optional<EmployerModel> emp = employerRepository.findById(employerId);

			EmployerModel em = emp.get();

			/*
			 * String Edate = em.getExpiryDate();
			 * 
			 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			 * sdf.setLenient(false); Date expiry; try { expiry = sdf.parse(Edate); String
			 * cDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			 * 
			 * Date curDate = sdf.parse(cDate);
			 * 
			 * boolean expired = expiry.before(curDate);
			 * 
			 * if (expired != true) { int jobCount = em.getPlanJobCount(); int newCount =
			 * jobCount + 1; em.setPlanJobCount(newCount); employerRepository.save(em); } }
			 * catch (ParseException e1) { // TODO Auto-generated catch block
			 * e1.printStackTrace(); }
			 */

			Map<String, String> kycEmailData = new HashMap<String, String>();
			kycEmailData.put("name", em.getContactPersonName());
			kycEmailData.put("jobName", existing.getJobCategory());

			/*
			 * TupleStore tupleStore = new TupleStore(); tupleStore.setKey(em.getEmailId());
			 * tupleStore.setValue(new Gson().toJson(kycEmailData));
			 * amazonSESMailUtil.sendEmailSES("EmployerJobClosedTemplateV1", tupleStore);
			 */

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "successfully saved");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("status", "failure");
			map.put("message", "Job Or Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@SuppressWarnings("unchecked")
	@GetMapping(path = "/jobStatusCount")
	public ResponseEntity<?> jobStatusCount(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId) {
		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);
		if (jobs != null) {
			String saveStatus = "S";
			String shortStatus = "L";
			String appliedStatus = "I";

			List<JobApplicationModel> persons1 = null;
			List<EmployerApplication> saved = null;
			List<EmployerApplication> shortLis = null;

			int savedCount = 0;
			int shortCount = 0;
			int appliedCount = 0;

			List<JobApplicationModel> details = jobApplicationRepository.findByJobId(jobId);

			if (!details.isEmpty()) {

				persons1 = em
						.createQuery(
								"select j from JobApplicationModel j where j.jobId = :jobId and j.status = :status")
						.setParameter("jobId", jobId).setParameter("status", appliedStatus).getResultList();

				appliedCount = persons1.size();
			}

			EmployerApplication details1 = employerApplicationRepository.findTopByEmployerId(employerId);
			if (details1 != null) {

				saved = em.createQuery(
						"select j from EmployerApplication j where j.employerId = :employerId and j.jobId = :jobId and j.status = :status")
						.setParameter("employerId", employerId).setParameter("jobId", jobId)
						.setParameter("status", saveStatus).getResultList();

				shortLis = em.createQuery(
						"select j from EmployerApplication j where j.employerId = :employerId and j.jobId = :jobId and j.status = :status")
						.setParameter("employerId", employerId).setParameter("jobId", jobId)
						.setParameter("status", shortStatus).getResultList();

				savedCount = saved.size();
				shortCount = shortLis.size();

			}
			
			Page<CandidateCallModel> employerCallModel = candidateCallRepository.getCanCallNotificationCount(jobId,
					PageRequest.of(0, 10000));

			EmpJobStatusCountModel count = new EmpJobStatusCountModel();
			count.setAppliedCount(appliedCount);
			count.setSavedCount(savedCount);
			count.setShortListedCount(shortCount);
			count.setCallCount(employerCallModel.getTotalElements()); 

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", count);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Or Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/jobCloseReasons")
	public ResponseEntity<?> getjobCloseReasons() {

		List<JobCloseReasonModel> details = jobCloseReasonRepository.findAll();

		if (!details.isEmpty()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Close Reasons Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/addJobVideo")
	public ResponseEntity<?> addJobVideo(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestPart MultipartFile video) throws IOException {

		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);
		if (jobs != null) {
			Optional<JobsModel> optional = jobRepository.findById(jobId);
			JobsModel existing = optional.get();

			String videoUrl = existing.getJobVideo();

			if (videoUrl != null && !videoUrl.isEmpty()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Already Video Added");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			} else {
				EmployerModel em = employerRepository.findById(employerId).get();


				if (video != null && !video.isEmpty()) {

					String jobVideo = jobService.uploadVideo(video, employerId, video.getBytes());
					if (jobVideo != null && !jobVideo.isEmpty()) {
						existing.setJobVideo(jobVideo);
						jobRepository.save(existing);

						try {
							adminService.uploadJobVideoFileToS3Bucket(video,
									EMPLOYERJOBVIDEOPREFIX + String.valueOf(jobId) + ".mp4");

						} catch (Exception e) {
							logger.error("error [" + e.getMessage() + "] occurred while uploading [" + video
									+ "] job video");
							return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
						}

						EmployerActivityModel EA = new EmployerActivityModel();
						EA.setEmpId(employerId);
						EA.setActivity("<b>JD Video</b> added to your job " + jobs.getJobCategory() + " successfully!");
						empActivityRepository.save(EA);

						DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
						String date = formatter.format(new Date());
						SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
						Calendar cal = Calendar.getInstance();
						String time = simpleDateFormat1.format(cal.getTime());


						HashMap<String, Object> map = new HashMap<>();
						map.put("code", 200);
						map.put("status", "success");
						map.put("message", "successfully saved");
						return new ResponseEntity<>(map, HttpStatus.OK);
					} else {
						EmployerActivityModel EA = new EmployerActivityModel();
						EA.setEmpId(employerId);
						EA.setActivity("<b>JD Video upload</b> for your job " + jobs.getJobCategory() + " failed!");
						empActivityRepository.save(EA);
						HashMap<String, Object> map = new HashMap<>();
						map.put("code", 400);
						map.put("message", "Video Not Saved");
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

					}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Video Field is required");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Or Employer Not found");

			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@GetMapping("/getCandidateVideo")
	public ResponseEntity<?> getJobById(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("job_id") final int jobId) {

		Optional<JobApplicationModel> appl = jobApplicationRepository.findByCandidateIdAndJobId(candidateId, jobId);

		if (appl.isPresent()) {

			JobApplicationModel j = appl.get();

			String videoUrl = j.getJobVideo();

			if (videoUrl != null && !videoUrl.isEmpty()) {

				EmpCandidateVideoModel model = new EmpCandidateVideoModel();
				model.setVideoURL(videoUrl);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("message", "success");
				map.put("status", "success");
				map.put("data", model);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Video is not present");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Details Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/getJobSalaryRange")
	public ResponseEntity<?> getJobSalaryRange() {

		List<CfgEmpJobSalaryModel> details = cfgEmpJobSalaryRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Salary Range Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/getJobPersonalization")
	public ResponseEntity<?> getJobPersonalization() {

		List<EmployerJobPersonalizationModel> details = employerJobPersonalizationRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Personalization Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobDescVideos")
	public ResponseEntity<?> getJobDescVideos() {

		List<JobDescVideosModel> details = jobDescVideosRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Videos Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobShiftTimings")
	public ResponseEntity<?> getJobShiftTimings(@RequestParam("shift_type") final String shiftType) {

		List<CfgEmpJobShiftTimings> details = empJobShiftTimingsRepository.findByShiftType(shiftType);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Shift timings not available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/empCandidateStatus")
	public ResponseEntity<?> empcandidateStatus(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("candidate_id") final int candidateId) {
		EmployerApplication details = employerApplicationRepository.findByEmployerIdAndStatus(employerId, candidateId,
				jobId);

		if (details != null) {
			Optional<JobsModel> optional = jobRepository.findById(jobId);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Candidate status is empty");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping(path = "/empJobCategories")
	public ResponseEntity<?> empJobCategories(@RequestParam("employer_id") final int employerId,
			@RequestParam(value = "job_category", required = false) final String jobRole) {
		EmployerModel em = employerRepository.findById(employerId).get();

		if (em != null) {
			if(jobRole!=null && !jobRole.isEmpty()) {
				JobsModel detail = jobRepository.getJobByJobCategory(employerId, jobRole);
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "success");
				map.put("data", detail);
				return new ResponseEntity<>(map, HttpStatus.OK);
				
			}else {
			List<Map<String, Object>> details = jobRepository.getEmpJobCategories(employerId);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
			}


		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping(path = "/empFilterJobCategories")
	public ResponseEntity<?> empJobCategories(@RequestParam("emp_id") final int employerId) {
		EmployerModel em = employerRepository.findById(employerId).get();
		List<Map<String, Object>> details = jobRepository.getEmpMostJobCategories(employerId);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

}
