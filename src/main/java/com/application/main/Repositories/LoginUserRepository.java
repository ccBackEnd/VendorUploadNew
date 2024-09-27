package com.application.main.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.UserModel.UserDetails;



public interface LoginUserRepository extends MongoRepository<UserDetails, String>{
	
	boolean existsByUsernameAndFirstNameAndLastName(String username,String firstName, String lastName);
	Optional<UserDetails> findByUsername(String username);
	List<UserDetails> findByEicDepartmentsContaining(List<String> eicdepartments);
	List<UserDetails> findByUserRolesContaining(List<String> userroles);
	boolean existsByEmail(String useremail);
	Optional<UserDetails> findByEmailAndUsername(String email, String username);
	Optional<UserDetails> findByUserCode(String userCode);
		//boolean existsByUsername(User username);

		boolean existsByUsername(String username);
		List<UserDetails> findByEic(boolean eic);
}
