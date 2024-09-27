package com.application.main.Repositories.InboxRepository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.InboxModel.InvoicesHistoryCollection;

public interface InvoiceHistoryRepository extends MongoRepository<InvoicesHistoryCollection, String> {

	List<InvoicesHistoryCollection> findByInvoicenumberOrderByForwardRevertDate(String invoicenumber);
	List<InvoicesHistoryCollection> findByInvoiceid(String invoiceid);
}
