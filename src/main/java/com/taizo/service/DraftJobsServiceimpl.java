package com.taizo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.taizo.model.DraftJobsModel;
import com.taizo.repository.DraftJobsRepository;

@Service
public class DraftJobsServiceimpl implements DraftJobsService {
	
	@Autowired
	 private DraftJobsRepository draftJobsRepository ;

	@Override
	public List<DraftJobsModel> getDraftJobsByEmployerId(int employerId) {
		 return draftJobsRepository.findByEmployerId(employerId);
	}

	@Override
	public void deleteDraftJobById(int id) {
	     draftJobsRepository.deleteById(id);
	}
	@Override
	public Page<DraftJobsModel> getDraftJobsByEmployerId(int employerId, Pageable pageable) {
        return draftJobsRepository.findByEmployerId(employerId, pageable);

}

	@Override
	public Page<DraftJobsModel> getDraftJobsByEmployer(Integer employerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
	    return draftJobsRepository.findByEmployerId(employerId, pageable);
	}

	@Override
	public Page<DraftJobsModel> getAlldraftjob(Pageable pageable) {
		// TODO Auto-generated method stub
		return draftJobsRepository.findAll(pageable);
	}
}
