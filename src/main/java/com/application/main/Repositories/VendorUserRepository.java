package com.application.main.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.VendorUserModel;


public interface VendorUserRepository extends MongoRepository<VendorUserModel, String>{
	
	boolean existsByUsernameAndFirstNameAndLastName(String username,String firstName, String lastName);
	Optional<VendorUserModel> findByUsername(String username);
	List<VendorUserModel> findByEicdepartmentsContaining(List<String> eicdepartments);
	List<VendorUserModel> findByUserroles(List<String> userroles);
	boolean existsByVendoremail(String vendoremail);
	boolean existsByVendoruserid(String vendoruserid);

		VendorUserModel findByVendoremail(String email);
		//boolean existsByUsername(User username);

		boolean existsByUsername(String username);

		VendorUserModel findByOtp(String otp);

		VendorUserModel findByEic(String eic);
}
