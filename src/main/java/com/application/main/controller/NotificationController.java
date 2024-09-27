package com.application.main.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.NotificationRepository.NotificationRepository;
import com.application.main.model.NotificationModel.VendorPortalNotification;

import jakarta.servlet.http.HttpServletRequest;


@Service
@RestController
@RequestMapping("/call/vendor/Vendorportal")
public class NotificationController {
	private static Logger log = LoggerFactory.getLogger(NotificationController.class);
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	LoginUserRepository loginrepository;

	@GetMapping("/notifications")
	public ResponseEntity<?> getallNotifications(@RequestHeader("username") String username,
			HttpServletRequest request) {
		try {
			List<VendorPortalNotification> list = notificationRepository
					.findAllByRecieverusernameOrderByGeneratedAtDesc(username);
			return ResponseEntity.ok(list).ok("New Notification");
		} catch (Exception e) {
			throw e;
		}
	}

	@PutMapping("changenotificationstatus/{id}")
	public ResponseEntity<?> changeNotificationStatus(@PathVariable("id") String id) {
		try {
			Optional<VendorPortalNotification> notification = notificationRepository.findById(id);
			if (notification.isPresent())
				notification.get().setStatus("read");
			notificationRepository.save(notification.get());
			return ResponseEntity.ok("status changed to Read");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@PutMapping("changeAllNotificationsStatus")
	public ResponseEntity<?> changeStatusAll() {
		List<VendorPortalNotification> notificiationlist = notificationRepository.findAll();
		if (notificiationlist.isEmpty())
			return ResponseEntity.ok(null);
		for (VendorPortalNotification n : notificiationlist) {
			n.setStatus("read");
			notificationRepository.save(n);
		}
		return ResponseEntity.ok("All " + notificiationlist.size() + " notifications are read");
	}

	@DeleteMapping("/deleteNotification/{id}")
	public ResponseEntity<?> deletenotification(@PathVariable("id") String id) {
		try {
			if (notificationRepository.findById(id).isPresent())
				notificationRepository.deleteById(id);
			else
				return null;
			return ResponseEntity.ok("successfully deleted");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@PutMapping("deleteAllNotifications")
	public ResponseEntity<?> deleteAllNotifications() {
		notificationRepository.deleteAll();
		List<VendorPortalNotification> notificiationlist = notificationRepository.findAll();
		if (notificiationlist.isEmpty())
			return ResponseEntity.ok("Successfully deleted all notifications");
		return ResponseEntity.ok(notificiationlist);
	}

}
