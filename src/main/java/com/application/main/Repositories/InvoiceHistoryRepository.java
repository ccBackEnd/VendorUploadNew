package com.application.main.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.InvoiceHistoryDatabase;
import java.util.List;




public interface InvoiceHistoryRepository extends MongoRepository<InvoiceHistoryDatabase, String> {
	
	List<InvoiceHistoryDatabase> findByInvoiceid(String invoiceid);
	

}
