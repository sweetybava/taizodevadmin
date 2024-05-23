package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerJobPersonalizationModel;
import com.taizo.model.EmployerPaymentModel;
import com.taizo.model.PlansModel;

@Repository
public interface PlansRepository extends JpaRepository<PlansModel, Long>, 
	JpaSpecificationExecutor<PlansModel> {

	Optional<PlansModel> findById(int id);

	@Query("select c from PlansModel c where c.active = :active")
	List<PlansModel> findAllByActive(@Param("active") boolean active);

	@Query("select c from PlansModel c where c.isExperienced = :type and c.active = :active")
	List<PlansModel> findByExperiencedStatus(@Param("type") boolean type, @Param("active") boolean active);

	PlansModel findByActiveAndIsExperienced(boolean b, boolean c);

	List<PlansModel> findByActive(boolean isActive);

	PlansModel findByAmount(int amounts);


}
