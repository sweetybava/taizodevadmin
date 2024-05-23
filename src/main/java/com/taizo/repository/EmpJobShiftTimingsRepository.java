package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgEmpJobShiftTimings;

public interface EmpJobShiftTimingsRepository extends JpaRepository<CfgEmpJobShiftTimings,Integer>{

	List<CfgEmpJobShiftTimings> findByShiftType(String shiftType);


}
