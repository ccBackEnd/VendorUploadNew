package com.application.main.InboxmodelRepository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Inboxmodel.SentInvoicesDatabaseModel;

public interface SentInvoicesRepository extends MongoRepository<SentInvoicesDatabaseModel, String> {

	List<SentInvoicesDatabaseModel> findByInvoicenumber(String invoicenumber);
	List<SentInvoicesDatabaseModel> findByInvoiceid(String invoiceid);
}
