package com.application.main.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.application.main.Repositories.NotificationRepository.NotificationRepository;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.NotificationModel.VendorPortalNotification;

@Service
public class NotificationService {
	private KafkaTemplate<String, Invoice> ktemplate;
	private KafkaTemplate<String, VendorPortalNotification> notificationTemplate;
	private NotificationRepository notificationRepository;
	Logger logApp = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	public NotificationService(KafkaTemplate<String, Invoice> ktemplate,
			KafkaTemplate<String, VendorPortalNotification> notificationTemplate,
			NotificationRepository notificationRepository) {
		super();
		this.ktemplate = ktemplate;
		this.notificationTemplate = notificationTemplate;
		this.notificationRepository = notificationRepository;
	}
	
	public boolean sendNotification(String senderName, String recieverName, String invoiceNumber, DocumentDetails invoicedetails , String notificationStatus) {
		String notificationmessage = "Invoice with " +invoiceNumber + " is generated";
		VendorPortalNotification vendornotification = new VendorPortalNotification(null, recieverName,
				LocalDateTime.now(), invoiceNumber, invoicedetails.getName(), senderName, notificationmessage, notificationStatus,
				null);
		vendornotification = notificationRepository.save(vendornotification);
		logApp.info("Notification Initiated...");
		logApp.info("Notification Sending...");
		notificationTemplate.send("vendorportalnotification", vendornotification);
		logApp.info("Notification Sent Properly...");
		return true;
	}
	
	
}
