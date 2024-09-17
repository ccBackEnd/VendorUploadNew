package com.application.main.Repositories;
import java.time.LocalDate;
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
	List<Invoice> findByUsernameContaining(String username);
	List<Invoice> findByUsernameContainingAndStatus(String username,String status);
	List<Invoice> findByInvoiceDateBetween(LocalDate invoiceDate1,LocalDate invDate2);
	Optional<Invoice> findByIdAndInvoiceNumber(String id, String invoiceNumber);


	Page<InvoiceDTO> findByEic(String eic, Pageable pageable);
	
	Page<InvoiceDTO> findByInvoiceNumber(String invoiceNumber,Pageable pageable);

	Page<InvoiceDTO> findByUsernameContainingAndPoNumber(String username, String poNumber,Pageable pageable);

	Page<InvoiceDTO> findByUsernameContainingAndInvoiceNumber(String username, String invoiceNumber,Pageable pageable);
	
	Page<InvoiceDTO> findByPoNumberAndInvoiceNumber(String poNumber, String invoiceNumber,Pageable pageable);

	Page<InvoiceDTO> findBypoNumberStartingWith(String prefix, Pageable pageable);

	//Page<InvoiceDTO> findByDeliveryPlantStartingWith(String deliveryPlant);

	//Page<InvoiceDTO> findBypoNumberContains(String poNumber);


	Page<InvoiceDTO> findByPoNumberContaining(String poNumber, Pageable pageable);

	long countByUsernameContaining(String username);

	Page<InvoiceDTO> findByUsernameAndStatusIgnoreCase(String username, String string, Pageable pageable);

	long countByUsernameContainingAndStatus(String username, String string);
	// In your InvoiceRepository interface

	Page<InvoiceDTO> findByUsernameAndStatusIgnoreCaseOrderByLatestRecievingDateDesc(String username, String string, Pageable pageable);
	

	

	
	

//	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrDeliveryTimelinesContainingOrInvoiceNumberContainingOrStatusContainingOrMobileNumberContainingOrDeliveryPlantContainingOrUsernameContainingOrRemarksContainingOrPaymentTypeContainingOrReceiverContainingOrClaimedByContainingOrTypeContainingOrMsmeCategoryContaining(
//			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
//			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
//			String searchItems10, String searchItems11, String searchItems12, String searchItems13,
//			String searchItems14);
//
//	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrUsernameAndDeliveryTimelinesContainingOrUsernameAndInvoiceNumberContainingOrUsernameAndStatusContainingOrUsernameAndMobileNumberContainingOrUsernameAndDeliveryPlantContainingOrUsernameAndUsernameContainingOrUsernameAndRemarksContainingOrUsernameAndPaymentTypeContainingOrUsernameAndReceiverContainingOrUsernameAndClaimedByContainingOrUsernameAndTypeContainingOrUsernameAndMsmeCategoryContaining(
//			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
//			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
//			String searchItems10, String searchItems11, String searchItems12, String searchItems13);




}
