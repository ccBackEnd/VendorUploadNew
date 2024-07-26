package com.application.main.Repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.application.main.model.Invoice;
import com.application.main.model.UserClass;


@Repository
public interface UserRepository extends MongoRepository<UserClass, String> {
    Optional<UserClass> findByUsername(String username);

	boolean existsByEmail(String email);
	UserClass findByEmail(String email);
	//boolean existsByUsername(User username);

	boolean existsByUsername(String username);

	UserClass findByOtp(String otp);

	UserClass findByEic(String eic);

	Invoice save(Invoice invoice);

	

	





	//List<User> findByCreatedByAndUsername(String createdBy, String username);

	//List<User> findByCreatedBy(String createdBy);
}

 




