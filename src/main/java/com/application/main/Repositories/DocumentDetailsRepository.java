package com.application.main.Repositories;



import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.DocumentDetails;


public interface DocumentDetailsRepository extends MongoRepository<DocumentDetails, String> {
	
	Optional<DocumentDetails> findByUrl(String url);
}
