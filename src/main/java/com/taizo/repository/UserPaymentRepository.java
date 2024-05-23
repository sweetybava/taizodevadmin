package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.UserPaymentModel;


@Repository
public interface UserPaymentRepository extends JpaRepository<UserPaymentModel, Long> {

	@Query("select j from UserPaymentModel j where j.userId = :userId")
	List<UserPaymentModel> findPaymentHistory(@Param("userId") int userId);

	UserPaymentModel findById(int paymentID);


}
