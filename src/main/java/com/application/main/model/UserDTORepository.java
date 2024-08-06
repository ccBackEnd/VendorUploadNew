package com.application.main.model;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;



public interface UserDTORepository extends MongoRepository<UserDTO, String> {
	
	public UserDTO findByAssignedrole(String role);
	public boolean existsByUsername(String username);
	Optional<UserDTO> findByUsername(String username);
	
	

}
