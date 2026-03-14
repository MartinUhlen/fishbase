package se.martinuhlen.fishbase.google.photos;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * HTTP client for the Google Photos Picker API.
 *
 * @author Martin
 */
public class PickerClient
{
	private static final String BASE_URL = "https://photospicker.googleapis.com/v1";

	/**
	 * A session returned by the Picker API.
	 */
	record PickerSession(String id, String pickerUri, long pollIntervalMs) {}

	private final Supplier<String> accessToken;

	public PickerClient(Supplier<String> accessToken)
	{
		this.accessToken = accessToken;
	}

	/**
	 * Creates a new picker session.
	 *
	 * @return the new session
	 * @throws IOException on network or HTTP errors
	 */
	PickerSession createSession() throws IOException
	{
		HttpURLConnection conn = openConnection(BASE_URL + "/sessions", "POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		try (OutputStream os = conn.getOutputStream())
		{
			os.write("{}".getBytes());
		}
		JsonObject json = readResponse(conn);
		String id = json.get("id").getAsString();
		String pickerUri = json.get("pickerUri").getAsString();
		long pollIntervalMs = parsePollInterval(json);
		return new PickerSession(id, pickerUri, pollIntervalMs);
	}

	/**
	 * Polls the session to check if the user has finished selecting media items.
	 *
	 * @param sessionId the session ID
	 * @return {@code true} if media items have been set (user finished picking)
	 * @throws IOException on network or HTTP errors
	 */
	boolean isSelectionDone(String sessionId) throws IOException
	{
		HttpURLConnection conn = openConnection(BASE_URL + "/sessions/" + sessionId, "GET");
		JsonObject json = readResponse(conn);
		JsonElement field = json.get("mediaItemsSet");
		return field != null && field.getAsBoolean();
	}

	/**
	 * Lists all media items picked in a session, handling pagination.
	 *
	 * @param sessionId the session ID
	 * @return list of picked photos
	 * @throws IOException on network or HTTP errors
	 */
	List<PickerGooglePhoto> listMediaItems(String sessionId) throws IOException
	{
		List<PickerGooglePhoto> result = new ArrayList<>();
		String pageToken = null;
		do
		{
			StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/mediaItems?sessionId=" + sessionId + "&pageSize=100");
			if (pageToken != null)
			{
				urlBuilder.append("&pageToken=").append(pageToken);
			}
			HttpURLConnection conn = openConnection(urlBuilder.toString(), "GET");
			JsonObject json = readResponse(conn);

			JsonArray items = json.has("mediaItems") ? json.getAsJsonArray("mediaItems") : null;
			if (items != null)
			{
				for (JsonElement element : items)
				{
					JsonObject item = element.getAsJsonObject();
					result.add(parseMediaItem(item));
				}
			}

			pageToken = json.has("nextPageToken") ? json.get("nextPageToken").getAsString() : null;
		}
		while (pageToken != null);
		return result;
	}

	/**
	 * Deletes a picker session.
	 *
	 * @param sessionId the session ID
	 * @throws IOException on network or HTTP errors
	 */
	void deleteSession(String sessionId) throws IOException
	{
		HttpURLConnection conn = openConnection(BASE_URL + "/sessions/" + sessionId, "DELETE");
		int code = conn.getResponseCode();
		if (code >= 400)
		{
			throw new IOException("DELETE /sessions/" + sessionId + " returned HTTP " + code);
		}
	}

	private HttpURLConnection openConnection(String urlString, String method) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Authorization", "Bearer " + accessToken.get());
		conn.setRequestProperty("Accept", "application/json");
		return conn;
	}

	private JsonObject readResponse(HttpURLConnection conn) throws IOException
	{
		int code = conn.getResponseCode();
		if (code >= 400)
		{
			String body = "";
			try (java.io.InputStream err = conn.getErrorStream())
			{
				if (err != null)
				{
					body = ": " + new String(err.readAllBytes());
				}
			}
			catch (Exception ignored) {}
			throw new IOException("HTTP " + code + " for " + conn.getURL() + body);
		}
		try (InputStreamReader reader = new InputStreamReader(conn.getInputStream()))
		{
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private PickerGooglePhoto parseMediaItem(JsonObject item)
	{
		String id = item.get("id").getAsString();
		String createTimeStr = item.has("createTime") ? item.get("createTime").getAsString() : null;
		LocalDateTime createTime;
		if (createTimeStr != null)
		{
			createTime = Instant.parse(createTimeStr).atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
		else
		{
			createTime = LocalDateTime.now();
		}

		String type = item.has("type") ? item.get("type").getAsString() : "PHOTO";
		boolean isVideo = "VIDEO".equalsIgnoreCase(type);

		JsonObject mediaFile = item.has("mediaFile") ? item.getAsJsonObject("mediaFile") : new JsonObject();
		String baseUrl = mediaFile.has("baseUrl") ? mediaFile.get("baseUrl").getAsString() : "";
		String filename = mediaFile.has("filename") ? mediaFile.get("filename").getAsString() : id;

		return new PickerGooglePhoto(id, filename, createTime, isVideo, baseUrl, accessToken);
	}

	private long parsePollInterval(JsonObject json)
	{
		if (!json.has("pollingConfig"))
		{
			return 2000L;
		}
		JsonObject pollingConfig = json.getAsJsonObject("pollingConfig");
		if (!pollingConfig.has("pollInterval"))
		{
			return 2000L;
		}
		String raw = pollingConfig.get("pollInterval").getAsString().trim();
		try
		{
			if (raw.endsWith("ms"))
			{
				return Long.parseLong(raw.substring(0, raw.length() - 2).trim());
			}
			else if (raw.endsWith("s"))
			{
				return Long.parseLong(raw.substring(0, raw.length() - 1).trim()) * 1000L;
			}
			else
			{
				return Long.parseLong(raw);
			}
		}
		catch (NumberFormatException e)
		{
			return 2000L;
		}
	}
}
