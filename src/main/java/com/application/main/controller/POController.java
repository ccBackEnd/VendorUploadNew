package com.application.main.controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.application.main.Repositories.DocumentDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.PaymentRepository.PaymentDetailsRepository;
import com.application.main.Service.EncDecService;
import com.application.main.Service.FileUploadService;
import com.application.main.config.awsconfig.AwsConfigService.AWSClientConfigService;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;
import com.application.main.model.PoDTO;
import com.application.main.model.PoSummary;

import jakarta.servlet.http.HttpServletRequest;

@RestController
//	@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@RequestMapping("/call/vendor/Vendorportal")
public class POController {

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	PaymentDetailsRepository paymentrepo;

	@Autowired
	FileUploadService s3service;

	@Autowired
	private AWSClientConfigService s3client;

	@Autowired
	DocumentDetailsRepository docdetailsrepository;

	private final MongoTemplate mongoTemplate;

	@Autowired
	LoginUserRepository vendoruserrepo;
	@Autowired
	InvoiceRepository invoiceRepository;

	@GetMapping("/version")
	public String version() {
		return "vendorPortalVersion: 1";
	}

	public POController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@GetMapping("/testing")
	public LocalDate testing() {
		return LocalDate.now();
	}

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public Page<?> convertStreamToPage(Stream<?> entityStream, int page, int size) {
		List<?> entityList = entityStream.collect(Collectors.toList());
		Pageable pageable = PageRequest.of(page, size);
		return new PageImpl<>(entityList, pageable, entityList.size());
	}

