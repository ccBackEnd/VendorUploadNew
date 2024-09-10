package com.application.main.InboxmodelRepository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Inboxmodel.InvoicesHistoryCollection;

public interface InvoiceHistoryRepository extends MongoRepository<InvoicesHistoryCollection, String> {

	List<InvoicesHistoryCollection> findByInvoicenumberOrderByForwardRevertDateDesc(String invoicenumber);
	List<InvoicesHistoryCollection> findByInvoiceid(String invoiceid);
}
