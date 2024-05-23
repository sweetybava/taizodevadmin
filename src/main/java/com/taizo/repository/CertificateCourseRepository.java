package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CertificateCoursesModel;
import com.taizo.model.KeySkillsModel;

@Repository
public interface CertificateCourseRepository extends JpaRepository<CertificateCoursesModel, Long> {

	@Query(value = "SELECT u FROM CertificateCoursesModel u order by jsOrderNo desc")
	List<CertificateCoursesModel> findByJsOrder();

	Optional<CertificateCoursesModel> findByCourses(String courses);

}
