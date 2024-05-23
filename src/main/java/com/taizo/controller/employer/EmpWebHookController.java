package com.taizo.controller.employer;

import java.io.IOException; 
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.ads.Lead;

import com.taizo.model.*;
import com.taizo.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.restfb.FacebookClient;
import com.taizo.controller.candidate.CandidateController;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.service.CandidateAnalyticsService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.WAAlertService;

@CrossOrigin
@RestController
public class EmpWebHookController {
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	CfgCanAdminAreaRepository cfgCanAdminAreaRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	WAAlertService waAlertService;
	
	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	FacebookMetaLeadRepository facebookMetaLeadRepository;

	@Autowired
	CanLeadRepository canLeadRepository;

	@Autowired
	CfgFullTimeJobRolesRepository cfgFullTimeJobRolesRepository;

	@Autowired
	AdminJobRolesMappingRepository adminJobRolesMappingRepository;
	
	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	CfgCanAdminCityGroupingRepository cfgCanAdminCityGroupingRepository;
	
	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;
	
	@Autowired
	CandidateAnalyticsService candidateAnalyticsService;
	
	@Autowired
	AdminCallNotiRepository adminCallNotiRepository;

	@Value("${property.baseNumber.url}")
	private String baseNumber;
	private static final Logger log = LogManager.getLogger(EmpWebHookController.class);


