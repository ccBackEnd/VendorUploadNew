package com.application.main.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class NotificationProducer {
	
	@Autowired
	KafkaTemplate<String, NotificationModel> kafkatemplate;
	public void sendMessage(String title , String message , String userid, String senderid) {
		NotificationModel nmd = new NotificationModel(message , title , senderid , userid , false);
		kafkatemplate.send("notificationtovendor", nmd);
	}
	

}
