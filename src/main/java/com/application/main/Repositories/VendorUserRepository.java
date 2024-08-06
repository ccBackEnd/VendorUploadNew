package com.application.main.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.VendorUserModel;
import java.util.List;
import java.util.Optional;


public interface VendorUserRepository extends MongoRepository<VendorUserModel, String>{
	
	boolean existsByUsernameAndFirstNameAndLastName(String username,String firstName, String lastName);
	Optional<VendorUserModel> findByUsername(String username);
	List<VendorUserModel> findByEicdepartmentsContaining(List<String> eicdepartments);
	List<VendorUserModel> findByUserroles(List<String> userroles);
}