	@RequestMapping(value = "/fbMeta-leads", method = RequestMethod.GET)
	public ResponseEntity<HashMap<String, Object>> facebookLeadAd(
	        @RequestParam(value = "mobile_number", required = false) String mobileNumber,
	        @RequestParam(value = "whatsapp_number", required = false) String whatsappNumber,
	        @RequestParam(value = "form_id", required = false) String formId,
	        @RequestParam(value = "name", required = false) String name,
	        @RequestParam(value = "education_qualification", required = false) String educationQualification,
	        @RequestParam(value = "job_category", required = false) String jobCategory,
	        @RequestParam(value = "experience", required = false) String experience,
	        @RequestParam(value = "preferred_location", required = false) String preferredLocation,
	        @RequestParam(value = "area", required = false) String area,
	        @RequestParam(value = "platform", required = false) String platform) {

	    try {
	        jobCategory = jobCategory.toLowerCase().replace("_", " ");

	        if (preferredLocation != null) {
	            // Convert to lowercase and replace underscores with spaces
	            preferredLocation = preferredLocation.toLowerCase().replace("_", " ");

	            // Find the next adminId for the specified preferredLocation and prevAdminId
	            int nextActiveAdminId = findNextAdminId(preferredLocation);

	            // Ensure mobileNumber is not null and has at least 10 characters
	            if (mobileNumber != null && mobileNumber.length() >= 10) {
	                // Extract the last 10 digits of the mobile number
	                mobileNumber = mobileNumber.substring(mobileNumber.length() - 10);

	                FacebookMetaLead fb = facebookMetaLeadRepository.findByMobileNumber(mobileNumber);
	                boolean active =fb.isInActive();
                    CanLeadModel canLead = canLeadRepository.findByMobileNumber(Long.parseLong(mobileNumber));
                    CandidateModel candidate = candidateRepository.findByMobileNumber(Long.parseLong(mobileNumber));

	                if (fb==null && canLead==null && candidate==null) {
	                    // Save to FacebookMetaLead table
	                    FacebookMetaLead facebookMetaLead = new FacebookMetaLead();
	                    facebookMetaLead.setCandidateName(name);
	                    facebookMetaLead.setMobileNumber(mobileNumber);
	                    facebookMetaLead.setWhatsappNumber(whatsappNumber);
	                    facebookMetaLead.setEducationQualification(educationQualification);
	                    facebookMetaLead.setJobCategory(jobCategory);
	                    facebookMetaLead.setPreferredLocation(preferredLocation);
	                    facebookMetaLead.setExperience(experience);
	                    facebookMetaLead.setResourcePlatform(platform);
	                    facebookMetaLead.setArea(area);

	                    // Set AssignTo based on the retrieved adminId
	                    facebookMetaLead.setAssignTo(nextActiveAdminId);

	                    facebookMetaLead.setFormId(formId);

	                    // Save the FacebookMetaLead object to the database
	                    facebookMetaLeadRepository.save(facebookMetaLead);

	                    // Increment lead counts
	                    candidateAnalyticsService.fbMetaLeadcount(Long.valueOf(nextActiveAdminId), LocalDate.now());
	                    candidateAnalyticsService.TotalLeadscount(Long.valueOf(nextActiveAdminId), LocalDate.now());

	                    // Create and save CandidateTimeLine
	                    CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
	                    LocalDate currentDate1 = LocalDate.now();
	                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	                    String formattedDate = currentDate1.format(formatter);
	                    String eventDescription = "FbMetaLead Generation" + " On " + formattedDate;
	                    candidateTimeLine.setCanId(0);
	                    candidateTimeLine.setFacebookId(facebookMetaLead.getId());
	                    candidateTimeLine.setCanLeadId(0);
	                    candidateTimeLine.setEventName("FbMetaLead generation");
	                    candidateTimeLine.setEventDescription(eventDescription);
	                    candidateTimeLineRepository.save(candidateTimeLine);

	                    // Create and save AdminCallNotiModel
	                    AdminCallNotiModel ac = new AdminCallNotiModel();
	                    ac.setEventName("Jobseeker Alert");
	                    ac.setType("Jobseeker");
	                    ac.setEventType("Meta Jobseeker Register");
	                    ac.setSource("fb");
	                    ac.setMobileNumber(mobileNumber);
	                    ac.setIdType("Jobseeker ID");
	                    ac.setReferenceId(Math.toIntExact(facebookMetaLead.getId()));
	                    ac.setAdminId(facebookMetaLead.getAssignTo());
	                    ac.setJobRole(jobCategory);
	                    ac.setCandidateName(name);
	                    adminCallNotiRepository.save(ac);
	                } else if ((fb!=null && active==true) && canLead!=null || candidate!=null) {
		                    // Save to FacebookMetaLead table
		                    FacebookMetaLead facebookMetaLead = new FacebookMetaLead();
		                    facebookMetaLead.setCandidateName(name);
		                    facebookMetaLead.setMobileNumber(mobileNumber);
		                    facebookMetaLead.setWhatsappNumber(whatsappNumber);
		                    facebookMetaLead.setEducationQualification(educationQualification);
		                    facebookMetaLead.setJobCategory(jobCategory);
		                    facebookMetaLead.setPreferredLocation(preferredLocation);
		                    facebookMetaLead.setExperience(experience);
		                    facebookMetaLead.setResourcePlatform(platform);
		                    facebookMetaLead.setArea(area);

		                    // Set AssignTo based on the retrieved adminId
		                    facebookMetaLead.setAssignTo(nextActiveAdminId);

		                    facebookMetaLead.setFormId(formId);
		                    
		                    if (canLead!=null) {
		                        // Retrieve CanLeadModel
		                        CanLeadModel canLeads = canLeadRepository.findByMobileNumber(Long.parseLong(mobileNumber));
		                        // Update FacebookMetaLead with CanLeadModel details
		                        facebookMetaLead.setCanLeadId(canLeads.getId());
		                        facebookMetaLead.setCandidateId(0);
		                        facebookMetaLead.setIsCandidate(false);
		                        facebookMetaLead.setIsCanLead(true);
		                    } else if (candidate!=null) {
		                        // Retrieve CandidateModel
		                        CandidateModel candidateModel = candidateRepository.findByMobileNumber(Long.parseLong(mobileNumber));
		                        // Update FacebookMetaLead with CandidateModel details
		                        facebookMetaLead.setCandidateId(candidateModel.getId());
		                        facebookMetaLead.setCanLeadId(0);
		                        facebookMetaLead.setIsCanLead(false);
		                        facebookMetaLead.setIsCandidate(true);
		                    }

		                    // Save the FacebookMetaLead object to the database
		                    facebookMetaLeadRepository.save(facebookMetaLead);

		                    // Increment lead counts
		                    candidateAnalyticsService.fbMetaLeadcount(Long.valueOf(nextActiveAdminId), LocalDate.now());
		                    candidateAnalyticsService.TotalLeadscount(Long.valueOf(nextActiveAdminId), LocalDate.now());

		                    // Create and save CandidateTimeLine
		                    CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
		                    LocalDate currentDate1 = LocalDate.now();
		                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		                    String formattedDate = currentDate1.format(formatter);
		                    String eventDescription = "FbMetaLead Generation" + " On " + formattedDate;
		                    candidateTimeLine.setCanId(0);
		                    candidateTimeLine.setFacebookId(facebookMetaLead.getId());
		                    candidateTimeLine.setCanLeadId(0);
		                    candidateTimeLine.setEventName("FbMetaLead generation");
		                    candidateTimeLine.setEventDescription(eventDescription);
		                    candidateTimeLineRepository.save(candidateTimeLine);

		                    // Create and save AdminCallNotiModel
		                    AdminCallNotiModel ac = new AdminCallNotiModel();
		                    ac.setEventName("Jobseeker Alert");
		                    ac.setType("Jobseeker");
		                    ac.setEventType("Meta Jobseeker Register");
		                    ac.setSource("fb");
		                    ac.setMobileNumber(mobileNumber);
		                    ac.setIdType("Jobseeker ID");
		                    ac.setReferenceId(Math.toIntExact(facebookMetaLead.getId()));
		                    ac.setAdminId(facebookMetaLead.getAssignTo());
		                    ac.setJobRole(jobCategory);
		                    ac.setCandidateName(name);
		                    adminCallNotiRepository.save(ac);

	                    // Construct response map
	                    HashMap<String, Object> map = new HashMap<>();
	                    map.put("statuscode", 200);
	                    map.put("message", "success");
	                    map.put("fbResult", fb);  // Changed key to avoid overwriting
	                    map.put("canLeadResult", canLead);  // Changed key to avoid overwriting
	                    map.put("candidateResult", candidate);  // Changed key to avoid overwriting

	                    // Return response entity with map and HTTP status OK
	                    return new ResponseEntity<>(map, HttpStatus.OK);
	                }
	                }
	            }
	        
	    } catch (Exception e) {
	        handleException(e);
	    }
	    // Return null if an exception occurs or if none of the conditions are met
	    return null;
	}




