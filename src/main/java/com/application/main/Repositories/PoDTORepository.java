package com.application.main.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.PoDTO;

public interface PoDTORepository extends MongoRepository<PoDTO, String> {

}
