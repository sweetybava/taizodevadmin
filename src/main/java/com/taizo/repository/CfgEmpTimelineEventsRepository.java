package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgEmpTimelineEvents;

public interface CfgEmpTimelineEventsRepository extends JpaRepository<CfgEmpTimelineEvents, Long> {

	List<CfgEmpTimelineEvents> findByActive(boolean b);

}