	private int findNextAdminId(String preferredLocation) {
		// Get the most recent FacebookMetaLead entry for the specified preferredLocation
	    Optional<FacebookMetaLead> latestEntryOptional = facebookMetaLeadRepository.findFirstByPreferredLocationOrderByCreatedTimeDesc(preferredLocation);
	    int prevAdminId;
	    if (latestEntryOptional.isPresent()) {
	        // If an entry is found, extract the assignTo (adminId) from the latest entry
	        prevAdminId = latestEntryOptional.get().getAssignTo();
	    } else {
	        // If no entry is found, set a default value or handle accordingly
	        prevAdminId = 0;
	    }
	    // Find the next adminId for the specified preferredLocation and prevAdminId
	    CfgCanAdminCityGrouping cfgCanAdminCityGrouping = findNextActiveAdmin(preferredLocation, prevAdminId);
	    if (cfgCanAdminCityGrouping != null) {
	        return cfgCanAdminCityGrouping.getAdminId();
	    } else {
	        // Handle the case where there is no next active adminId for the specified preferredLocation
	        // Restart the loop from the first active adminId for that city
	        return findFirstActiveAdminForCity(preferredLocation);
	    }
	}

	private CfgCanAdminCityGrouping findNextActiveAdmin(String preferredLocation, int prevAdminId) {
	    return cfgCanAdminCityGroupingRepository.findFirstByCityNameAndAdminIdGreaterThanAndActiveOrderByAdminIdAsc(
	            preferredLocation, prevAdminId, true);
	}

	private int findFirstActiveAdminForCity(String preferredLocation) {
	    CfgCanAdminCityGrouping firstAdminForCity = cfgCanAdminCityGroupingRepository.findFirstByCityNameAndActiveOrderByAdminIdAsc(preferredLocation, true);
	    if (firstAdminForCity != null) {
	        return firstAdminForCity.getAdminId();
	    } else {
	        // Handle the case where there is no active adminId for the specified preferredLocation
	        return 1;
	    }
	}


	    private boolean checkIfExistsInCanLeadModel(Long mobileNumber) {
	        CanLeadModel canLeadModel = canLeadRepository.findByMobileNumber(mobileNumber);
	        return canLeadModel != null;
	    }

	    private boolean checkIfExistsInCandidateModel(Long mobileNumber) {
	        CandidateModel candidateModel = candidateRepository.findByMobileNumber(mobileNumber);
	        return candidateModel != null;
	    }

	    private boolean checkIfExistsInFacebookMetaLead(String mobileNumber) {
	        return facebookMetaLeadRepository.existsByMobileNumber(mobileNumber);
	    }
	    
