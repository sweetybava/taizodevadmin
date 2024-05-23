package com.taizo.controller.admin;

import com.taizo.model.*;
import com.taizo.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminCfgController {

    @Autowired
    CfgFullTimeJobRolesRepository cfgFullTimeJobRolesRepository;
    
    @Autowired
    CfgCanTimelineEventsRepository cfgCanTimelineEventsRepository;
    
    @Autowired
    CfgEmpTimelineEventsRepository cfgEmpTimelineEventsRepository;
    
    @Autowired
    CfgCanAdminAreaRepository cfgCanAdminAreaRepository;

    @Autowired
    CfgCanAdminCityGroupingRepository cfgCanAdminCityGroupingRepository;
    
 

    @PostMapping("/fullTimeJobRoles")
    public ResponseEntity<?>createNewJobRole(@RequestBody CfgFullTimeJobRoles cfgFullTimeJobRoles)
    {
        CfgFullTimeJobRoles cfgFullTimeJobRoles1 = new CfgFullTimeJobRoles();

        cfgFullTimeJobRoles1.setJobRoles(cfgFullTimeJobRoles.getJobRoles());
        cfgFullTimeJobRoles1.setCategory("job category");
        cfgFullTimeJobRoles1.setIndustryId(cfgFullTimeJobRoles.getIndustryId());
        cfgFullTimeJobRoles1.setActive(true);

        cfgFullTimeJobRolesRepository.save(cfgFullTimeJobRoles1);

        HashMap<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("message","Successfully");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/canEvents")
    public List<CfgCanTimelineEvents> getAllActiveEvents(
            @RequestParam(name = "active", required = false) Boolean active) {
        if (active != null && active) {
            return cfgCanTimelineEventsRepository.findByActive(true);
        } else {
            return cfgCanTimelineEventsRepository.findAll();
        }
    }


    @PutMapping("/fullTimeJobRoles")
    public ResponseEntity<?> updateJobRole(@RequestBody CfgFullTimeJobRoles cfgFullTimeJobRoles) {
        Optional<CfgFullTimeJobRoles> existingJobRole = cfgFullTimeJobRolesRepository.findById(cfgFullTimeJobRoles.getId());

        if (existingJobRole.isPresent()) {
            CfgFullTimeJobRoles existingRole = existingJobRole.get();
            existingRole.setJobRoles(cfgFullTimeJobRoles.getJobRoles());
            existingRole.setIndustryId(cfgFullTimeJobRoles.getIndustryId());
            existingRole.setActive(cfgFullTimeJobRoles.isActive());
            cfgFullTimeJobRolesRepository.save(existingRole);

            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("message", "Successfully updated");
            return ResponseEntity.ok(map);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 400);
            map.put("message", "not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
    }
    
    @GetMapping("/fullTimeJobRoles")
    public ResponseEntity<Page<CfgFullTimeJobRoles>> getPaginatedJobRoles(
            @RequestParam(name = "active", required = false) Boolean active, 
            Pageable pageable) {
        
        Page<CfgFullTimeJobRoles> jobRolesPage;

        if (active != null && active) {
            jobRolesPage = cfgFullTimeJobRolesRepository.findByActiveTrue(pageable);
        } else {
            jobRolesPage = cfgFullTimeJobRolesRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(jobRolesPage);
    }
    
    @GetMapping("/empEvents")
    public List<CfgEmpTimelineEvents> getEmpAllActiveEvents(
            @RequestParam(name = "active", required = false) Boolean active) {
        if (active != null && active) {
            return cfgEmpTimelineEventsRepository.findByActive(true);
        } else {
            return cfgEmpTimelineEventsRepository.findAll();
        }
    }
    
    @GetMapping(path = "/areas")
	public ResponseEntity<?> getIndianCitiesArea(@RequestParam(value = "city_id", required = false) final int cityId) {

		
		List<CfgCanAdminArea> details = cfgCanAdminAreaRepository.findByCityIdAndActive(cityId,true);


		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Area Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

    @PutMapping("/metaAdminActivity")
    public ResponseEntity<?> activeAdminIdViceAssign(@RequestParam int id, @RequestParam boolean active) {
        HashMap<String, String> response = new HashMap<>();
        List<CfgCanAdminCityGrouping> cfgCanAdminCityGroupingList = cfgCanAdminCityGroupingRepository.findByAdminId(id);

        if (!cfgCanAdminCityGroupingList.isEmpty()) {
            try {
                for (CfgCanAdminCityGrouping cfg : cfgCanAdminCityGroupingList) {
                    cfg.setActive(active);
                }
                // Save the updated entities back to the database
                cfgCanAdminCityGroupingRepository.saveAll(cfgCanAdminCityGroupingList);

                response.put("status", "success");
                response.put("message", "Active status updated successfully");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", "Failed to update active status");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else {
            response.put("status", "error");
            response.put("message", "No records found for adminId: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    
  

}
