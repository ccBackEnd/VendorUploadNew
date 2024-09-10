package com.application.main.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.application.main.Inboxmodel.InvoicesHistoryCollection;
import com.application.main.InboxmodelRepository.InvoiceHistoryRepository;
import com.application.main.model.Invoice;

@Component
public class InvoiceListener {
	
	@Autowired
	InvoiceHistoryRepository sentinvrepo;
	

	
	@KafkaListener(topics = "Invoiceinbox")
	public ResponseEntity<?> recievedInvoice(Invoice invoice) {
		try{
			Map<String , Object> response = new HashMap<>();

			ArrayList<String> sentlist = invoice.getSentrevertidlist();
			String id = sentlist.get(sentlist.size()-1);
		InvoicesHistoryCollection recievedInvoice = sentinvrepo.findById(id).get();
		
		
		response.put("LatestInvoiceObjectId",id);	
		response.put("Date",recievedInvoice.getDateofarrival());
		response.put("Time", recievedInvoice.getTimeofarrival());
		response.put("InvoiceInboxObject", recievedInvoice);
		response.put("Inboxobject", recievedInvoice.getInvoicehistory());
		
		return ResponseEntity.ok(response);
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
	}

}
