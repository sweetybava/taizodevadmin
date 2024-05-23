package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgCanTimelineEvents;

import java.util.List;

public interface CfgCanTimelineEventsRepository extends JpaRepository<CfgCanTimelineEvents, Long> {

	List<CfgCanTimelineEvents> findByActive(boolean b);

	CfgCanTimelineEvents findByEventName(String eventName);

	
}
