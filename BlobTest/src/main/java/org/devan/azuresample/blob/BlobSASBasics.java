package org.devan.azuresample.blob;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobHeaders;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

/**
 * 
 * @author devan Expecting cloud storage connection string as argument
 */
public class BlobSASBasics {

	public static void main(String[] args) throws InvalidKeyException, URISyntaxException {

		// Setup the cloud storage account.
		CloudStorageAccount account = CloudStorageAccount.parse(args[0]);

		// Create a blob service client
		CloudBlobClient blobClient = account.createCloudBlobClient();

		try {

			CloudBlobContainer container = blobClient.getContainerReference("blobsascontainer");

			// Create the container if it does not exist
			container.createIfNotExists();

			// Create a permissions object
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Exclude public access in the permissions object
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);

			// Set the permissions on the container
			container.uploadPermissions(containerPermissions);

			// Set the permission for SAS and expire time as 1 hour from now (3600 milli
			// seconds)
			SharedAccessBlobPolicy sp = createSharedAccessPolicy(
					EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 3600);

			// Get a reference to a blob in the container
			CloudBlockBlob blob = container.getBlockBlobReference("devanBlobSample");

			// Upload text to the blob
			blob.uploadText("Hello, SAS");

			// IP range can access blob SAS url - Change as your requirement
			IPRange ipRange = new IPRange("0.0.0.0", "255.255.255.255");

			// Sample Headers
			SharedAccessBlobHeaders headers = new SharedAccessBlobHeaders();
			headers.setCacheControl("no-cache");
			headers.setContentDisposition("attachment; filename=\"fname.ext\"");
			headers.setContentEncoding("gzip");
			headers.setContentLanguage("da");
			headers.setContentType("text/html; charset=utf-8");

			// sas token
			String sasToken = blob.generateSharedAccessSignature(sp, headers, null, ipRange,
					SharedAccessProtocols.HTTPS_ONLY);

			// String sasToken = blob.generateSharedAccessSignature(sp, null);

			System.out.println(MessageFormat.format("SAS Token : {0}", sasToken));

			String blobURLWithSas = MessageFormat.format("{0}?{1}", blob.getUri().toURL(), sasToken);
			System.out.println(MessageFormat.format("Blob url with SAS : {0}", blobURLWithSas));
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	/**
	 * 
	 * @param sap
	 * @param expireTimeInSeconds
	 * @return
	 */
	private final static SharedAccessBlobPolicy createSharedAccessPolicy(EnumSet<SharedAccessBlobPermissions> sap,
			int expireTimeInSeconds) {

		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, expireTimeInSeconds);
		SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
		policy.setPermissions(sap);
		policy.setSharedAccessExpiryTime(cal.getTime());
		System.out.println(new Date());
		System.out.println(cal.getTime());
		return policy;

	}
}
