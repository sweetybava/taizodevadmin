package com.taizo.repository.specification;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.taizo.model.EmployerModel;

public class EmployerSpecification implements Specification<EmployerModel> {
	
	 private EmployerModel filter;

	    public EmployerSpecification(EmployerModel filter) {
	        super();
	        this.filter = filter;
	    }

	    public Predicate toPredicate(Root<EmployerModel> root, CriteriaQuery<?> cq,
	            CriteriaBuilder cb) {

	        Predicate p = cb.conjunction();

	    
	    	String[] elements = filter.getCountry().split(","); 
			List<String> country = Arrays.asList(elements);
			
			String[] elements1 = filter.getCategory().split(","); 
			List<String> category1 = Arrays.asList(elements1);
		
			
		    if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
	            p.getExpressions().add(
                cb.and(root.get("category").in(category1)));
	        }

	        if (filter.getCountry() != null && !filter.getCountry().isEmpty()) {
	            p.getExpressions().add(
	                    cb.and(root.get("country").in(country)
	                            ));
	        }
	        if (!filter.getListPlans().isEmpty() && filter.getListPlans().size()!=0) {
	        	if(filter.getListPlans().equals(0)) {
	        		
	        	}else {
	            p.getExpressions().add(
	                    cb.and(root.get("plan").in(filter.getListPlans())
	                            ));
	        	}
	        }
	     

	        return p;
	    }	

}