	@RequestMapping(value = "/fbMeta-lead", method = RequestMethod.GET)
	public void facebookLeadAd(@RequestParam(value = "mobile_number", required = false) String mobileNumber,
	                           @RequestParam(value = "whatsapp_number", required = false) String whatsappNumber,
	                           @RequestParam(value = "form_id", required = false) String formId,
	                           @RequestParam(value = "name", required = false) String name,
	                           @RequestParam(value = "education_qualification", required = false) String educationQualification,
	                           @RequestParam(value = "job_category", required = false) String jobCategory,
	                           @RequestParam(value = "experience", required = false) String experience,
	                           @RequestParam(value = "preferred_location", required = false) String preferredLocation,
	                           @RequestParam(value = "area", required = false) JSONArray areaJsonArray,
	                           @RequestParam(value = "platform", required = false) String platform) {

	    try {
	        // Ensure jobCategory is not null
	        if (jobCategory != null) {
	            jobCategory = jobCategory.toLowerCase().replace("_", " ");
	        }

	        // Define a list of possible area keys
	        List<String> possibleAreaKeys = Arrays.asList(
	                "choose_a_area_in_\"coimbatore\"_that_is_comfortable_for_you_to_join_work",
	                "choose_a_area_in_\"chennai\"_that_is_comfortable_for_you_to_join_workd",
	                "choose_a_area_in_\"kancheepuram\"_that_is_comfortable_for_you_to_join_work",
	                "choose_a_area_in_\"chengalpattu\"_that_is_comfortable_for_you_to_join_work",
	                "choose_a_area_in_\"tiruvallur\"_that_is_comfortable_for_you_to_join_work",
	                "choose_a_area_in_\"hosur\"_that_is_comfortable_for_you_to_join_work"
	               
	        );

	        // Extract "area" value from the JSONArray
	        String area = extractAreaFromJsonArray(areaJsonArray, possibleAreaKeys);

	        // Ensure area is not null
	        if (area != null) {
	            area = area.toLowerCase().replace("_", " ");
	        }
	        if (preferredLocation != null) {
	            // Convert to lowercase and replace underscores with spaces
	            preferredLocation = preferredLocation.toLowerCase().replace("_", " ");

	            // Find the next adminId for the specified preferredLocation and prevAdminId
	            int nextAdminId = findNextAdminId(preferredLocation);
	        }


	        // Ensure mobileNumber is not null and has at least 10 characters
	        if (mobileNumber != null && mobileNumber.length() >= 10) {
	            // Extract the last 10 digits of the mobile number
	            mobileNumber = mobileNumber.substring(mobileNumber.length() - 10);
	        }

	        // Create FacebookMetaLead object
	        FacebookMetaLead facebookMetaLead = new FacebookMetaLead();
	        facebookMetaLead.setCandidateName(name);
	        facebookMetaLead.setMobileNumber(mobileNumber);
	        facebookMetaLead.setWhatsappNumber(whatsappNumber);
	        facebookMetaLead.setEducationQualification(educationQualification);
	        facebookMetaLead.setJobCategory(jobCategory);
	        facebookMetaLead.setPreferredLocation(preferredLocation);
	        facebookMetaLead.setExperience(experience);
	        facebookMetaLead.setResourcePlatform(platform);
	        facebookMetaLead.setArea(area);
	        facebookMetaLead.setFormId(formId);

	        // Find and set the assignTo based on area
	        setAssignToBasedOnArea(facebookMetaLead);

	        // Save FacebookMetaLead
	        facebookMetaLeadRepository.save(facebookMetaLead);

	        // Send meta alert
	        sendMetaAlert(whatsappNumber);

	    } catch (Exception e) {
	        handleException(e);
	    }
	}
	
	private String extractAreaFromJsonArray(JSONArray jsonArray, List<String> possibleAreaKeys) {
	    if (jsonArray != null && jsonArray.length() > 0 && possibleAreaKeys != null && !possibleAreaKeys.isEmpty()) {
	        for (int i = 0; i < jsonArray.length(); i++) {
	            JSONObject jsonObject = jsonArray.getJSONObject(i);
	            String name = jsonObject.optString("name");

	            // Check if the name corresponds to any of the possible area keys
	            if (name != null && possibleAreaKeys.contains(name)) {
	                JSONArray valuesArray = jsonObject.optJSONArray("values");
	                if (valuesArray != null && valuesArray.length() > 0) {
	                    return valuesArray.optString(0);
	                }
	            }
	        }
	    }
	    return null;
	}
	

