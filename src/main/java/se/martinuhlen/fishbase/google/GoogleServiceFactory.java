package se.martinuhlen.fishbase.google;

import static se.martinuhlen.fishbase.utils.Constants.APPLICATION_NAME;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.UserCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

public class GoogleServiceFactory
{
	private static final String USER_ID = "user";
	private static final java.io.File LOCAL_FOLDER = new java.io.File(System.getProperty("user.home"), "." + APPLICATION_NAME.toLowerCase());

	private static final List<String> REQUIRED_SCOPES = List.of(
			//"https://www.googleapis.com/auth/photoslibrary.readonly",
			"https://www.googleapis.com/auth/photoslibrary",
			DriveScopes.DRIVE,
			DriveScopes.DRIVE_FILE,
			DriveScopes.DRIVE_PHOTOS_READONLY);	// FIXME Redundant?

	public static GoogleServiceFactory get()
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
	
	private static GoogleServiceFactory getImpl() throws GeneralSecurityException, IOException
	{
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(LOCAL_FOLDER);
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		GoogleClientSecrets clientSecrets = readClientSecrets(jsonFactory);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, REQUIRED_SCOPES)
				.setDataStoreFactory(dataStoreFactory)
				.build();

		Credential credential = flow.loadCredential(USER_ID);
		if (credential == null)
		{
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(USER_ID);
		}
		return new GoogleServiceFactory(httpTransport, jsonFactory, clientSecrets, credential);
	}

	private static GoogleClientSecrets readClientSecrets(JsonFactory jsonFactory) throws IOException
	{
		try (Reader reader = new InputStreamReader(GoogleServiceFactory.class.getResourceAsStream("/ClientSecrets.json")))
		{
			return GoogleClientSecrets.load(jsonFactory, reader);
		}
	}

	private final HttpTransport httpTransport;
	private final JsonFactory jsonFactory;
	private final GoogleClientSecrets clientSecrets;
	private final Credential credential;

	private GoogleServiceFactory(HttpTransport httpTransport, JsonFactory jsonFactory, GoogleClientSecrets clientSecrets, Credential credential)
	{
		this.httpTransport = httpTransport;
		this.jsonFactory = jsonFactory;
		this.clientSecrets = clientSecrets;
		this.credential = credential;
	}

	public Drive createDrive()
	{
		return new Drive.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public PhotosLibraryClient createPhotosLibraryClient()
	{
		UserCredentials userCredentials = UserCredentials.newBuilder()
				.setClientId(clientSecrets.getDetails().getClientId())
				.setClientSecret(clientSecrets.getDetails().getClientSecret())
				.setRefreshToken(credential.getRefreshToken())
				.build();

		try
		{
			PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(userCredentials))
					.build();
			return PhotosLibraryClient.initialize(settings);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
