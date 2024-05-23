package com.taizo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taizo.model.DraftJobsModel;

public interface DraftJobsService {

	List<DraftJobsModel> getDraftJobsByEmployerId(int employerId);
	
	void deleteDraftJobById(int id);

	Page<DraftJobsModel> getDraftJobsByEmployerId(int employerId, Pageable pageable);

	Page<DraftJobsModel> getDraftJobsByEmployer(Integer employerId, int page, int size);

	Page<DraftJobsModel> getAlldraftjob(Pageable pageable);

}
