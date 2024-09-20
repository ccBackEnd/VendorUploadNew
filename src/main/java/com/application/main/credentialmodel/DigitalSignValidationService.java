package com.application.main.credentialmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.SignatureUtil;

@Service
public class DigitalSignValidationService {

	Logger logApp = LoggerFactory.getLogger(DigitalSignValidationService.class);
	private MultipartFile file;

	private byte[] getByteRangeData(ByteArrayInputStream bis, int[] byteRange) {
		int length1 = byteRange[1] + byteRange[3];
		byte[] contentSigned = new byte[length1];
		bis.skip(byteRange[0]);
		bis.read(contentSigned, 0, byteRange[1]);
		bis.skip(byteRange[2] - byteRange[1] - byteRange[0]);
		bis.read(contentSigned, byteRange[1], byteRange[3]);
		bis.reset();
		return contentSigned;

	}

	public boolean verify(MultipartFile file) throws IOException {
		this.file = file;
		try (
			PdfReader pdfReader = new PdfReader(file.getInputStream()); 
			PdfDocument doc = new PdfDocument(pdfReader)) {
			SignatureUtil signUtil = new SignatureUtil(doc);
			List<String> signatureNames = signUtil.getSignatureNames();

			if (signatureNames == null || signatureNames.isEmpty()) {
				System.out.println("IS NOT SIGNED");
				return false;
			}

			// Check for digital signature validity
			boolean hasValidDigitalSignature = digitalverification();

			if (hasValidDigitalSignature) {
				System.out.println("VALID DIGITAL SIGNATURE");
				return true;
			} else {
				System.out.println("NOT VALID DIGITAL SIGNATURE");
				return false;
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatusCode.valueOf(400),"Error processing the PDF: " + e.getMessage());
		}
	}

	public boolean digitalverification() throws Exception {

		PDDocument pdfDoc = null;
		try {
			ByteArrayInputStream pdfBytes = new ByteArrayInputStream(file.getBytes());
			pdfDoc = PDDocument.load(file.getInputStream());
			pdfDoc.getSignatureDictionaries().forEach(a -> {
				try {
					processSignature(a, pdfBytes);
				} catch (Exception e) {
					logApp.error("Error processing Signature", e);
				}
			});

			pdfBytes.close();
			return true;
		} finally {
			pdfDoc.close();
		}
	}
	
	private boolean isValidSubFilter(String subFilter) {
	    return subFilter.trim().contains("ETSI.CAdES.detached") &&
	           subFilter.trim().contains("adbe.pkcs7.detached") &&
	           subFilter.trim().contains("ETSI.RFC3161");
	}
	@SuppressWarnings("unchecked")
	private void processSignature(PDSignature signature, ByteArrayInputStream pdfBytes) throws Exception {
		byte[] contentToSigned = getByteRangeData(pdfBytes, signature.getByteRange());
		String filter = signature.getFilter();
		String subFilter = signature.getSubFilter();
		String contactInfo = Optional.ofNullable(signature.getContactInfo()).orElse("N/A");
		String reason = Optional.ofNullable(signature.getReason()).orElse("N/A");

		if (!filter.trim().equalsIgnoreCase("Adobe.PPKLite")) {
			logApp.error("Cannot process PDF Signature {} with filter {}", signature.getName(), filter);
			return ;
		}
		if (!isValidSubFilter(subFilter)) {
			logApp.error("Cannot process PDF Signature {} with subFilter {}", signature.getName(), subFilter);
			return ;
		}
		logApp.info("Signature {} Filter:{}", signature.getName(), filter);
		logApp.info("Signature {} SubFilter:{}", signature.getName(), subFilter);
		logApp.info("Signature {} ContactInfo:{}", signature.getName(), contactInfo);
		logApp.info("Signature {} Reason:{}", signature.getName(), reason);

		// Get PKCS#7 Data
		CMSSignedData signedData = new CMSSignedData(signature.getContents());
		// Get SignerInfo
		SignerInformation signerInfo = signedData.getSignerInfos().iterator().next();

		// Get Attribute
		Attribute attribute1 = signerInfo.getSignedAttributes().get(PKCSObjectIdentifiers.pkcs_9_at_messageDigest);
		Attribute attribute2 = null;
		if (signerInfo.getUnsignedAttributes() != null) {
			attribute2 = signerInfo.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
		}

		// Get MD in CMS
		String messageDigest = "";
		if (subFilter.contains("ETSI.RFC3161")) {
			TimeStampToken timeToken = new TimeStampToken(signedData);
			messageDigest = Base64.getEncoder().encodeToString(timeToken.getTimeStampInfo().getMessageImprintDigest());
		} else {
			messageDigest = Base64.getEncoder()
					.encodeToString(Hex.decode(attribute1.getAttributeValues()[0].toString().substring(1)));
		}
		MessageDigest digest = MessageDigest.getInstance(signerInfo.getDigestAlgOID());
		logApp.info("Digest Algorithm used:{}", digest.getAlgorithm());

		String signatureSID = signerInfo.getSID().getSerialNumber().toString(16);
		// Check timestamp token
		if (attribute2 != null && attribute2.getAttributeValues().length > 0) {
			logApp.info("Signature ID {} contains timestamp", signatureSID);
		}

		// Getting PublicKey
		Collection<X509CertificateHolder> matches = signedData.getCertificates().getMatches(signerInfo.getSID());
		byte[] pubByte = matches.iterator().next().getSubjectPublicKeyInfo().getEncoded();

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubByte);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pubKey = kf.generatePublic(keySpec);
		String encAlgo = null;
		if (signerInfo.getEncryptionAlgOID().trim().equals("1.2.840.113549.1.1.1")) {
			encAlgo = "RSA";
		}
		if (encAlgo != null) {
			if (digest.getAlgorithm().equals("1.3.14.3.2.26")) {
				encAlgo = "SHA1withRSA";
			} else if (digest.getAlgorithm().equals("2.16.840.1.101.3.4.2.1")) {
				encAlgo = "SHA256withRSA";
			} else if (digest.getAlgorithm().equals("2.16.840.1.101.3.4.2.2")) {
				encAlgo = "SHA384withRSA";
			} else if (digest.getAlgorithm().equals("2.16.840.1.101.3.4.2.3")) {
				encAlgo = "SHA512withRSA";
			}

		} else {
			encAlgo = signerInfo.getEncryptionAlgOID();
		}
		Signature rsaSign = Signature.getInstance(encAlgo);
		rsaSign.initVerify(pubKey);
		rsaSign.update(signerInfo.getEncodedSignedAttributes());
		boolean cmsSignatureValid = rsaSign.verify(signerInfo.getSignature());

		if (cmsSignatureValid) {
			logApp.info("Signature ID {} have VALID CMS Signature", signatureSID);
		} else {
			logApp.error("Signature ID {} have INVALID CMS Signature", signatureSID);
		}

		// Calculate MD in PDF

		String mdPdf = Base64.getEncoder().encodeToString(digest.digest(contentToSigned));
		logApp.info("Message Digest Signature ID {} in CMS:{}", signatureSID, messageDigest);
		logApp.info("Message Digest Signature ID {} in PDF:{}", signatureSID, mdPdf);

		if (mdPdf.equals(messageDigest)) {
			logApp.info("Message Digest Signature ID {} is valid, data integrity is OK", signatureSID);
		} else {
			logApp.info("Message Digest Signature ID {} is invalid, data integrity is NOT OK", signatureSID);
		}

	}
}
