package com.application.main.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.UserDTO;
import com.application.main.model.VendorUserModel;


public interface VendorUserRepository extends MongoRepository<UserDTO, String>{
	
	boolean existsByUsernameAndFirstNameAndLastName(String username,String firstName, String lastName);
	Optional<UserDTO> findByUsername(String username);
	List<UserDTO> findByEicdepartmentsContaining(List<String> eicdepartments);
	List<UserDTO> findByUserroles(List<String> userroles);

		boolean existsByUsername(String username);


}
