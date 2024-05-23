package com.taizo.repository.specification;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.taizo.model.JobsModel;

public class JobsSpecification implements Specification<JobsModel> {
	
	 private JobsModel filter;

	    public JobsSpecification(JobsModel filter) {
	        super();
	        this.filter = filter;
	    }

	    public Predicate toPredicate(Root<JobsModel> root, CriteriaQuery<?> cq,
	            CriteriaBuilder cb) {

	        Predicate p = cb.conjunction();
	        
	        String[] category = filter.getJobCategory().split(","); 
			List<String> jobCategory = Arrays.asList(category);
			String[] loc = filter.getJobLocation().split(","); 
			List<String> jobLocation = Arrays.asList(loc);
			String[] qual = filter.getQualification().split(","); 
			List<String> qualification = Arrays.asList(qual);
			String[] bene = filter.getBenefits().split(","); 
			List<String> benefits = Arrays.asList(bene);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date()); // Now use today date.
			String expiryDate = sdf.format(c.getTime());


	        if (filter.getJobCategory() != null && !filter.getJobCategory().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(root.get("jobCategory").in(jobCategory)
	                            ));
	        }
	        if (filter.getJobLocation() != null && !filter.getJobLocation().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(root.get("jobLocation").in(jobLocation)
	                            ));
	        }
	        
	        if (filter.getQualification() != null && !filter.getQualification().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(root.get("qualification").in(qualification)
	                            ));
	        }
	        if (filter.getBenefits() != null && !filter.getBenefits().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(root.get("benefits").in(benefits)
	                            ));
	        }
	        
	        if (filter.getJobExp() != -1) {
	            p.getExpressions().add(
	                    cb.and(cb.greaterThanOrEqualTo(root.get("jobExp"), filter.getJobExp())
	                            ));
	        }
	        
	        if (filter.getJobMaxExp() != -1) {
	            p.getExpressions().add(
	                    cb.and(cb.lessThanOrEqualTo(root.get("jobMaxExp"), filter.getJobMaxExp())
	                            ));
	        }
	        
	        if (filter.getAge() != -1) {
	            p.getExpressions().add(
	                    cb.and(cb.greaterThanOrEqualTo(root.get("age"), filter.getAge())
	                            ));
	        }
	        
	        if (filter.getMaxAge() != -1) {
	            p.getExpressions().add(
	                    cb.and(cb.lessThanOrEqualTo(root.get("maxAge"), filter.getMaxAge())
	                            ));
	        }
	        if (filter.getSalary()!= -1) {
	            p.getExpressions().add(
	                    cb.and(cb.greaterThanOrEqualTo(root.get("salary"), filter.getSalary())
	                            ));
	        }
	        
	        if (filter.getMaxSalary() != -1) {
	            p.getExpressions().add(
	                    cb.and(cb.lessThanOrEqualTo(root.get("maxSalary"), filter.getMaxSalary())
	                            ));
	        }
	        if (filter.getJobStatus() != null && !filter.getJobStatus().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.equal(root.get("jobStatus"), filter.getJobStatus())
	                            ));
	        }
			if(filter.getExpiryDate().equalsIgnoreCase("Closed")) {
				p.getExpressions().add(
	                    cb.and(cb.lessThan(root.get("expiryDate"), expiryDate)
	                            ));
				
			}else {
				p.getExpressions().add(
	                    cb.and(cb.greaterThanOrEqualTo(root.get("expiryDate"), expiryDate)
	                            ));
			}
		

	        
	        return p;
	    }	

}
