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
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

/**
 * 
 * @author devan
 *
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

			// Make the container public
			// Create a permissions object
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Exclude public access in the permissions object
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);

			// Set the permissions on the container
			container.uploadPermissions(containerPermissions);

			// Set the permission for SAS and expire time as 1 hour from now (3600 milli seconds)
			SharedAccessBlobPolicy sp = createSharedAccessPolicy(
					EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 3600);

			// Get a reference to a blob in the container
			CloudBlockBlob blob = container.getBlockBlobReference("devanBlobSample");

			// Upload text to the blob
			blob.uploadText("Hello, SAS");
			// sas token
			String sasToken = blob.generateSharedAccessSignature(sp, null);
			System.out.println(sasToken);

			String blobURLWithSas = MessageFormat.format("{0}?{1}", blob.getUri().toURL(), sasToken);
			System.out.println(blobURLWithSas);
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