package com.taizo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taizo.model.CandidateModel;
import com.taizo.model.EmpEnquiryModel;
import com.taizo.repository.EmpEnquiryRepository;



@Service
public class EmpEnquiryServiceimpl implements EmpEnquiryService {
	
	@Autowired
	EmpEnquiryRepository empEnquiryRepository;
	
	@PersistenceContext
    private EntityManager entityManager;

	@Override
	public EmpEnquiryModel createEmpEnquiry(EmpEnquiryModel empEnquiryModel) {
		return empEnquiryRepository.save(empEnquiryModel);
	}

	@Override
	public List<Map<String, Object>> filterEmpEnquiry(String mobileNumber,String emailId,String companyName, int page, int size, Date createdTime,
			Date endDate) {
		
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<EmpEnquiryModel> criteriaQuery = criteriaBuilder.createQuery(EmpEnquiryModel.class);
	        Root<EmpEnquiryModel> root = criteriaQuery.from(EmpEnquiryModel.class);

	        List<Predicate> predicates = new ArrayList<>();
	        

	        if (mobileNumber != null) {
	            predicates.add(criteriaBuilder.equal(root.get("mobileNumber"), mobileNumber));
	        }
	        
	        if (emailId != null) {
	            predicates.add(criteriaBuilder.equal(root.get("emailId"), emailId));
	        }
	        
	        if (companyName != null) {
	            predicates.add(criteriaBuilder.equal(root.get("companyName"), companyName));
	        }

	        if (createdTime != null && endDate != null) {
	        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
	        }
	        
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
	        
	        List<EmpEnquiryModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * size)
	                .setMaxResults(size)
	                .getResultList();

	        List<Map<String, Object>> resultMaps = new ArrayList<>();

	        for (EmpEnquiryModel empEnquiryModel : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("empEnquiryModel", empEnquiryModel);

	            resultMaps.add(resultMap);
	        }

	        return resultMaps;
	    }

	@Override
	public long filterempEnquiryCount(String mobileNumber,String emailId,String companyName, int page, int size, Date createdTime, Date endDate) {
		
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
	        Root<EmpEnquiryModel> root = criteriaQuery.from(EmpEnquiryModel.class);

	        List<Predicate> predicates = new ArrayList<>();
	        
	        

	        if (mobileNumber != null) {
	            predicates.add(criteriaBuilder.equal(root.get("mobileNumber"), mobileNumber));
	        }
	        
	        if (emailId != null) {
	            predicates.add(criteriaBuilder.equal(root.get("emailId"), emailId));
	        }
	        
	        if (companyName != null) {
	            predicates.add(criteriaBuilder.equal(root.get("companyName"), companyName));
	        }

	        if (createdTime != null && endDate != null) {
	        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
	        }
	        
	        if (predicates != null && !predicates.isEmpty()) {
	            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
	            criteriaQuery.where(predicateArray);
	        }

	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
	        
	        criteriaQuery.select(criteriaBuilder.count(root)); // Count the results

	        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

	        long totalCount = query.getSingleResult();

	        return totalCount;
	}
    }
	
	


