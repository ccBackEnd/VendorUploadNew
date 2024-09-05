package com.application.main.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.application.main.Inboxmodel.RecieveInvoiceDatabaseModel;
import com.application.main.Inboxmodel.SentInvoicesDatabaseModel;
import com.application.main.InboxmodelRepository.RecievedInvoiceRepository;
import com.application.main.InboxmodelRepository.SentInvoicesRepository;
import com.application.main.model.Invoice;

@Component
public class InvoiceListener {
	
	@Autowired
	SentInvoicesRepository sentinvrepo;
	

	@Autowired
	RecievedInvoiceRepository recieveinvrepo;
	
	@KafkaListener(topics = "ForwardedInvoice")
	public ResponseEntity<?> recievedInvoice(Invoice invoice) {
		Map<String , Object> response = new HashMap<>();
		LinkedHashMap<String,String> mapid = invoice.getSentinvoicesidlist();
		List<Map.Entry<String, String>> entryList = new ArrayList<>(mapid.entrySet());
		Map.Entry<String, String> lastmapentry = entryList.get(entryList.size() - 1);
		SentInvoicesDatabaseModel sentinvoice = sentinvrepo.findById(lastmapentry.getValue()).get();
		
		
		response.put("LatestInvoiceObjectId", lastmapentry.getValue());	
		response.put("DATETIME",sentinvoice.getSentinvoice().getForwardRevertDate().toString());
		response.put("InvoiceInboxObject", sentinvoice);
		response.put("Inboxobject", sentinvoice.getSentinvoice());
		
		return ResponseEntity.ok(response);
		
	}
	
	
	
	@KafkaListener(topics = "RevertedInvoice")
	public ResponseEntity<?> recieve(Invoice invoice ) {
		Map<String , Object> response = new HashMap<>();
		LinkedHashMap<String,String> mapid = invoice.getRecieveinvoicesidlist();
		List<Map.Entry<String, String>> entryList = new ArrayList<>(mapid.entrySet());
		Map.Entry<String, String> lastmapentry = entryList.get(entryList.size() - 1);
		RecieveInvoiceDatabaseModel recievedinvoice = recieveinvrepo.findById(lastmapentry.getValue()).get();
		
		
		response.put("LatestInvoiceObjectId", lastmapentry.getValue());	
		response.put("DATETIME",recievedinvoice.getRecievedinvoices().getForwardRevertDate().toString());
		response.put("InvoiceInboxObject", recievedinvoice);
		response.put("Inboxobject", recievedinvoice.getRecievedinvoices());
		
		return ResponseEntity.ok(response);
		
	}

}
