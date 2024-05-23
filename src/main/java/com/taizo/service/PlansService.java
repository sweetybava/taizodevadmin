package com.taizo.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taizo.model.LeadModel;
import com.taizo.model.PlansModel;

public interface PlansService {

	PlansModel update(int id, PlansModel plan);

	List<PlansModel> findActivePlans(boolean isActive);

}
