package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.GraduationCoursesModel;

@Repository
public interface GraduationCourseRepository extends JpaRepository<GraduationCoursesModel, Long> {

}