	    // Method to set the "assignTo" based on the area
	    private void setAssignToBasedOnArea(FacebookMetaLead facebookMetaLead) {
	        String area = facebookMetaLead.getArea();
	        CfgCanAdminArea cfgCanAdminArea = (area != null) ? cfgCanAdminAreaRepository.findByAreas(area) : null;

	        if (cfgCanAdminArea != null) {
	            int adminId = findAdminIdByAssingnedToAdminId(cfgCanAdminArea.getAssingnedToAdminId());
	            facebookMetaLead.setAssignTo(Math.toIntExact(adminId));
	        } else {
	            facebookMetaLead.setAssignTo(1);
	        }
	    }

	
	


	// Helper method to append non-null strings to the StringBuilder
	private void appendIfNotNull(StringBuilder builder, String value) {
	    if (value != null) {
	        builder.append(value).append(" ");
	    }
	}

	// Helper method to transform area names
	private String transformArea(String area) {
	    if (area != null) {
	        return area.replace("_", " ");
	    }
	    return null;
	}


	private int findAdminIdByAssingnedToAdminId(int assingnedToAdminId) {
		// TODO Auto-generated method stub
		List<CfgCanAdminArea> adminJobRolesMapping =cfgCanAdminAreaRepository.findByAssingnedToAdminId(assingnedToAdminId);
		if (adminJobRolesMapping != null && !adminJobRolesMapping.isEmpty()) {
			return adminJobRolesMapping.get(0).getAssingnedToAdminId();
		} else {
			return 0;
		}
	}

	private Long findAdminIdByGroupId(Integer groupId) {
		List<AdminJobRolesMapping> adminJobRolesMapping = adminJobRolesMappingRepository.findByGroupId(groupId);
		if (adminJobRolesMapping != null && !adminJobRolesMapping.isEmpty()) {
			return adminJobRolesMapping.get(0).getAdminId();
		} else {
			return null;
		}
	}

	private boolean isCallAllowed() {
		LocalTime currentTime = LocalTime.now();
		LocalTime startTime = LocalTime.of(9, 30);
		LocalTime endTime = LocalTime.of(19, 0);
		return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
	}

