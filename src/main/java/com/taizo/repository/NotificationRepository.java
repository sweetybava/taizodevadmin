package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.NotificationModel;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
	

	@Query("select n from NotificationModel n where n.candidateId = :candidateId")
	List<NotificationModel> findByCandidateId(@Param("candidateId") int candidateId);

}
