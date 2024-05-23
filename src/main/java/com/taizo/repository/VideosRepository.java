package com.taizo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.SampleVideosModel;

@Repository
public interface VideosRepository extends PagingAndSortingRepository<SampleVideosModel, Long>, JpaRepository<SampleVideosModel, Long>,
		JpaSpecificationExecutor<SampleVideosModel> {

	List<SampleVideosModel> findAllByActive(boolean b);

}