	public void sendMetaAlert(String whatsappNumber) {
	    try {
	        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
	        LocalTime currentTime = LocalTime.now(zoneId);

	        // Define the start time and end time for not sending alerts
	        LocalTime startTime = LocalTime.of(9, 0);
	        LocalTime endTime = LocalTime.of(18, 30);

	        // Check if the current time is outside the specified range
	        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
	            // Implement logic to send WhatsApp alert
	            HashMap<String, String> wa = new HashMap<>();
	            wa.put("mn", "91" + whatsappNumber);
	            waAlertService.sendMetaAlerts(wa);
	        } else {
	        	HashMap<String, String> wa = new HashMap<>();
	            wa.put("mn", "91" + whatsappNumber);
	            waAlertService.sendMetaAlerts(wa);
	        }

	    } catch (Exception e) {
	        handleException(e);
	    }
	}



	private void handleException(Exception e) {
		e.printStackTrace();
		System.out.println(e.getMessage());
	}



	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		out.println("<html><title>Hello endpoint!</title>" + "<body bgcolor=FFFFFF>");

		out.println("<h2>myendpoint</h2>");

		out.println("<p>Hi! I'm alive. Thanks for asking.</p><p>Returned from doGet</p></body></html>");
		out.close();
		
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SecurityException {
		// Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		// If message doesn't have the message type header, don't process it.

		if (messagetype == null) {
			return;
		}

		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		Message msg = readMessageFromJson(builder.toString());

		// The signature is based on SignatureVersion 1.
		if (msg.getSignatureVersion().equals("1")) {

			if (isMessageSignatureValid(msg)) {
				log.info(">>Signature verification succeeded");

			} else {
				log.info(">>Signature verification failed");
				CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
				logEventModel.setType("SNS");
				logEventModel.setMessage("failure");
				logEventModel.setDescription("Signature verification failed");

				try {
					cloudWatchLogService.cloudLogFailure(logEventModel, "E");
				} catch (Exception e) {

				}
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			log.info(">>Unexpected signature version. Unable to verify signature.");
			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("SNS");
			logEventModel.setMessage("failure");
			logEventModel.setDescription("Unexpected signature version. Unable to verify signature.");

			try {
				cloudWatchLogService.cloudLogFailure(logEventModel, "E");
			} catch (Exception e) {

			}
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
//			cloudWatchLogService.cloudLog(logEventModel, "Inside Notification Block");
			

			String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
			if (msg.getSubject() != null) {
				logMsgAndSubject += " Subject: " + msg.getSubject();
			}
			logMsgAndSubject += " Message: " + msg.getMessage();
			
			String message = msg.getMessage();

			saveVideo(message);

			log.info(logMsgAndSubject);
		} else if (messagetype.equals("SubscriptionConfirmation")) {

			Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}

			log.info(">>Subscription confirmation (" + msg.getSubscribeURL() + ") Return value: " + sb.toString());
			// Process the return value to ensure the endpoint is subscribed.
		} else if (messagetype.equals("UnsubscribeConfirmation")) {

			log.info(">>Unsubscribe confirmation: " + msg.getMessage());
		} else {
			// Handle unknown message type.
			log.info(">>Unknown message type.");
		}
		
		log.info(">>Done processing message: " + msg.getMessageId());
	}

	private  Message readMessageFromJson(String json) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("readMessageFromJson");
		
		Message m = new Message();

		JsonFactory f = new JsonFactory();
		JsonParser jp = f.createParser(json);

		jp.nextToken();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String name = jp.getCurrentName();
			jp.nextToken();
			if ("Type".equals(name))
				m.setType(jp.getText());
			else if ("Message".equals(name))
				m.setMessage(jp.getText());
			else if ("MessageId".equals(name))
				m.setMessageId(jp.getText());
			else if ("SubscribeURL".equals(name))
				m.setSubscribeURL(jp.getText());
			else if ("UnsubscribeURL".equals(name))
				m.setUnsubscribeURL(jp.getText());
			else if ("Subject".equals(name))
				m.setSubject(jp.getText());
			else if ("Timestamp".equals(name))
				m.setTimestamp(jp.getText());
			else if ("TopicArn".equals(name))
				m.setTopicArn(jp.getText());
			else if ("Token".equals(name))
				m.setToken(jp.getText());
			else if ("Signature".equals(name))
				m.setSignature(jp.getText());
			else if ("SignatureVersion".equals(name))
				m.setSignatureVersion(jp.getText());
			else if ("SigningCertURL".equals(name))
				m.setSigningCertURL(jp.getText());

		}
		
		System.out.println("readMessageFromJson-End");

		return m;
	}

	private boolean isMessageSignatureValid(Message msg) {

		try {
			URL url = new URL(msg.getSigningCertURL());

	        try {
	            TimeUnit.SECONDS.sleep(10);
				verifyMessageSignatureURL(msg, url);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	 

			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}

	private void verifyMessageSignatureURL(Message msg, URL endpoint) {
		URI certUri = URI.create(msg.getSigningCertURL());

		if (!"https".equals(certUri.getScheme())) {
			throw new SecurityException("SigningCertURL was not using HTTPS: " + certUri.toString());
		}

		/*
		 * if (!endpoint.equals(certUri.getHost())) { CloudwatchLogEventModel log = new
		 * CloudwatchLogEventModel(); log.setType("SNS"); log.setMessage("success");
		 * log.setDescription("endpoint Called"+ endpoint + certUri.toString());
		 * 
		 * try { cloudWatchLogService.cloudLog(log, "E"); } catch (Exception e) {
		 * 
		 * } throw new SecurityException(String.format(
		 * "SigningCertUrl does not match expected endpoint. " +
		 * "Expected %s but received endpoint was %s.", endpoint, certUri.getHost()));
		 * 
		 * }
		 */
	}

	private static byte[] getMessageBytesToSign(Message msg) {
		byte[] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	public static String buildNotificationStringToSign(Message msg) {
		String stringToSign = null;

		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	public static String buildSubscriptionStringToSign(Message msg) {
		String stringToSign = null;

		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}
	
	public void saveVideo(String message) {
 
		JSONObject json = new JSONObject(message);  
		String url = json.getJSONObject("Outputs").getJSONArray("HLS_GROUP").getString(0); 
		String jobId = url.substring(url.lastIndexOf("_")+1, url.lastIndexOf("."));  
		
		CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
		logEventModel.setType("SNS");
		logEventModel.setMessage("success");
		logEventModel.setDescription(
				"Job video uploaded (" + url + ") ");

		try {
			cloudWatchLogService.cloudLog(logEventModel, "E");
		} catch (Exception e) {

		}
		
		JobsModel existing = jobRepository.findById(Integer.parseInt(jobId)).get();
		
		if (url != null && !url.isEmpty()) {
			existing.setJobVideo(url);
			jobRepository.save(existing);
		}

	}

}