	@PostMapping("/createPO")
	public ResponseEntity<?> createPurchaseOrder(@RequestParam(value = "poNumber") String poNumber,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "poStatus", required = true) String poStatus,
			@RequestParam(value = "poIssueDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String poIssueDate,
			@RequestParam(value = "deliveryDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String deliveryDate,
			@RequestParam(value = "poAmount") String poAmount,
			@RequestParam(value = "deliveryTimelines", required = false) String deliveryTimelines,
			@RequestParam(value = "deliveryPlant", required = false) Set<String> deliveryPlant,
			@RequestParam(value = "eic") String eic,
			@RequestParam(value = "receiver", required = false) String receiver,
			@RequestParam(value = "filePO", required = false) MultipartFile filePO, HttpServletRequest request)
			throws Exception {
		if (porepo.existsByPoNumber(poNumber))
			return ResponseEntity.ok("Po Number " + poNumber + " Already Exists");
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = s3service.getUserNameFromToken(token);
		s3service.createBucket(token, username);
		System.out.println("Po Creation Initiated : UserName -> " + username);

		try {
			LocalDate issuedt = LocalDate.parse(poIssueDate, formatter);
			LocalDate delt = LocalDate.parse(deliveryDate, formatter);
			DocumentDetails PoUploadObject = s3service.uploadFile(token, filePO, poNumber, "");
//			DocumentDetails doc = new DocumentDetails(filePO.getOriginalFilename(), poNumber, PoUploadObject.get("generatedURL").toString(),(SecretKey) PoUploadObject.get("secretkey"));

			PoSummary ps = new PoSummary(poStatus, poNumber, description, issuedt, delt, deliveryPlant,
					deliveryTimelines, 0, eic, poAmount, receiver, username, PoUploadObject.getUrl());
			System.out.println("------------------------------------");
			ps.setUsername(username);
			System.err.println("PO File URL : " + PoUploadObject.getUrl());
			ps.setDoc(PoUploadObject);
			porepo.save(ps);
			System.err.println("Po Creation Successfully Ended ! ");
		} catch (Exception e) {
			System.out.println("Exception Found");
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
		return ResponseEntity.ok("Po Creation Successfully Ended ! , saved" + HttpStatus.ACCEPTED);
	}

	@GetMapping("/getPOdeliveryplants")
	public Set<?> getAlldeliveryplants(@RequestHeader(value = "poNumber") String poNumber,
			@RequestParam(value = "deliveryplant") String s) {
		Optional<PoSummary> po = porepo.findByPoNumber(poNumber);
		if (!po.isPresent()) {
			System.out.println("Failed to fetch");
			return Set.of(HttpStatus.NOT_FOUND, "NO PO FOUND !");
		}
		return po.get().getDeliveryPlant().stream().filter(x -> x.contains(s)).collect(Collectors.toSet());
	}

	@GetMapping("/GetPo")
	public ResponseEntity<?> getPurchaseOrder(@RequestParam(value = "poNumber") String ponumber) {
		System.out.println("Getting Purchase Order");
		Optional<PoSummary> po = porepo.findByPoNumber(ponumber);
		if (!po.isPresent())
			return ResponseEntity.ok(HttpStatus.NOT_FOUND);

		PoDTO podto = new PoDTO(po.get().getId(), po.get().getPoNumber(), po.get().getDescription(),
				po.get().getPoIssueDate(), po.get().getDeliveryDate(), po.get().getPoStatus(), po.get().getPoAmount(),
				po.get().getNoOfInvoices(), po.get().getDeliveryTimelines(), po.get().getDeliveryPlant(),
				po.get().getEic(), po.get().getReceiver(), po.get().getUrl());
		return ResponseEntity.ok(podto);

	}

	@GetMapping("/poSummary/invoiceagainstpo")
	public List<InvoiceDTO> getInvoices(@RequestParam(value = "poNumber") String poNumber) {
		try {
			Optional<PoSummary> po = porepo.findByPoNumber(poNumber);
			if (po.isEmpty() || po.get().getInvoiceidlist().isEmpty())
				return List.of();
			Map<String, String> invoicemap = po.get().getInvoiceidlist();

			List<Invoice> invoicelist = invoicemap.entrySet().stream()
					.map(entry -> invoiceRepository.findByIdAndInvoiceNumber(entry.getKey(), entry.getValue()))
					.flatMap(Optional::stream).collect(Collectors.toList());

//		List<Invoice> invoicelist = new ArrayList<>();
//		   for (Map.Entry<String, String> entry : invoicemap.entrySet()) {
//			   Optional<Invoice> iv = invoiceRepository.findByIdAndInvoiceNumber(entry.getKey(), entry.getValue());
//			   if(iv.isPresent()) invoicelist.add( iv.get());
//		   }
			return invoicelist.stream()
					.map(iv -> new InvoiceDTO(iv.getId(), iv.getPoNumber(), iv.getInvoiceNumber(), iv.getInvoiceDate(),
							iv.getStatus(), iv.getDeliveryPlant(), iv.getMobileNumber(), iv.getEic(), null,
							iv.getPaymentType(), iv.getInvoiceurl(), iv.getInvoiceAmount(), iv.getLatestRecievingDate(),
							iv.getLatestforwardDate(), iv.getLatestforwardTime(), iv.getLatestRecievedTime()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			return List.of();
		}
	}

	@GetMapping("/poSummary/getSummary")
	public Page<PoDTO> searchPO(@RequestHeader(value = "Filterby", required = false) String poStatus,
			@RequestHeader(value = "Fromdate") String fromdate, @RequestHeader(value = "Todate") String todate,
			@RequestHeader(value = "Search", required = false) String searchItems,
			@RequestHeader(value = "Username", required = true) String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0") int page,
			@RequestHeader(value = "pageSize", defaultValue = "10") int size) {
		Page<PoDTO> poDTOpage = null;
		Page<PoSummary> purchaseorderpage;
		List<PoSummary> purchaseorders = null;
		try {
			Criteria criteria = new Criteria().where("username").is(username);
			if (!poStatus.equalsIgnoreCase("All")) {
				Pattern statusPattern = Pattern.compile(poStatus, Pattern.CASE_INSENSITIVE);
				criteria = criteria.and("poStatus").regex(statusPattern);
			}

			if (searchItems != null && !searchItems.isEmpty()) {
				criteria = criteria.andOperator(new Criteria().orOperator(Criteria.where("poNumber").regex(searchItems),
						Criteria.where("deliveryTimelines").regex(searchItems),
						Criteria.where("description").regex(searchItems),
						Criteria.where("deliveryPlant").regex(searchItems),
						Criteria.where("poStatus").regex(searchItems), Criteria.where("eic").regex(searchItems),
						Criteria.where("remarks").regex(searchItems), Criteria.where("receiver").regex(searchItems),
						Criteria.where("poAmount").regex(searchItems), Criteria.where("createdBy").regex(searchItems)));
			}

			purchaseorders = mongoTemplate.find(Query.query(criteria), PoSummary.class);
			purchaseorders.forEach(System.out::println);
			System.out.println("000-------------------------------------------000");
			List<PoSummary> purchaseordersbydate = porepo.findByPoIssueDateBetween(LocalDate.parse(fromdate, formatter),
					LocalDate.parse(todate, formatter));
			purchaseorders = purchaseorders.stream().filter(obj1 -> purchaseordersbydate.stream()
					.anyMatch(obj2 -> obj2.getPoNumber().equals(obj1.getPoNumber()))).toList();
			System.out.println("-$$$$$$$$$$$$$$$$$$$---------Printing Filtered List-----------$$$$$$$$$$$$$$----");
			System.out.println();
			System.out.println();
			if (purchaseorders == null)
				return null;
			System.out.println("---------------------------------------");
			purchaseorders.forEach(System.out::println);
			purchaseorderpage = convertListToPage(purchaseorders, page, size);
			System.out.println("---------Converting PO to PODTO---------");
			poDTOpage = convertPoAsPODTO(purchaseorderpage);
			return poDTOpage;
		} catch (Exception e) {
			System.out.println("---------Exception --------- " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	private Page<PoDTO> convertPoAsPODTO(Page<PoSummary> purchaseorderpage) {
		System.out.println("---------   CONVERTING PAGE PO TO PAGE PO DTO   -----------------------------");
		if (purchaseorderpage == null || purchaseorderpage.isEmpty()) {
			System.out.println("------------NULL CASE -----");
			return null;
		}
		Page<PoDTO> pagepodto = purchaseorderpage.map(po -> new PoDTO(po.getId(), po.getPoNumber(), po.getDescription(),
				po.getPoIssueDate(), po.getDeliveryDate(), po.getPoStatus(), po.getPoAmount(), po.getNoOfInvoices(),
				po.getDeliveryTimelines(), po.getDeliveryPlant(), po.getEic(), po.getReceiver(), po.getUrl()));
		System.out.println("--------PRINTING PAGE PODTO CONTENTS AS LIST----------");
		System.out.println();
		System.out.println();
		pagepodto.getContent().forEach(System.out::println);
		System.out.println("---------------- PO AS PODTO CONVERTED SUCCESSFULLY------------");
		return pagepodto;
	}

	private Page<PoSummary> convertListToPage(List<PoSummary> purchaseorders, int page, int size) {
		System.out.println("------------------    CONVERTING LIST TO PAGE    ---------------");
		if (purchaseorders == null || purchaseorders.isEmpty())
			return null;
		Pageable pageable = PageRequest.of(page, size, Sort.by("poIssueDate").descending());
		int start = Math.min((int) pageable.getOffset(), purchaseorders.size());
		int end = Math.min((start + pageable.getPageSize()), purchaseorders.size());
		List<PoSummary> subList = purchaseorders.subList(start, end);
		System.out.println("--------SUCCESFULLY CONVERTED LIST TO PAGE --------");
		return new PageImpl<>(subList, pageable, purchaseorders.size());
	}

	@GetMapping("/getFileURLObject")
	public ResponseEntity<?> getObject(@RequestHeader(value = "url") String url,
			@RequestHeader("Authorization") String token) throws Exception {
		if (url == null)
			return ResponseEntity.ok("");
		token = token.replace("Bearer ", "");
		Optional<DocumentDetails> existingdoc = docdetailsrepository.findByUrl(url);
		if (!existingdoc.isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		String key = existingdoc.get().getBase64Encodedsecretkey();
		SecretKey secretkey = convertBase64ToSecretKey(key);

		url = new EncDecService().decrypt(url, secretkey);
		int index = url.indexOf("XCIDHK2788k99BBSEEL99");

		String bucketname = url.substring(0, index);
		String filename = url.substring(index + "XCIDHK2788k99BBSEEL99".length());
		System.out.println("Bucketname : ############################### " + bucketname);
		System.out.println("filename : ############################### " + filename);
		AmazonS3 s3 = s3client.awsClientConfiguration(token);
		InputStream inputStream = s3.getObject(bucketname, filename).getObjectContent();
		byte arr[] = IOUtils.toByteArray(inputStream);
		ByteArrayResource resource = new ByteArrayResource(arr);
		System.out.println("url : " + url + " : FILENAME " + filename);

		return ResponseEntity.ok().contentLength(arr.length).header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + filename + "\"").body(resource);

	}

	public static SecretKey convertBase64ToSecretKey(String base64Key) {
		byte[] decodedKey = Base64.getDecoder().decode(base64Key);
		SecretKey sec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		System.err.println(sec);
		return sec;
	}

}
