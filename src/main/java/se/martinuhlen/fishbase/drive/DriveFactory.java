package se.martinuhlen.fishbase.drive;

import static java.util.Arrays.asList;
import static se.martinuhlen.fishbase.utils.Constants.APPLICATION_NAME;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveFactory
{
	private static final String USER_ID = "user";
	private static final java.io.File LOCAL_FOLDER = new java.io.File(System.getProperty("user.home"), "." + APPLICATION_NAME.toLowerCase());

	public static DriveFactory get()
	{
		try
		{
			return getImpl();
		}
		catch (GeneralSecurityException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static DriveFactory getImpl() throws GeneralSecurityException, IOException
	{
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(LOCAL_FOLDER);
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(DriveFactory.class.getResourceAsStream("/client_secrets.json")));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes())
				.setDataStoreFactory(dataStoreFactory)
				.build();

		Credential credential = flow.loadCredential(USER_ID);
		if (credential == null)
		{
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(USER_ID);
		}

		return new DriveFactory(flow.getTransport(), flow.getJsonFactory(), credential);
	}

	/**
	 * Gets the scopes we're using.
	 * <p>
	 * It should've been enough with DRIVE_FILE (FishBase json files) and DRIVE_PHOTOS_READONLY.
	 * But DRIVE_PHOTOS_READONLY doesn't work without DRIVE.
	 *
	 * @return used Google Drive scopes
	 */
	private static Collection<String> scopes()
	{
		return asList(
			DriveScopes.DRIVE,
			DriveScopes.DRIVE_FILE,
			DriveScopes.DRIVE_PHOTOS_READONLY);
	}

	private final HttpTransport httpTransport;
	private final JsonFactory jsonFactory;
	private final HttpRequestInitializer httpRequestInitializer;

	private DriveFactory(HttpTransport httpTransport, JsonFactory jsonFactory, HttpRequestInitializer httpRequestInitializer)
	{
		this.httpTransport = httpTransport;
		this.jsonFactory = jsonFactory;
		this.httpRequestInitializer = httpRequestInitializer;
	}

	public Drive create()
	{
		return new Drive.Builder(httpTransport, jsonFactory, httpRequestInitializer)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
}
