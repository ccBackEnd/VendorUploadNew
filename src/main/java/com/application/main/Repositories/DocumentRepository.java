package com.application.main.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.DocumentsMongo;

public interface DocumentRepository extends MongoRepository<DocumentsMongo, String>{

	List<DocumentsMongo> findAllByusername(String username);

}
