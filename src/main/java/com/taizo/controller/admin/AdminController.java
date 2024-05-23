package com.taizo.controller.admin;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.Multipart;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.taizo.DTO.AdminDTO;
import com.taizo.DTO.PrivilegeDTO;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	SalesPageRepository salesPageRepository;
	
	@Autowired
	AdminService adminService;
	
	@Autowired
	AdminRolesMappingRepository adminRolesMappingRepository;
	
	@Autowired
	AdminRolesPrevilegeMappingRepository adminRolesPrevilegeMappingRepository;
	
	@Autowired
	CfgAdminPrevilegeRepository cfgAdminPrevilegeRepository;
	
	@Autowired
	CfgAdminRolesRepository cfgAdminRolesRepository;
	
	 @Autowired
	 private PasswordEncoder passwordEncoder;
	 
	 @Autowired
	 AdminAnalyticsRepository adminAnalyticsRepository;
	 
	 @Autowired
	 AdminCallRegistryRepository adminCallRegistryRepository;
	 
	 @Autowired
	 CfgCanAdminCityGroupingRepository cfgCanAdminCityGroupingRepository;
	
	 @GetMapping("/loginDetails")
	 public ResponseEntity<Map<String, Object>> login(@RequestParam String emailId, @RequestParam String password) {
	     try {
	         Admin admin = adminService.findAdminByEmail(emailId);

	         if (admin != null) {
	     
	             String hashedPassword = admin.getPassword();

	             BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	         
	             if (passwordEncoder.matches(password, hashedPassword) || password.equals(hashedPassword)) {
	                 // Passwords match
	                 Map<String, Object> response = new HashMap<>();
	                 response.put("statuscode", 200);
	                 response.put("message", "Login successful");

	                 Map<String, Object> adminDetails = new HashMap<>();
	                 adminDetails.put("id", admin.getId());
	                 adminDetails.put("userName", admin.getUserName());
	                 adminDetails.put("mobileNo", admin.getMobileNo());
	                 adminDetails.put("emailId", admin.getEmailId());
	                 adminDetails.put("module", admin.getModule());
	                 adminDetails.put("isAvailable", admin.isAvailable());
	                 adminDetails.put("ProfilePic", admin.getProfilePic());
	                 response.put("admin", adminDetails);
	                 
	                 List<CfgAdminRolesModel> roles = adminService.getAdminRoles(admin.getId());
                     List<Map<String, Object>> rolesResponse = new ArrayList<>();

                     for (CfgAdminRolesModel role : roles) {
                         Map<String, Object> roleResponse = new HashMap<>();
                         roleResponse.put("roleId", role.getId());
                         roleResponse.put("roleName", role.getRoleName());

                         List<AdminRolesPrevilegeMappingModel> privileges = adminService.getPrivilegesByRoleId(role.getId());
                         List<Map<String, Object>> privilegesResponse = new ArrayList<>();

                         for (AdminRolesPrevilegeMappingModel privilege : privileges) {
                             Map<String, Object> privilegeResponse = new HashMap<>();
                             privilegeResponse.put("id", privilege.getPrivilegeId().getId());
                             privilegeResponse.put("privilegeId", privilege.getPrivilegeId().getPrivilegeId());
                             privilegeResponse.put("privilegeName", privilege.getPrivilegeId().getPrivilegeName());
                             privilegeResponse.put("create", privilege.isCreate());
                             privilegeResponse.put("read", privilege.isRead());
                             privilegeResponse.put("update", privilege.isUpdate());
                             privilegeResponse.put("delete", privilege.isDelete());
                             privilegesResponse.add(privilegeResponse);
                         }

                         roleResponse.put("privileges", privilegesResponse);
                         rolesResponse.add(roleResponse);
                     }

                     response.put("roles", rolesResponse);

                     return ResponseEntity.ok(response);

	             } else {
	                 // Passwords don't match
	                 Map<String, Object> errorResponse = new HashMap<>();
	                 errorResponse.put("statuscode", 400);
	                 errorResponse.put("message", "Login failed: Incorrect password");
	                 return ResponseEntity.badRequest().body(errorResponse);
	             }
	         } else {
	             // Admin not found
	             Map<String, Object> errorResponse = new HashMap<>();
	             errorResponse.put("statuscode", 400);
	             errorResponse.put("message", "Login failed: Admin not found");
	             return ResponseEntity.badRequest().body(errorResponse);
	         }
	     } catch (RuntimeException e) {
	         // Handle exceptions
	         Map<String, Object> errorResponse = new HashMap<>();
	         errorResponse.put("statuscode", 400);
	         errorResponse.put("message", "Login failed: " + e.getMessage());
	         return ResponseEntity.badRequest().body(errorResponse);
	     }
	 }

	
	@PostMapping(path = "/adminDetails")
	public ResponseEntity<?> adminLogin(@RequestParam("email_id") final String emailId,
			@RequestParam("password") final String password) {

		Admin adminExists = adminRepository.findByEmailId(emailId);
		if (adminExists != null) {
			Optional<Admin> check = adminRepository.adminlogin(emailId, password);

			if (check.isPresent()) {
				Admin a = check.get();
				// Remove the password from the response for security reasons
				a.setPassword("");

				// Prepare the response data
				Map<String, Object> data = new HashMap<>();
				data.put("admin_id", a.getId());
				data.put("admin_data", a);

				// Prepare the response map
				Map<String, Object> responseMap = new HashMap<>();
				responseMap.put("statuscode", 200);
				responseMap.put("message", "Login Successfully");
				responseMap.put("data", data);

				return new ResponseEntity<>(responseMap, HttpStatus.OK);
			} else {
				Map<String, Object> responseMap = new HashMap<>();
				responseMap.put("statuscode", 400);
				responseMap.put("message", "Login Failure");
				return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
			}

		} else {
			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("statuscode", 400);
			responseMap.put("message", "Email id not found");
			return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
		}

	}
	@GetMapping("/getPrivileges")
	public ResponseEntity<Map<String, Object>> getPrivileges() {
	    try {
	        List<CfgAdminPrevilegeModel> privileges = adminService.getAllPrivileges();
	        int privilegeCount = privileges.size();
	        List<Map<String, Object>> privilegesResponse = new ArrayList<>();
	        for (CfgAdminPrevilegeModel privilege : privileges) {
	            Map<String, Object> privilegeResponse = new HashMap<>();
	            privilegeResponse.put("id",privilege.getId());
	            privilegeResponse.put("privilegeId", privilege.getPrivilegeId());
	            privilegeResponse.put("privilegeName", privilege.getPrivilegeName());
	            privilegeResponse.put("description", privilege.getDescription());
	            privilegesResponse.add(privilegeResponse);
	        }
	        Map<String, Object> response = new HashMap<>();
	        response.put("statuscode", 200);
	        response.put("privilegeCount", privilegeCount);
	        response.put("privileges", privilegesResponse);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("statuscode", 400);
	        errorResponse.put("message", "Error retrieving privileges: " + e.getMessage());
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}
	
	@GetMapping("/detailsById")
	public ResponseEntity<Map<String, Object>> login(@RequestParam Long adminId) {
	    try {
	        // Fetch admin based on adminId
	        Admin admin = adminRepository.findById(adminId).get();

	        if (admin != null) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("statuscode", 200);
	            response.put("message", "success");

	            // Admin details
	            Map<String, Object> adminDetails = new HashMap<>();
	            adminDetails.put("id", admin.getId());
	            adminDetails.put("userName", admin.getUserName());
	            adminDetails.put("module", admin.getModule());
	            adminDetails.put("profilePic", admin.getProfilePic());
	            adminDetails.put("emailId", admin.getEmailId());
	            adminDetails.put("password", admin.getPassword());
	            adminDetails.put("mobileNo", admin.getMobileNo());
	            adminDetails.put("mobileCountryCode", admin.getMobileCountryCode());
	            adminDetails.put("isAvailable", admin.isAvailable());
	            adminDetails.put("introVideoUrl", admin.getIntroVideoUrl());
	            adminDetails.put("introGifUrl", admin.getIntroGifUrl());
	            adminDetails.put("emailSignature", admin.getEmailSignature());
	            adminDetails.put("createdOn", admin.getCreatedOn());
	            adminDetails.put("is_deactivate", admin.isDeactivate());
	         
	            response.put("admin", adminDetails);

	            // Admin roles and privileges
	            List<CfgAdminRolesModel> roles = adminService.getAdminRoles(admin.getId());
	            List<Map<String, Object>> rolesResponse = new ArrayList<>();

	            for (CfgAdminRolesModel role : roles) {
	                Map<String, Object> roleResponse = new HashMap<>();
	                roleResponse.put("roleId", role.getId());
	                roleResponse.put("roleName", role.getRoleName());

	                List<AdminRolesPrevilegeMappingModel> privileges = adminService.getPrivilegesByRoleId(role.getId());
	                List<Map<String, Object>> privilegesResponse = new ArrayList<>();

	                for (AdminRolesPrevilegeMappingModel privilege : privileges) {
	                    Map<String, Object> privilegeResponse = new HashMap<>();
	                    privilegeResponse.put("id", privilege.getPrivilegeId().getId());
	                    privilegeResponse.put("privilegeId", privilege.getPrivilegeId().getPrivilegeId());
	                    privilegeResponse.put("privilegeName", privilege.getPrivilegeId().getPrivilegeName());
	                    privilegeResponse.put("create", privilege.isCreate());
	                    privilegeResponse.put("read", privilege.isRead());
	                    privilegeResponse.put("update", privilege.isUpdate());
	                    privilegeResponse.put("delete", privilege.isDelete());
	                    privilegesResponse.add(privilegeResponse);
	                }

	                roleResponse.put("privileges", privilegesResponse);
	                rolesResponse.add(roleResponse);
	            }

	            response.put("roles", rolesResponse);

	            // Add the response for the privilege table
	            List<CfgAdminPrevilegeModel> allPrivileges = adminService.getAllPrivileges();
	            List<Map<String, Object>> allPrivilegesResponse = new ArrayList<>();

	            for (CfgAdminPrevilegeModel privilege : allPrivileges) {
	                Map<String, Object> privilegeResponse = new HashMap<>();
	                privilegeResponse.put("privilegeId", privilege.getPrivilegeId());
	                privilegeResponse.put("privilegeName", privilege.getPrivilegeName());
	                allPrivilegesResponse.add(privilegeResponse);
	            }

	            // response.put("allPrivileges", allPrivilegesResponse);

	            return ResponseEntity.ok(response);
	        } else {
	            Map<String, Object> errorResponse = new HashMap<>();
	            errorResponse.put("statuscode", 400);
	            errorResponse.put("message", "Login failed: Admin not found");
	            return ResponseEntity.badRequest().body(errorResponse);
	        }
	    } catch (RuntimeException e) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("statuscode", 400);
	        errorResponse.put("message", "Login failed: " + e.getMessage());
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}
	
	@PostMapping("/addUser")
	public ResponseEntity<Map<String, Object>> addAdmin(@RequestBody AdminDTO adminDTO) {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        // Create and save the Admin entity
	        Admin admin = new Admin();
	        admin.setUserName(adminDTO.getUserName());
	        admin.setEmailId(adminDTO.getEmailId());
	        String encodedPassword = passwordEncoder.encode(adminDTO.getPassword());
	        admin.setPassword(encodedPassword);
	        admin.setMobileNo(adminDTO.getMobileNo());
	        admin.setMobileCountryCode("91");
	        admin.setModule(adminDTO.getModule());
	        admin = adminRepository.save(admin);

	        // Fetch the role entity from the database using roleId
	        CfgAdminRolesModel role = cfgAdminRolesRepository.findById(adminDTO.getRoleId())
	                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + adminDTO.getRoleId()));

	        AdminRolesMappingModel roles = new AdminRolesMappingModel();
	        roles.setAdmin(admin);
	        roles.setRoleId(role);
	        adminRolesMappingRepository.save(roles);
	        
	        // Create and save privilege mappings
	        for (PrivilegeDTO privilegeDTO : adminDTO.getPrivileges()) {
	            Long privilegeId = privilegeDTO.getPrivilegeId();
	            Boolean isCreate = privilegeDTO.isCreate();
	            Boolean isRead = privilegeDTO.isRead();
	            Boolean isUpdate = privilegeDTO.isUpdate();
	            Boolean isDelete = privilegeDTO.isDelete();

	            // Fetch the privilege entity from the database using privilegeId
	            CfgAdminPrevilegeModel privilege = cfgAdminPrevilegeRepository.findById(privilegeId)
	                    .orElseThrow(() -> new EntityNotFoundException("Privilege not found with ID: " + privilegeId));

	            AdminRolesPrevilegeMappingModel privilegeMapping = new AdminRolesPrevilegeMappingModel();
	            privilegeMapping.setAdmin(admin);
	            privilegeMapping.setRoleId(role);
	            privilegeMapping.setPrivilegeId(privilege);
	            privilegeMapping.setCreate(isCreate);
	            privilegeMapping.setRead(isRead);
	            privilegeMapping.setUpdate(isUpdate);
	            privilegeMapping.setDelete(isDelete);
	            adminRolesPrevilegeMappingRepository.save(privilegeMapping);
	        }

	        response.put("statuscode", 200);
	        response.put("message", "Admin added successfully.");
	        return ResponseEntity.ok(response);

	    } catch (EntityNotFoundException roleNotFoundEx) {
	        response.put("statuscode", 400);
	        response.put("message", roleNotFoundEx.getMessage());
	        return ResponseEntity.badRequest().body(response);
	    } catch (Exception e) {
	        response.put("statuscode", 500);
	        response.put("message", "Internal Server Error: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	    @GetMapping("/adminRoles")
	    public List<CfgAdminRolesModel> getAllRoles() {
	        // Use the JpaRepository to fetch all roles from the database
	        return cfgAdminRolesRepository.findAll();
	    }
	    
	    @GetMapping("/findAdmin")
	    public ResponseEntity<?> checkAdminExistence(
	        @RequestParam(name = "mobileNo", required = false) String mobileNo,
	        @RequestParam(name = "emailId", required = false) String emailId
	    ) {
	        Map<String, Object> response = new HashMap<>();

	        Admin existingAdminByMobileNo = adminRepository.findByMobileNo(mobileNo);
	        Admin existingAdminByEmailId = adminRepository.findByEmailId(emailId);

	        if (existingAdminByMobileNo != null) {
	            response.put("code", "400");
	            response.put("message", "Mobile number already exists.");
	            return ResponseEntity.ok(response);
	        }

	        if (existingAdminByEmailId != null) {
	            response.put("code", "400");
	            response.put("message", "Email ID already exists.");
	            return ResponseEntity.ok(response);
	        }

	        response.put("code", "200");
	        response.put("message", "new User");
	        return ResponseEntity.ok(response);
	    }
	    
	    @PutMapping("/adminAvailability")
	    public ResponseEntity<Map<String, Object>> checkAdminAvailability(
	            @RequestParam("emailId") String emailId,
	            @RequestParam("isAvailable") boolean isAvailable) {

	        Admin existingAdmin = adminRepository.findByEmailId(emailId);

	        Map<String, Object> response = new HashMap<>();

	        if (existingAdmin != null) {
	            Long adminId = existingAdmin.getId();
	            int adminIdInt = adminId.intValue();

	            existingAdmin.setAvailable(isAvailable);
	            adminRepository.save(existingAdmin);

	            List<CfgCanAdminCityGrouping> cfgCanAdminCityGroupingList = cfgCanAdminCityGroupingRepository.findByAdminId(adminIdInt);

	            if (!cfgCanAdminCityGroupingList.isEmpty()) {
	                try {
	                    for (CfgCanAdminCityGrouping cfg : cfgCanAdminCityGroupingList) {
	                        cfg.setActive(isAvailable);
	                    }
	                    // Save the updated entities back to the database
	                    cfgCanAdminCityGroupingRepository.saveAll(cfgCanAdminCityGroupingList);

	                    response.put("status", "success");
	                    response.put("message", "Active status updated successfully");
	                    return ResponseEntity.ok(response);
	                } catch (Exception e) {
	                	response.put("code", "400");
	                    response.put("status", "error");
	                    response.put("message", "Failed to update active status");
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	                }
	            } else {
	            	response.put("code", "400");
	                response.put("status", "error");
	                response.put("message", "No records found for adminId: " + adminId);
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            }
	        } else {
	            response.put("code", "400");
	            response.put("message", "Admin not found");
	            return ResponseEntity.badRequest().body(response);
	        }
	    }


	    @GetMapping("/checkAdminAvailability")
	    public ResponseEntity<Map<String, Object>> checkAdminAvailability(@RequestParam("emailId") String emailId) {
	    	
	        Admin existingAdmin = adminRepository.findByEmailIdAndIsAvailable(emailId, true);
	        
	        Map<String, Object> response = new HashMap<>();
	        
	        if (existingAdmin != null) {
	        	response.put("code", "200");
	        	response.put("message", "Admin is available");
	            return ResponseEntity.ok(response);
	        } else {
	        	response.put("code", "400");
	        	response.put("message", "Admin is not available");
	            return ResponseEntity.ok(response);
	        }
	    }
	    
	    @GetMapping("/salesPage")
	    public ResponseEntity<Map<String, Object>> salesPage(@RequestParam int adminId){
	    	List<SalesModel> salesModel =salesPageRepository.findByAdminIdAndActive(adminId,true);
	    	
	    	 Map<String, Object> response = new HashMap<>();
		        
		        if (salesModel != null) {
		        	response.put("code", "200");
		        	response.put("message", "SalesPage is available");
		        	response.put("data", salesModel); 
		            return ResponseEntity.ok(response);
		        } else {
		        	response.put("code", "400");
		        	response.put("message", "SalesPage is not available");
		            return ResponseEntity.ok(response);
		        }
	    }
	    
	    @PostMapping("/dailyReports")
	    public ResponseEntity<Map<String, Object>> performAction(
	        @RequestBody AdminAnalyticsModel request
	    ) {
	        Map<String, Object> response = new HashMap<>();
	        
	        if (request == null || request.getAdminId() == null || request.getModule() == null) {
	            response.put("code", 400);
	            response.put("message", "Bad Request: Missing or invalid request data.");
	            return ResponseEntity.badRequest().body(response);
	        }

	        List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(request.getAdminId());



            if (!adminAnalyticsList.isEmpty()) {

                LocalDate currentDate = LocalDate.now();
                boolean dateMatch = false;

                for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
                    LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
                    if (currentDate.isEqual(createdOnDate)) {
						dateMatch = true;
						adminAnalytics.setAdminId(request.getAdminId());
						adminAnalytics.setModule(request.getModule());

						 if (request.getEmpFollowUpCount() != null) {
			                    Integer currentEmpFollowUpCount = adminAnalytics.getEmpFollowUpCount();
			                    if (currentEmpFollowUpCount == null) {
			                        currentEmpFollowUpCount = 0;
			                    }
			                    adminAnalytics.setEmpFollowUpCount(currentEmpFollowUpCount+ request.getEmpFollowUpCount());
			                }
						if (request.getEmpNoOfCalls() != null) {
							Integer currentEmpNoOfCalls = adminAnalytics.getEmpNoOfCalls();
							if (currentEmpNoOfCalls == null) {
								currentEmpNoOfCalls = 0;
							}
							adminAnalytics.setEmpNoOfCalls(currentEmpNoOfCalls+ request.getEmpNoOfCalls());
						}
						if (request.getEmpQualifiedCount() != null) {
							Integer currentEmpQualifiedCount = adminAnalytics.getEmpQualifiedCount();
							if (currentEmpQualifiedCount == null) {
								currentEmpQualifiedCount = 0;
							}
							adminAnalytics.setEmpQualifiedCount(currentEmpQualifiedCount+ request.getEmpQualifiedCount());
						}
						if (request.getEmpNotQualifiedCount() != null) {
							Integer currentEmpNotQualifiedCount = adminAnalytics.getEmpNotQualifiedCount();
							if (currentEmpNotQualifiedCount == null) {
								currentEmpNotQualifiedCount = 0;
							}
							adminAnalytics.setEmpNotQualifiedCount(currentEmpNotQualifiedCount+ request.getEmpNotQualifiedCount());
						}

						if (request.getEmpNewLeadCount() != null) {
							Integer currentEmpNewLeadCount = adminAnalytics.getEmpNewLeadCount();
							if (currentEmpNewLeadCount == null) {
								currentEmpNewLeadCount = 0;
							}
							adminAnalytics.setEmpNewLeadCount(currentEmpNewLeadCount+ request.getEmpNewLeadCount());
						}
						if (request.getEmpNewLeadCount() != null) {
							Integer currentEmpNewLeadCount = adminAnalytics.getEmpNewLeadCount();
							if (currentEmpNewLeadCount == null) {
								currentEmpNewLeadCount = 0;
							}
							adminAnalytics.setEmpNewLeadCount(currentEmpNewLeadCount+ request.getEmpNewLeadCount());
						}
						if (request.getCanTotalChatCount() != null) {
							Integer currentCanTotalChatCount = adminAnalytics.getCanTotalChatCount();
							if (currentCanTotalChatCount == null) {
								currentCanTotalChatCount = 0;
							}
							adminAnalytics.setCanTotalChatCount(currentCanTotalChatCount+ request.getCanTotalChatCount());
						}
						if (request.getCanNoOfCalls() != null) {
							Integer currentCanNoOfCalls = adminAnalytics.getCanNoOfCalls();
							if (currentCanNoOfCalls == null) {
								currentCanNoOfCalls = 0;
							}
							adminAnalytics.setCanNoOfCalls(currentCanNoOfCalls+ request.getCanNoOfCalls());
						}
						if (request.getCanQualifiedCount() != null) {
							Integer currentCanQualifiedCount = adminAnalytics.getCanQualifiedCount();
							if (currentCanQualifiedCount == null) {
								currentCanQualifiedCount = 0;
							}
							adminAnalytics.setCanQualifiedCount(currentCanQualifiedCount+ request.getCanQualifiedCount());
						}
						if (request.getCanNotQualifiedCount() != null) {
							Integer currentCanNotQualifiedCount = adminAnalytics.getCanNotQualifiedCount();
							if (currentCanNotQualifiedCount == null) {
								currentCanNotQualifiedCount = 0;
							}
							adminAnalytics.setCanNotQualifiedCount(currentCanNotQualifiedCount+ request.getCanNotQualifiedCount());
						}
						if (request.getEmpFieldNewLeadCount() != null) {
							Integer currentEmpFieldNewLeadCount = adminAnalytics.getEmpFieldNewLeadCount();
							if (currentEmpFieldNewLeadCount == null) {
								currentEmpFieldNewLeadCount = 0;
							}
							adminAnalytics.setEmpFieldNewLeadCount(currentEmpFieldNewLeadCount+ request.getEmpFieldNewLeadCount());
						}
						if (request.getEmpFieldFollowUpCount() != null) {
							Integer currentEmpFieldFollowUpCount = adminAnalytics.getEmpFieldFollowUpCount();
							if (currentEmpFieldFollowUpCount == null) {
								currentEmpFieldFollowUpCount = 0;
							}
							adminAnalytics.setEmpFieldFollowUpCount(currentEmpFieldFollowUpCount+ request.getEmpFieldFollowUpCount());
						}
						if (request.getEmpFieldFollowUpVisitCount() != null) {
							Integer currentEmpFieldFollowUpVisitCount = adminAnalytics.getEmpFieldFollowUpVisitCount();
							if (currentEmpFieldFollowUpVisitCount == null) {
								currentEmpFieldFollowUpVisitCount = 0;
							}
							adminAnalytics.setEmpFieldFollowUpVisitCount(currentEmpFieldFollowUpVisitCount+ request.getEmpFieldFollowUpVisitCount());
						}
						if (request.getEmpFieldNewVisitCount() != null) {
							Integer currentEmpFieldNewVisitCount= adminAnalytics.getEmpFieldFollowUpVisitCount();
							if (currentEmpFieldNewVisitCount == null) {
								currentEmpFieldNewVisitCount = 0;
							}
							adminAnalytics.setEmpFieldNewVisitCount(currentEmpFieldNewVisitCount+ request.getEmpFieldNewVisitCount());
						}
						if (request.getEmpFieldNoOfPaymentCount() != null) {
							Integer currentEmpFieldNoOfPaymentCount= adminAnalytics.getEmpFieldNoOfPaymentCount();
							if (currentEmpFieldNoOfPaymentCount == null) {
								currentEmpFieldNoOfPaymentCount = 0;
							}
							adminAnalytics.setEmpFieldNoOfPaymentCount(currentEmpFieldNoOfPaymentCount+ request.getEmpFieldNoOfPaymentCount());
						}
                    }
                }

                if (!dateMatch) {
          
                    // If the dates are different for all records, insert a new record
					AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();

					newAdminAnalytics.setAdminId(request.getAdminId());
					newAdminAnalytics.setModule(request.getModule());
					newAdminAnalytics.setEmpFollowUpCount(request.getEmpFollowUpCount());
					newAdminAnalytics.setEmpNoOfCalls(request.getEmpNoOfCalls());
					newAdminAnalytics.setEmpQualifiedCount(request.getEmpQualifiedCount());
					newAdminAnalytics.setEmpNotQualifiedCount(request.getEmpNotQualifiedCount());
					newAdminAnalytics.setEmpNewLeadCount(request.getEmpNewLeadCount());
					newAdminAnalytics.setEmpNoOfPayment(request.getEmpNoOfPayment());
					newAdminAnalytics.setCanTotalChatCount(request.getCanTotalChatCount());
					newAdminAnalytics.setCanNoOfCalls(request.getCanNoOfCalls());
					newAdminAnalytics.setCanQualifiedCount(request.getCanQualifiedCount());
					newAdminAnalytics.setCanNotQualifiedCount(request.getCanNotQualifiedCount());
					newAdminAnalytics.setCanInterviewScheduledCount(request.getCanInterviewScheduledCount());
					newAdminAnalytics.setCanInterviewAttendedCount(request.getCanInterviewAttendedCount());
					newAdminAnalytics.setCanInterviewJoinedCount(request.getCanInterviewJoinedCount());
					newAdminAnalytics.setCanInterviewNotSelectedCount(request.getCanInterviewNotSelectedCount());
					newAdminAnalytics.setClosedJobCount(request.getClosedJobCount());
					newAdminAnalytics.setRetentionCount(request.getRetentionCount());
					newAdminAnalytics.setEmpFieldNewLeadCount(request.getEmpFieldNewLeadCount());
					newAdminAnalytics.setEmpFieldFollowUpCount(request.getEmpFieldFollowUpCount());
					newAdminAnalytics.setEmpFieldFollowUpVisitCount(request.getEmpFieldFollowUpVisitCount());
					newAdminAnalytics.setEmpFieldNewVisitCount(request.getEmpFieldNewVisitCount());
					newAdminAnalytics.setEmpFieldNoOfPaymentCount(request.getEmpFieldNoOfPaymentCount());

					adminAnalyticsList.add(newAdminAnalytics);
                }
               
                adminAnalyticsRepository.saveAll(adminAnalyticsList);
                
                response.put("code", "200");
	        	response.put("message", "successfully");
	            return ResponseEntity.ok(response);
	            
            } else {
              
                // If there are no existing records for the adminId, insert a new record
                AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
				adminAnalytics.setAdminId(request.getAdminId());
				adminAnalytics.setModule(request.getModule());
				adminAnalytics.setEmpFollowUpCount(request.getEmpFollowUpCount());
				adminAnalytics.setEmpNoOfCalls(request.getEmpNoOfCalls());
				adminAnalytics.setEmpQualifiedCount(request.getEmpQualifiedCount());
				adminAnalytics.setEmpNotQualifiedCount(request.getEmpNotQualifiedCount());
				adminAnalytics.setEmpNewLeadCount(request.getEmpNewLeadCount());
				adminAnalytics.setEmpNoOfPayment(request.getEmpNoOfPayment());
				adminAnalytics.setCanTotalChatCount(request.getCanTotalChatCount());
				adminAnalytics.setCanNoOfCalls(request.getCanNoOfCalls());
				adminAnalytics.setCanQualifiedCount(request.getCanQualifiedCount());
				adminAnalytics.setCanNotQualifiedCount(request.getCanNotQualifiedCount());
				adminAnalytics.setCanInterviewScheduledCount(request.getCanInterviewScheduledCount());
				adminAnalytics.setCanInterviewAttendedCount(request.getCanInterviewAttendedCount());
				adminAnalytics.setCanInterviewJoinedCount(request.getCanInterviewJoinedCount());
				adminAnalytics.setCanInterviewNotSelectedCount(request.getCanInterviewNotSelectedCount());
				adminAnalytics.setClosedJobCount(request.getClosedJobCount());
				adminAnalytics.setRetentionCount(request.getRetentionCount());
				adminAnalytics.setEmpFieldNewLeadCount(request.getEmpFieldNewLeadCount());
				adminAnalytics.setEmpFieldFollowUpCount(request.getEmpFieldFollowUpCount());
				adminAnalytics.setEmpFieldFollowUpVisitCount(request.getEmpFieldFollowUpVisitCount());
				adminAnalytics.setEmpFieldNewVisitCount(request.getEmpFieldNewVisitCount());
				adminAnalytics.setEmpFieldNoOfPaymentCount(request.getEmpFieldNoOfPaymentCount());

				
                adminAnalyticsRepository.save(adminAnalytics);
            }
            response.put("code", "200");
        	response.put("message", "successfully");
	        return ResponseEntity.ok(response);
	    }

	    @PostMapping("/callRegistry")
	    public ResponseEntity<Map<String, Object>> saveAdminCallRegistry(
	            @RequestParam("admin_id") Long adminId,
	            @RequestParam(value = "candidate_id", required = false) Integer candidateId,
	            @RequestParam(value = "employer_id", required = false) Integer employerId,
	            @RequestParam("call_time") String callTime
	    ) {
	        Map<String, Object> response = new HashMap<>();

	        try {
	            // Check if adminId exists
	            Optional<Admin> adminOptional = adminRepository.findById(adminId);
	            if (!adminOptional.isPresent()) {
	                response.put("code", 404);
	                response.put("message", "Admin not found.");
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            }

	            AdminCallRegistryModel adminCallRegistry = new AdminCallRegistryModel();
	            adminCallRegistry.setAdminId(Math.toIntExact(adminId));
	            adminCallRegistry.setCandidateId(candidateId);
	            adminCallRegistry.setEmployerId(employerId);
	            adminCallRegistry.setCallTime(callTime);

	            adminCallRegistryRepository.save(adminCallRegistry);

	            List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(adminId);

	            LocalDate currentDate = LocalDate.now();
	            boolean dateMatch = false;

	            for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	                LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	                if (currentDate.isEqual(createdOnDate)) {
	                    dateMatch = true;

	                    // Initialize properties to zero if null
	                    Integer empNoOfCalls = adminAnalytics.getEmpNoOfCalls() != null ? adminAnalytics.getEmpNoOfCalls() : 0;
	                    Integer canNoOfCalls = adminAnalytics.getCanNoOfCalls() != null ? adminAnalytics.getCanNoOfCalls() : 0;

	                    if (employerId != null) {
	                        adminAnalytics.setEmpNoOfCalls(empNoOfCalls + 1);
	                    }
	                    if (candidateId != null) {
	                        adminAnalytics.setCanNoOfCalls(canNoOfCalls + 1);
	                    }
	                }
	            }

	            if (!dateMatch) {
	                String module = adminOptional.get().getModule();
	                // If the dates are different for all records, insert a new record
	                AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	                newAdminAnalytics.setAdminId(adminId);
	                newAdminAnalytics.setModule(module);
	                if (employerId != null) {
	                    newAdminAnalytics.setEmpNoOfCalls(1);
	                }
	                if (candidateId != null) {
	                    newAdminAnalytics.setCanNoOfCalls(1);
	                }
	                adminAnalyticsList.add(newAdminAnalytics);
	            }

	            adminAnalyticsRepository.saveAll(adminAnalyticsList);

	            response.put("code", 200);
	            response.put("message", "Saved successfully.");
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.put("code", 400);
	            response.put("message", "Invalid action.");
	            return ResponseEntity.badRequest().body(response);
	        }
	    }



	    @PutMapping("/profiles")
	    public ResponseEntity<Map<String, Object>> updateAdminProfilePic(
	            @RequestParam(value = "admin_id",required = false)Long adminId,
				@RequestParam(value = "user_name",required = false)String userName,
				@RequestParam(value = "mobile_number",required = false)String mobileNumber,
				@RequestParam(value = "email_id",required = false)String emailId,
	            @RequestParam(value = "pic",required = false) MultipartFile pic) {

	        try {
	            Optional<Admin> optionalAdmin = adminRepository.findById(adminId);

	            if (optionalAdmin.isPresent()) {
	                Admin existingAdmin = optionalAdmin.get();

					existingAdmin.setUserName(userName);
					existingAdmin.setEmailId(emailId);
					existingAdmin.setMobileNo(mobileNumber);
					
					   if (pic != null) {
			                String picUrl = adminService.uploadProfilePic(pic, pic.getBytes());
			                existingAdmin.setProfilePic(picUrl);
			            }
			            
	                adminRepository.save(existingAdmin);

	                
	                Map<String, Object> response = new HashMap<>();
	                response.put("code", 200);
	                response.put("message", "Profile picture updated successfully");
	                return ResponseEntity.ok(response);
	            } else {
	            	Map<String, Object> errorResponse = new HashMap<>();
		            errorResponse.put("code", 400); 
		            errorResponse.put("message", "An error occurred while processing the profile picture.");
		            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	            }
	        } catch (IOException e) {
	            
	            Map<String, Object> errorResponse = new HashMap<>();
	            errorResponse.put("code", 400); 
	            errorResponse.put("message", "An error occurred while processing the profile picture.");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	        }
	    }

	    @GetMapping("/list")
	    public List<Map<String, Object>> getAllAdmins() {
	        List<Admin> admins = adminRepository.findAll();
	        List<Map<String, Object>> result = new ArrayList<>();

	        for (Admin admin : admins) {
	            Map<String, Object> adminMap = new HashMap<>();
	            adminMap.put("id", admin.getId());
	            adminMap.put("userName", admin.getUserName());
	            adminMap.put("Deactived", admin.isDeactivate());
	            result.add(adminMap);
	        }

	        return result;
	    }


	    @PutMapping("/updateAdmin")
	    public ResponseEntity<Map<String, Object>> updateAdmin(
	            @RequestParam Long adminId,
	            @RequestParam(required = false) String userName,
	            @RequestParam(required = false) String mobileNo,
	            @RequestParam(required = false) String emailId,
	            @RequestParam(required = false) Boolean isAvailable,
	            @RequestParam(required = false) String newPassword) {
	        try {
	            Optional<Admin> adminOptional = adminRepository.findById(adminId);

	            if (adminOptional.isPresent()) {
	                Admin admin = adminOptional.get();

	                // Update fields based on provided data
	                if (userName != null) {
	                    admin.setUserName(userName);
	                }
	                if (mobileNo != null) {
	                    admin.setMobileNo(mobileNo);
	                }
	                if (emailId != null) {
	                    admin.setEmailId(emailId);
	                }
	                if (isAvailable != null) {
	                    admin.setAvailable(isAvailable);
	                }
	                if (newPassword != null) {
	                    // Set the new password after encoding
	                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	                    admin.setPassword(passwordEncoder.encode(newPassword));
	                }

	                // Save the updated admin
	                adminRepository.save(admin);

	                // Prepare and return the response
	                Map<String, Object> response = prepareLoginResponse(admin);
	                return ResponseEntity.ok(response);
	            } else {
	                // Admin not found
	                return ResponseEntity.badRequest().body(createErrorResponse(400, "Admin not found"));
	            }
	        } catch (RuntimeException e) {
	            // Handle exceptions
	            return ResponseEntity.badRequest().body(createErrorResponse(400, "Failed to update admin: " + e.getMessage()));
	        }
	    }
	    
	    private Map<String, Object> prepareLoginResponse(Admin admin) {
	        // Prepare the login response map
	        Map<String, Object> response = new HashMap<>();
	        response.put("statuscode", 200);
	        response.put("message", "Login successful");

	        Map<String, Object> adminDetails = new HashMap<>();
	        adminDetails.put("id", admin.getId());
	        adminDetails.put("userName", admin.getUserName());
	        adminDetails.put("mobileNo", admin.getMobileNo());
	        adminDetails.put("emailId", admin.getEmailId());
	        adminDetails.put("module", admin.getModule());
	        adminDetails.put("isAvailable", admin.isAvailable());
	        adminDetails.put("ProfilePic", admin.getProfilePic());
	        response.put("admin", adminDetails);

	        // Add roles and privileges here if needed

	        return response;
	    }

	    private Map<String, Object> createErrorResponse(int statusCode, String message) {
	        // Create an error response map
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("statuscode", statusCode);
	        errorResponse.put("message", message);
	        return errorResponse;
	    }
	}




