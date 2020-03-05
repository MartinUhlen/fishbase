package se.martinuhlen.fishbase.google.drive;

import static java.util.Arrays.asList;
import static se.martinuhlen.fishbase.utils.Checked.$;
import static se.martinuhlen.fishbase.utils.Checked.get;
import static se.martinuhlen.fishbase.utils.Constants.APPLICATION_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.model.File;

import se.martinuhlen.fishbase.utils.Logger;

public class DriveService
{
	private static final Logger LOGGER = Logger.getLogger(DriveService.class);
	private static final String MIMETYPE_FOLDER = "application/vnd.google-apps.folder";

	private final Drive drive;

	private File applicationFolder;

	public DriveService(Drive drive)
	{
		this.drive = drive;
	}

	public void upload(String name, InputStream input)
	{
		InputStreamContent content = new InputStreamContent(null, input);
		findFile(name)
  		.ifPresentOrElse(
  				$(file -> updateFile(file, content)),
  				$(() -> insertFile(name, content)));
	}

	private void updateFile(File file, AbstractInputStreamContent content) throws IOException
	{
		log("Starting update of '" + file.getName() + "'");
		Update update = drive.files().update(file.getId(), null, content);
		update.getMediaHttpUploader().setDirectUploadEnabled(true);
		update.execute();
		log("Finished updating '" + file.getName() + "'");
	}

	private void insertFile(String name, AbstractInputStreamContent content) throws IOException
	{
		log("Starting insert of '" + name + "'");
	    File file = new File();
	    file.setName(name);
	    file.setParents(asList(getApplicationFolder().getId()));
		drive.files().create(file, content).execute();
		log("Finished inserting '" + name + "'");
	}

	public void download(String name, OutputStream output)
	{
		findFile(name)
			.ifPresentOrElse(
				$(f ->
		  		{
		  			log("Starting download of '" + name + "'");
		  			try (output)
		  			{
		  				drive.files().get(f.getId()).executeMediaAndDownloadTo(output);
		  				log("Finished downloading '" + name + "'");
		  			}
		  		}),
				$(() -> output.close()));
	}

	private Optional<File> findFile(String name)
	{
		log("Searching for file '" + name + '"');
		Optional<File> file = get(() -> drive.files()
				.list()
				.setQ("name='"+name+"' and parents in '"+getApplicationFolder().getId()+"' and trashed=false")
				.execute()
				.getFiles()
				.stream()
				.findAny());

		if (file.isPresent())
		{
			log("Found file '" + name + "'");
		}
		else
		{
			log("File not found: '" + name + "'");
		}
		return file;
	}

	private void log(String message)
	{
		LOGGER.log(message);
	}

	private synchronized File getApplicationFolder()
	{
		if (applicationFolder == null)
		{
			applicationFolder = get(() -> drive.files()
					.list()
					.setQ("name = '" + APPLICATION_NAME + "' and trashed = false and mimeType = '" + MIMETYPE_FOLDER + "'")
					.setFields("files(id, name)")
					.execute())
						.getFiles()
						.stream()
						.findAny()
						.orElseGet($(() ->
						{
							File file = new File()
									.setName(APPLICATION_NAME)
									.setMimeType(MIMETYPE_FOLDER);
							return drive.files().create(file).execute();
						}));
		}
		return applicationFolder;
	}
}
