package com.application.main.Repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.application.main.model.PoSummary;
import java.util.Set;


@EnableMongoRepositories
public interface PoSummaryRepository extends MongoRepository<PoSummary, String> {
	
	List<PoSummary> findByPoNumberContaining(String x); 
	Optional<PoSummary> findByPoNumber(String poNumber);
	List<PoSummary> findByEic(String eic);
	List<PoSummary> findByPoStatus(String poStatus);
	List<PoSummary> findByUsername(String username);
	List<PoSummary> findByPoStatusAndUsername(String postatus,String username);
	List<PoSummary> findByPoStatusOrUsername(String postatus,String username);
	
	List<PoSummary> findByPoIssueDateBetween(LocalDate StartpoIssueDate,LocalDate EndPoIssueDate);
	Boolean existsByPoNumber(String poNumber);
	
}
