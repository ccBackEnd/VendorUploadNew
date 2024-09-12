package com.application.main.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.credentialmodel.UserDTO;



public interface LoginUserRepository extends MongoRepository<UserDTO, String>{
	
	boolean existsByUsernameAndFirstNameAndLastName(String username,String firstName, String lastName);
	Optional<UserDTO> findByUsername(String username);
	List<UserDTO> findByEicDepartmentsContaining(List<String> eicdepartments);
	List<UserDTO> findByUserRolesContaining(List<String> userroles);
	boolean existsByEmail(String useremail);
	Optional<UserDTO> findByEmailAndUsername(String email, String username);
	Optional<UserDTO> findByUserCode(String userCode);
		//boolean existsByUsername(User username);

		boolean existsByUsername(String username);
		List<UserDTO> findByEic(boolean eic);
}
