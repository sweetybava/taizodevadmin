package com.taizo.repository.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.taizo.model.CandidateModel;

public class CandidateSpecification implements Specification<CandidateModel> {
	
	 private CandidateModel filter;

	    public CandidateSpecification(CandidateModel filter) {
	        super();
	        this.filter = filter;
	    }
	    
		public static Specification<CandidateModel> findAll(Long empId, Date fromDate, Date toDate, String jobCategory) {
			// TODO Auto-generated method stub
			return (root, query, cb) -> {
				final Collection<Predicate> predicates = new ArrayList<Predicate>();
				
				if (empId != null) {				
					Predicate userPredicate = cb.equal(root.get("empId"), empId);
					predicates.add(userPredicate);
				}
				
				if (fromDate != null && toDate != null) {
					Predicate datePredicate = cb.between(root.get("createdTime"),
							fromDate, toDate);
					predicates.add(datePredicate);
				}
				
				if (jobCategory != null && !jobCategory.isEmpty()) {
					Predicate searchPredicate = cb.like(cb.lower(root.get("jobCategory").get("firstName")), "%" + jobCategory.toLowerCase() + "%");
					predicates.add(searchPredicate);
				}
				
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));


		};
		}
		
	    public Predicate toPredicate(Root<CandidateModel> root, CriteriaQuery<?> cq,
	            CriteriaBuilder cb) {

	        Predicate p = cb.conjunction();

	        if (filter.getJobType() != null && !filter.getJobType().isEmpty()) {
	            p.getExpressions()
	                    .add(cb.like(root.get("jobType"), "%"+filter.getJobType()+"%"));
	        }

	        if (filter.getJobCategory() != null && !filter.getJobCategory().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.equal(root.get("jobCategory"), filter.getJobCategory())
	                            ));
	        }
	        if (filter.getCurrentState() != null && !filter.getCurrentState().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.like(root.get("currentState"), "%"+filter.getCurrentState()+"%")
	                            ));
	        }
	        
	        if (filter.getQualification() != null && !filter.getQualification().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.equal(root.get("qualification"), filter.getQualification())
	                            ));
	        }
	        if (filter.getSpecification() != null && !filter.getSpecification().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.equal(root.get("specification"), filter.getSpecification())
	                            ));
	        }
	        
	        if (filter.getGender() != null && !filter.getGender().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.equal(root.get("gender"), filter.getGender())
	                            ));
	        }
	        
	        if (filter.getExperience() != 0  && filter.getExpMonths() != 0) {
	            p.getExpressions().add(
	                    cb.and(cb.between(root.get("experience"), filter.getExperience(), filter.getExpMonths())
	                            ));
	        }
	        
	        if (filter.getAge() != null && !filter.getAge().isEmpty() && filter.getDateOfBirth() != null
	        		&& !filter.getDateOfBirth().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(cb.between(root.get("age"), filter.getAge(), filter.getDateOfBirth())
	                            ));
	        }

	        return p;
	    }

	

}
