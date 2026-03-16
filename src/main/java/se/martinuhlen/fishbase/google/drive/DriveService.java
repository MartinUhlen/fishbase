package se.martinuhlen.fishbase.google.drive;

import static java.util.Arrays.asList;
import static se.martinuhlen.fishbase.utils.Checked.$;
import static se.martinuhlen.fishbase.utils.Checked.get;
import static se.martinuhlen.fishbase.utils.Checked.run;
import static se.martinuhlen.fishbase.utils.Constants.APPLICATION_NAME;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.model.File;
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import se.martinuhlen.fishbase.dao.PersistenceDirectory;

public class DriveService {

    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();
    private static final String MIMETYPE_FOLDER = "application/vnd.google-apps.folder";

    private final Drive drive;

    private File applicationFolder;
    private final Map<PersistenceDirectory, File> subFolders = new HashMap<>();

    public DriveService(Drive drive) {
        this.drive = drive;
    }

    public void upload(PersistenceDirectory dir, String name, InputStream input) {
        InputStreamContent content = new InputStreamContent(null, input);
        findFile(dir, name).ifPresentOrElse(
                $(file -> updateFile(dir, file, content)),
                $(() -> insertFile(dir, name, content)));
    }

    private void updateFile(PersistenceDirectory dir, File file, AbstractInputStreamContent content) throws IOException {
        LOG.atInfo().log("Starting update of '" + file.getName() + "'");
        Update update = drive.files().update(file.getId(), null, content);
        update.getMediaHttpUploader().setDirectUploadEnabled(true);
        update.execute();
        LOG.atInfo().log("Finished updating '" + file.getName() + "'");
    }

    private void insertFile(PersistenceDirectory dir, String name, AbstractInputStreamContent content) throws IOException {
        LOG.atInfo().log("Starting insert of '" + name + "'");
        File file = new File();
        file.setName(name);
        file.setParents(asList(getSubFolder(dir).getId()));
        drive.files().create(file, content).execute();
        LOG.atInfo().log("Finished inserting '" + name + "'");
    }

    public boolean download(PersistenceDirectory dir, String name, OutputStream output) {
        boolean downloaded = download(dir, name, () -> output);
        run(() -> output.close());
        return downloaded;
    }

    public boolean download(PersistenceDirectory dir, String name, Callable<OutputStream> outputSupplier) {
        File file = findFile(dir, name).orElse(null);
        if (file != null) {
            run(() -> {
                LOG.atInfo().log("Starting download of '" + name + "'");
                try (OutputStream output = outputSupplier.call();) {
                    drive.files().get(file.getId()).executeMediaAndDownloadTo(output);
                    LOG.atInfo().log("Finished downloading '" + name + "'");
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private Optional<File> findFile(PersistenceDirectory dir, String name) {
        Optional<File> file = get(() -> drive.files()
                .list()
                .setQ("name='"+name+"' and parents in '"+getSubFolder(dir).getId()+"' and trashed=false")
                .execute()
                .getFiles()
                .stream()
                .findAny());

        if (file.isPresent()) {
            LOG.atInfo().log("Found file '%s'", name);
        } else {
            LOG.atInfo().log("File not found: '%s'", name);
        }
        return file;
    }


    private synchronized File getSubFolder(PersistenceDirectory dir) {
        return subFolders.computeIfAbsent(dir, d -> findOrCreateFolder(d.name().toLowerCase(), getApplicationFolder().getId()));
    }

    private synchronized File getApplicationFolder() {
        if (applicationFolder == null) {
            applicationFolder = findOrCreateFolder(APPLICATION_NAME, null);
        }
        return applicationFolder;
    }

    private File findOrCreateFolder(String name, String parentId) {
        String parentFilter = parentId != null ? " and parents in '" + parentId + "'" : "";
        return get(() -> drive.files()
                .list()
                .setQ("name = '" + name + "'" + parentFilter + " and trashed = false and mimeType = '" + MIMETYPE_FOLDER + "'")
                .setFields("files(id, name)")
                .execute())
                    .getFiles()
                    .stream()
                    .findAny()
                    .orElseGet($(() -> {
                        File folder = new File()
                                .setName(name)
                                .setMimeType(MIMETYPE_FOLDER);
                        if (parentId != null) {
                            folder.setParents(asList(parentId));
                        }
                        return drive.files().create(folder).execute();
                    }));
    }
}
