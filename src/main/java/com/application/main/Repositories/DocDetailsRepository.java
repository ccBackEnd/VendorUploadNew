package com.application.main.Repositories;



import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.credentialmodel.DocDetails;


public interface DocDetailsRepository extends MongoRepository<DocDetails, String> {
	
	Optional<DocDetails> findByUrl(String url);
}
