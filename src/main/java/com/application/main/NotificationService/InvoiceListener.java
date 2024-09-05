package com.application.main.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
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
		try{
			Map<String , Object> response = new HashMap<>();

			ArrayList<String> sentlist = invoice.getSentinvoicesidlist();
			String id = sentlist.get(sentlist.size()-1);
		SentInvoicesDatabaseModel sentinvoice = sentinvrepo.findById(id).get();
		
		
		response.put("LatestInvoiceObjectId",id);	
		response.put("DATETIME",sentinvoice.getSentinvoice().getForwardRevertDate().toString());
		response.put("InvoiceInboxObject", sentinvoice);
		response.put("Inboxobject", sentinvoice.getSentinvoice());
		
		return ResponseEntity.ok(response);
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
	}
	
	
	
	@KafkaListener(topics = "RevertedInvoice")
	public ResponseEntity<?> recieve(Invoice invoice ) {
		try {
		Map<String , Object> response = new HashMap<>();
		ArrayList<String> recievedlist = invoice.getRecieveinvoicesidlist();
		String id = recievedlist.get(recievedlist.size()-1);

		RecieveInvoiceDatabaseModel recievedinvoice = recieveinvrepo.findById(id).get();
		
		
		response.put("LatestInvoiceObjectId", id);	
		response.put("DATETIME",recievedinvoice.getRecievedinvoices().getForwardRevertDate().toString());
		response.put("InvoiceInboxObject", recievedinvoice);
		response.put("Inboxobject", recievedinvoice.getRecievedinvoices());
		
		return ResponseEntity.ok(response);
		
	}
		catch(Exception e) {
			e.printStackTrace();			
			return ResponseEntity.ok(e);
		}
	}

}
