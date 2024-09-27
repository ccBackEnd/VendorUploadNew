package com.application.main.model.InboxModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.application.main.Repositories.InboxRepository.InvoiceHistoryRepository;
import com.application.main.model.Invoice;

@Component
public class InvoiceListener {
	
	@Autowired
	InvoiceHistoryRepository sentinvrepo;
	

	@KafkaListener(topics = "Invoiceinbox")
	public void recievedInvoice(Invoice invoice) {
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
		Set<String> set = response.keySet();
		for(String s : set) {
			System.out.println(s + " : " + response.get(s));
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
