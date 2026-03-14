package se.martinuhlen.fishbase.google;

import static se.martinuhlen.fishbase.utils.Constants.APPLICATION_NAME;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import se.martinuhlen.fishbase.google.photos.PickerClient;

public class GoogleServiceFactory
{
	private static final String USER_ID = "user_v3";
	private static final java.io.File LOCAL_FOLDER = new java.io.File(System.getProperty("user.home"), "." + APPLICATION_NAME.toLowerCase());

	private static final List<String> REQUIRED_SCOPES = Stream.concat(
	        Stream.of("https://www.googleapis.com/auth/photospicker.mediaitems.readonly"),
	        DriveScopes.all().stream()).toList();

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
		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
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
		InputStream stream = GoogleServiceFactory.class.getResourceAsStream("/ClientSecrets.json");
		if (stream == null)
		{
			throw new IllegalStateException("ClientSecrets.json is missing from the classpath. Please add it to src/main/resources.");
		}
		try (Reader reader = new InputStreamReader(stream))
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

	public PickerClient createPickerClient()
	{
		Supplier<String> accessToken = () ->
		{
			try
			{
				if (credential.getAccessToken() == null || credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60)
				{
					credential.refreshToken();
				}
				return credential.getAccessToken();
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failed to get access token", e);
			}
		};
		return new PickerClient(accessToken);
	}
}
