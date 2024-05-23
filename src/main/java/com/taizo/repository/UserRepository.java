package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.UserModel;


@Repository

public interface UserRepository extends JpaRepository<UserModel, Long> {
	
	UserModel findByMobileNumber(long mobileNumber);
	
	@Query(value = "SELECT u FROM UserModel u where u.mobileNumber = ?1")
	Optional<UserModel> findByMobNumber(long mobileNumber);


	@Query(value = "SELECT u FROM UserModel u where u.mobileNumber = ?1 and u.password = ?2 and u.countryCode = ?3")
	Optional<UserModel> login(long mobileNumber,String password,String country);

	Optional<UserModel> findByToken(String token);

	Optional<UserModel> findById(int id);

	@Query("select t from UserModel t where t.id = :id")
	List<UserModel> findUserId(int id);

	void deleteById(int userID);

}
