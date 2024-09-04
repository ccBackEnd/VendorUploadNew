package com.application.main.InboxmodelRepository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Inboxmodel.RecieveInvoiceDatabaseModel;

public interface RecievedInvoiceRepository extends MongoRepository<RecieveInvoiceDatabaseModel, String> {
	
	List<RecieveInvoiceDatabaseModel> findByInvoicenumber(String invoicenumber);
	List<RecieveInvoiceDatabaseModel> findByInvoiceid(String invoiceid);

}
