package com.taizo.repository;

import com.taizo.model.FacebookMetaLead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FacebookMetaLeadRepository extends JpaRepository <FacebookMetaLead,Long> {

	List<FacebookMetaLead> findAll();

	FacebookMetaLead findByMobileNumber(String mobileNumber);

	FacebookMetaLead findByMobileNumber(long mn);
	
	 boolean existsByMobileNumber(String mobileNumber);

	 Optional<FacebookMetaLead> findFirstByPreferredLocationOrderByCreatedTimeDesc(String preferredLocation);

	
}
