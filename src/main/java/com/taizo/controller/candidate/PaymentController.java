package com.taizo.controller.candidate;

import java.net.URISyntaxException; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Convert;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgFullTimeGroup;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobApplicationModel;
import com.taizo.model.JobsModel;
import com.taizo.model.PlansModel;
import com.taizo.model.UserPaymentModel;
import com.taizo.model.UserPlanModel;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.FullTimeGroupingRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.UserPaymentRepository;
import com.taizo.service.CandidateService;

@CrossOrigin
@RestController
public class PaymentController {

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	CandidateService candidateService;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;
	@Autowired
	IndustryRepository industryRepository;
	@Autowired
	JobRepository jobRepository;

	@Autowired
	UserPaymentRepository userPaymentRepository;

	@PersistenceContext
	EntityManager em;

	@PutMapping(path = "/updatePaymentStatus")
	public ResponseEntity<?> updatePaymentDetails(@RequestParam("user_id") final int userId,
			@RequestParam("payment_status") final String paymentStatus) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", candidateService.updatePaymentStatus(userId, paymentStatus));
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/paymentDetails")
	public ResponseEntity<?> setSelectedPlan(@RequestParam("user_id") final int userId,
			@RequestParam("payment_id") final int paymentId) {

		UserPaymentModel pay = userPaymentRepository.findById(paymentId);

		if (pay == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Payment details not found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", pay);
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@GetMapping(path = "/getPaymentStatus")
	public ResponseEntity<Object> getPaymentStatus(@RequestParam("user_id") final int userId) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			boolean paidStatus = false;
			int jobCount = getCanMatchedJobs(details.getCity(), details.getId());
			if (jobCount >= 5) {
				int amnt = details.getAmount() + details.getDiscountAmount();
				if (amnt > 9) {
					paidStatus = true;
				}
			} else {
				int amnt = details.getAmount() + details.getDiscountAmount();
				if (amnt < 10) {
					details.setDiscountAmount(10);
					candidateRepository.save(details);
				}
				paidStatus = true;
			}
			List<UserPlanModel> array = new ArrayList<>();
			UserPlanModel plan = new UserPlanModel();
			plan.setAmount(50);
			plan.setApplyLimit(5);
			plan.setFree(0);
			array.add(plan);
			UserPlanModel plan1 = new UserPlanModel();
			plan1.setAmount(100);
			plan1.setApplyLimit(10);
			plan1.setFree(0);
			array.add(plan1);

			HashMap<String, Object> st = new HashMap<>();
			st.put("PaidStatus", paidStatus);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", st);
			map.put("plans", array);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/getNewPaymentStatus")
	public ResponseEntity<Object> getNewPaymentStatus(@RequestParam("user_id") final int userId) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			boolean paidStatus = false;
			int jobCount = getCanMatchedJobs(details.getCity(), details.getId());
			if (jobCount >= 5) {
				int amnt = details.getAmount() + details.getDiscountAmount();
				if (amnt > 1) {
					paidStatus = true;
				}
			} else {
				if (details.isUsedFreeTrial()) {
					int amnt = details.getAmount() + details.getDiscountAmount();
					if (amnt < 2) {
						details.setDiscountAmount(2);
						candidateRepository.save(details);
					}
					paidStatus = true;
				}
			}
			List<UserPlanModel> array = new ArrayList<>();

			if (details.isUsedFreeTrial()) {
				UserPlanModel plan1 = new UserPlanModel();
				plan1.setAmount(10);
				plan1.setApplyLimit(5);
				plan1.setFreetrial(false);
				array.add(plan1);
				UserPlanModel plan2 = new UserPlanModel();
				plan2.setAmount(20);
				plan2.setApplyLimit(10);
				plan2.setFreetrial(false);
				array.add(plan2);

			} else {
				UserPlanModel plan = new UserPlanModel();
				plan.setAmount(0);
				plan.setApplyLimit(4);
				plan.setFreetrial(true);
				array.add(plan);
				UserPlanModel plan1 = new UserPlanModel();
				plan1.setAmount(10);
				plan1.setApplyLimit(5);
				plan1.setFreetrial(false);
				array.add(plan1);
			}

			HashMap<String, Object> st = new HashMap<>();
			st.put("PaidStatus", paidStatus);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", st);
			map.put("plans", array);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	public int getCanMatchedJobs(String city, int canId) {

		Page<JobsModel> results = null;
		List<String> cityList = null;
		int length = 10, start = 0;

		Optional<CandidateModel> canDetails = candidateRepository.findById(canId);

		if (city != null && !city.isEmpty()) {
			String[] elements = city.split(",");
			cityList = Arrays.asList(elements);
		}

		int page = start / 1; // Calculate page number

		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "jobPostedTime"));

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
				String industry = canDetails.get().getIndustry();

				int exp = canDetails.get().getExperience();
				String cities = canDetails.get().getCity();

				if (cities != null && !cities.isEmpty()) {
					String[] elements = cities.split(",");
					cityList = Arrays.asList(elements);
				}

				results = jobRepository.findCanAllExperiencedJobs(canlist, exp, jobRole, industry, cityList, pageable);

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
					canlist.addAll(g);

					relList = jobRepository.findNewExperiencedRelatedJobs(roleList, canlist, cityList, exp, pageable);

					finalList.addAll(relList);

					results = new PageImpl<>(finalList);

				}

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
		return results.getContent().size();

	}

	public List<Integer> getValuesForGivenKey(List<JobsModel> matchedresults, String key) {
		JSONArray jsonArray = new JSONArray(matchedresults);
		return IntStream.range(0, jsonArray.length()).mapToObj(index -> ((JSONObject) jsonArray.get(index)).optInt(key))
				.collect(Collectors.toList());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@GetMapping(path = "/walletDetails")
	public ResponseEntity<?> getWalletDetails(@RequestParam("user_id") final int userId) {

		CandidateModel c = candidateRepository.finduser(userId);
		if (c != null) {

			int candidateId = c.getId();
			String status = "I";
			int balance = 0;
			int applyLimit = 0;
			int appliedJobs = 0;
			List<String> jobAppl = null;

			balance = c.getAmount();
			applyLimit = balance / 10;

			jobAppl = em.createQuery(
					"select e from JobApplicationModel e where e.candidateId = :candidateId and e.status = :status")
					.setParameter("candidateId", candidateId).setParameter("status", status).getResultList();

			appliedJobs = jobAppl.size();

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("balance", balance);
			map.put("applyLimit", applyLimit);
			map.put("appliedJobs", appliedJobs);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/selectFreeTrial")
	public ResponseEntity<?> selectFreeTrial(@RequestParam("user_id") final int userId) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			int limit = 2;
			if (details.getCandidateType().equalsIgnoreCase("Fresher")) {
				limit = 1;
			}
			details.setDiscountAmount(4);
			details.setJobLimit(limit);
			details.setUsedFreeTrial(true);

			candidateRepository.save(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

}
