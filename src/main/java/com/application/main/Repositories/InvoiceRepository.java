package com.application.main.Repositories;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
	//Optional<Invoice> findBypoNumber(String poNumber);
	

	Page<Invoice> findAll(Pageable pageable);
	Boolean existsByInvoiceNumber(String invoicenumber);
//	Invoice findBypoNumber(String poNumber);
	Page<InvoiceDTO> findByPoNumber(String poNumber,Pageable pageable);
	Page<InvoiceDTO> findByUsername(String username,Pageable pageable);
	Optional<Invoice> findById(String id);



	Page<InvoiceDTO> findByUsernameAndClaimedIsTrue(String username,Pageable pageable);

	//Page<InvoiceDTO> findByClaimedAndUsername(Boolean claimed, String username);

	Page<InvoiceDTO> findByClaimed(Boolean claimed, Pageable pageable);

	Page<InvoiceDTO> findByType(String string, Pageable pageable);

//	Page<InvoiceDTO> findByEicAndInvoice(String eic, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndUsername(Boolean claimed, String username, Pageable pageable);

//	Page<InvoiceDTO> findByUsernameAndInvoice(String username, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndId(Boolean claimed, String id, Pageable pageable);

	Page<InvoiceDTO> findByEicAndClaimed(String eic, boolean b, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEic(String type, String eic, Pageable pageable);

	Page<InvoiceDTO> findByUsernameAndClaimed(String username, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndEic(Boolean claimed, String eic, Pageable pageable);

	Page<InvoiceDTO> findByEic(String eic, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimed(String type, String eic, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndEicAndClaimedBy(Boolean claimed, String eic, String username, Pageable pageable);



	Page<InvoiceDTO> findByTypeAndClaimed(String string, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByClaimedTrue(Pageable pageable);

	Page<InvoiceDTO> findByClaimedFalse(Pageable pageable);

	long countByTypeAndClaimed(String string, boolean b);

	long countByTypeAndEicAndClaimed(String type, String eic, boolean b);

	Page<InvoiceDTO> findByInvoiceNumber(String invoiceNumber,Pageable pageable);

	Page<InvoiceDTO> findByUsernameAndPoNumber(String username, String poNumber,Pageable pageable);

	Page<InvoiceDTO> findByUsernameAndInvoiceNumber(String username, String invoiceNumber,Pageable pageable);
	
	Page<InvoiceDTO> findByPoNumberAndInvoiceNumber(String poNumber, String invoiceNumber,Pageable pageable);

	Page<InvoiceDTO> findBypoNumberStartingWith(String prefix, Pageable pageable);

	//Page<InvoiceDTO> findByDeliveryPlantStartingWith(String deliveryPlant);

	//Page<InvoiceDTO> findBypoNumberContains(String poNumber);

	Page<InvoiceDTO> findBypoNumberContaining(String poNumber, Pageable pageable);

	Page<InvoiceDTO> findByReceiver(String receiver, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimedAndReceiver(String type, String eic, boolean b, String receiver, Pageable pageable);

	Page<InvoiceDTO> findByTypeAndClaimedAndReceiver(String string, boolean b, String receiver, Pageable pageable);

	Page<InvoiceDTO> findByTypeAndClaimedOrReceiver(String string, boolean b, String eic, Pageable pageable);

	//Page<InvoiceDTO> findAllByTypeAndEicAndClaimedOrReceiver(String type, String eic, boolean b, String eic2, Pageable pageable);

	//Page<InvoiceDTO> findByTypeAndClaimedOrReceiverAndReceiver(String string, boolean b, String receiver, Pageable pageable);

	//Page<InvoiceDTO> findAllByTypeAndEicAndClaimedOrReceiverAndReceiver(String type, String eic, boolean b,
	//		String receiver, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndClaimedByOrReceiver(Boolean claimed, String claimedBy, String receiver, Pageable pageable);

//	Optional<Invoice> findByPoNumber(String poNumber, Pageable pageable);

	
	Page<InvoiceDTO> findByPoNumberContaining(String poNumber, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimedOrReceievedBy(String type, String eic, boolean b, String receievedBy, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimedOrReceievedByAndReceievedBy(String type, String eic, boolean b,
			String receievedBy, Pageable pageable);

	Page<InvoiceDTO> findByTypeAndClaimedOrReceievedByAndReceievedBy(String string, boolean b, String receievedBy, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimedAndReceievedBy(String type, String eic, boolean b, String receievedBy, Pageable pageable);

	Page<InvoiceDTO> findAllByTypeAndEicAndClaimedOrReceiver(String type, String eic, boolean b, String eic2, Pageable pageable);

	Page<InvoiceDTO> findByClaimedAndClaimedBy(Boolean claimed, String claimedBy, Pageable pageable);

	Page<InvoiceDTO> findByRoleName(String roleName, Pageable pageable);

	Page<InvoiceDTO> findByRoleNameAndTypeAndClaimed(String roleName, String string, boolean b, Pageable pageable);

	Page<InvoiceDTO> findByTypeAndClaimed(String roleName, String string, boolean b, Pageable pageable);

	long countByUsernameAndClaimed(String username, boolean b);

	long countByUsername(String username);

	Page<InvoiceDTO> findByUsernameAndStatus(String username, String string, Pageable pageable);

	long countByUsernameAndStatus(String username, String string);
	// In your InvoiceRepository interface

	Collection<? extends Invoice> DeliveryTimelinesContaining(String deliveryTimelines);

	Collection<? extends Invoice> findByInvoiceNumberContaining(String invoiceNumber);

	Collection<? extends Invoice> findByStatusContaining(String status);

	Collection<? extends Invoice> findByInvoiceAmount(Double invoiceAmount);

	Collection<? extends Invoice> findByMobileNumberContaining(String mobileNumber);

	Collection<? extends Invoice> findByDeliveryPlantContaining(String deliveryPlant);

	Collection<? extends Invoice> findByUsernameContaining(String username);

	Collection<? extends Invoice> findByRemarksContaining(String remarks);

	Collection<? extends Invoice> findByPaymentTypeContaining(String paymentType);

	Collection<? extends Invoice> findByReceiverContaining(String receiver);

	Collection<? extends Invoice> findByClaimedByContaining(String claimedBy);

	Collection<? extends Invoice> findByMsmeCategoryContaining(String msmeCategory);

	Collection<? extends Invoice> findByTypeContaining(String type);

	

	

	
	

	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrDeliveryTimelinesContainingOrInvoiceNumberContainingOrStatusContainingOrMobileNumberContainingOrDeliveryPlantContainingOrUsernameContainingOrRemarksContainingOrPaymentTypeContainingOrReceiverContainingOrClaimedByContainingOrTypeContainingOrMsmeCategoryContaining(
			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
			String searchItems10, String searchItems11, String searchItems12, String searchItems13,
			String searchItems14);

	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrUsernameAndDeliveryTimelinesContainingOrUsernameAndInvoiceNumberContainingOrUsernameAndStatusContainingOrUsernameAndMobileNumberContainingOrUsernameAndDeliveryPlantContainingOrUsernameAndUsernameContainingOrUsernameAndRemarksContainingOrUsernameAndPaymentTypeContainingOrUsernameAndReceiverContainingOrUsernameAndClaimedByContainingOrUsernameAndTypeContainingOrUsernameAndMsmeCategoryContaining(
			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
			String searchItems10, String searchItems11, String searchItems12, String searchItems13);




}
