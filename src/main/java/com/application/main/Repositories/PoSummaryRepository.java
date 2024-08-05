package com.application.main.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.PoDTO;
import com.application.main.model.PoSummary;


public interface PoSummaryRepository extends MongoRepository<PoSummary, String> {

	Optional<PoSummary> findByPoNumber(String poNumber);
	List<PoSummary> findByEic(String eic);
	List<PoSummary> findByPoStatus(String poStatus);
	Optional<Page<PoSummary>> findByUsername(String username,Pageable pageable);
	Boolean existsByPoNumber(String poNumber);
}
