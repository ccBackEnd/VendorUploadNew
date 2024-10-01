package com.application.main.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.application.main.Repositories.DocumentDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.config.awsconfig.AwsConfigService.AWSClientConfigService;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.NotificationModel.VendorPortalNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FileUploadService {

//	@Value("${bucket_name}")
	private String bucketName;
	@Value("${minio_url}")
	private String minioUrl;
	@Autowired
	private AWSClientConfigService s3client;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	InvoiceRepository invoicerepository;

	@Autowired
	LoginUserRepository loginrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocumentDetailsRepository documentDetailsRepository;

	KafkaTemplate<String, Invoice> ktemplate;
	KafkaTemplate<String, VendorPortalNotification> notificationtemplate;
	Logger logApp = LoggerFactory.getLogger(FileUploadService.class);

	private String id;
	private String fileName;
	private String fileURL;

	public static final String ALGORITHM = "AES";
	public static final int KEY_SIZE = 256;

	private AmazonS3 awsClient;

	public void createBucket(String token, String folderappend) {
		this.bucketName = "vendorportalfiles" + folderappend;
		try {
			this.awsClient = s3client.awsClientConfiguration(token);
			if (!awsClient.doesBucketExistV2(bucketName))
				awsClient.createBucket(bucketName);
		} catch (AmazonS3Exception e) {
			logApp.error("AMAZON AWS : EXCEPTION IN BUCKET CREATION");
			e.printStackTrace();
		}
	}

	public String getUserNameFromToken(String token) {
		String tokenBody = token.split("\\.")[1];
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(tokenBody));
		System.out.println("Getting Username from payload");
		return getFieldFromJson(payload, "preferred_username");
	}

	private String getFieldFromJson(String json, String fieldName) {
		String fieldValue = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);
			System.out.println(jsonNode.toPrettyString());
			fieldValue = jsonNode.get(fieldName).asText();
			System.out.println("------------------------------------");
			System.out.println(fieldName + " : " + fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fieldValue;
	}

	public DocumentDetails uploadFile(String token, MultipartFile file, String id, String username)
			throws IOException, Exception {
		String fileName = id.concat("?#" + file.getOriginalFilename());
		this.id = id;
		this.fileName = fileName;
//		AmazonS3 awsClient = s3client.awsClientConfiguration(token);

		if (file == null || file.isEmpty()) {
			logApp.error("File is null");
			return new DocumentDetails();
		} else if (awsClient.doesObjectExist(bucketName, fileName)) {
			logApp.error("FILE WITH " + file.getOriginalFilename() + " Already Exists !");
			logApp.error("-------------Deleting Older file without permission-------------");
			awsClient.deleteObject(bucketName, fileName);
		}
		logApp.info("UPLOADING FILES ................");

		InputStream fileinputstream = file.getInputStream();
		awsClient.putObject(bucketName, fileName, fileinputstream, getObjectMeta(file));
		String invoiceFileUrl = bucketName + "XCIDHK2788k99BBSEEL99" + fileName;
		DocumentDetails details = generateSecretKey(invoiceFileUrl);
		documentDetailsRepository.save(details);
		return details;

	}

	private ObjectMetadata getObjectMeta(MultipartFile file) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file.getSize());
		return objectMetadata;
	}

	public DocumentDetails generateSecretKey(String invoiceFileUrl) throws Exception {
		logApp.info("GENERATING SECRETKEY...");
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_SIZE);
		SecretKey secretkey = keyGenerator.generateKey();
		invoiceFileUrl = new EncDecService().encrypt(invoiceFileUrl, secretkey);
		this.fileURL = invoiceFileUrl;
		String EncodedSecretKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
		logApp.info("SecretKey is Generated");
		return createDocumentdetails(EncodedSecretKey);
	}

	public DocumentDetails createDocumentdetails(String encodedSecretKey) {
		logApp.info("Generating DocumentDetails...");
		System.out.println("Filename : " + fileName);
		System.out.println("ID : " + id);
		System.out.println("fileURL : " + fileURL);
		return new DocumentDetails(fileName, id, fileURL, encodedSecretKey);
	}
}
