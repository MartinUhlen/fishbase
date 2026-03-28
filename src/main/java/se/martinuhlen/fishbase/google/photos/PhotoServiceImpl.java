package se.martinuhlen.fishbase.google.photos;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static se.martinuhlen.fishbase.dao.PersistenceDirectory.PHOTOS;
import static se.martinuhlen.fishbase.dao.PersistenceDirectory.THUMBNAILS;
import static se.martinuhlen.fishbase.utils.Checked.apply;
import static se.martinuhlen.fishbase.utils.Checked.run;

import com.google.common.flogger.FluentLogger;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.google.drive.DriveService;
import se.martinuhlen.fishbase.google.photos.PickerClient.PickerSession;
import se.martinuhlen.fishbase.google.photos.data.PhotoDataFactory;

/**
 * Default implementation of {@link PhotoService}.
 *
 * @author martin
 */
class PhotoServiceImpl implements PhotoService {
    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();
    private static final long TIMEOUT_MS = 30L * 60L * 1000L; // 30 minutes
    private static final ExecutorService UPLOAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final PickerClient pickerClient;
    private final DriveService driveService;
    private final PhotoDataFactory photoDataFactory;

    PhotoServiceImpl(PickerClient pickerClient, DriveService driveService) {
        this.pickerClient = pickerClient;
        this.driveService = driveService;
        this.photoDataFactory = new PhotoDataFactory(driveService);
    }

    @Override
    public List<FishingPhoto> load(List<Photo> photos) {
        requireNonNull(photos, "photos cannot be null");
        if (photos.isEmpty()) {
            return List.of();
        } else {
            LOG.atInfo().log("Loading %d photos", photos.size());
            return photos
                    .stream()
                    .map(photo -> new FishingPhotoImpl(photo, photoDataFactory::get))
                    .collect(toList());
        }
    }

    @Override
    public List<GooglePhoto> pick() {
        try {
            LOG.atInfo().log("Creating picker session");
            PickerSession session = pickerClient.createSession();
            LOG.atInfo().log("Picker session created: %s, opening browser at %s", session.id(), session.pickerUri());

            Desktop.getDesktop().browse(URI.create(session.pickerUri()));

            long pollIntervalMs = session.pollIntervalMs();
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                Thread.sleep(pollIntervalMs);
                LOG.atInfo().log("Polling picker session %s", session.id());
                if (pickerClient.isSelectionDone(session.id())) {
                    LOG.atInfo().log("Selection done, listing media items");
                    List<PickedPhoto> items = pickerClient.listMediaItems(session.id());
                    LOG.atInfo().log("Got %d media items", items.size());
                    try {
                        pickerClient.deleteSession(session.id());
                        LOG.atInfo().log("Picker session deleted");
                    }
                    catch (Exception e) {
                        LOG.atWarning().withCause(e).log("Failed to delete picker session");
                    }
                    return List.copyOf(items);
                }
            }
            LOG.atWarning().log("Picker session timed out after 30 minutes");
            try {
                pickerClient.deleteSession(session.id());
            }
            catch (Exception e) {
                LOG.atWarning().withCause(e).log("Failed to delete timed-out picker session");
            }
            return emptyList();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.atInfo().log("Picker interrupted");
            return emptyList();
        }
        catch (Exception e) {
            LOG.atSevere().withCause(e).log("Picker failed");
            throw new RuntimeException("Failed to pick photos", e);
        }
    }

    @Override
    public List<FishingPhoto> createAll(Collection<? extends GooglePhoto> photos, String tripId) {
        List<Future<FishingPhoto>> futurePhotos = photos
                .stream()
                .map(photo -> UPLOAD_EXECUTOR.submit(() -> create(photo, tripId)))
                .toList();

        return futurePhotos
                .stream()
                .map(apply(Future::get))
                .toList();
    }

    @Override
    public FishingPhoto create(GooglePhoto photo, String tripId) {
        Photo domain = Photo.asNew(photo.getId())
                .tripId(tripId)
                .specimens(emptySet())
                .fileName(photo.getName())
                .time(photo.getTime())
                .starred(false);

        Future<?> futurePhoto = UPLOAD_EXECUTOR.submit(() -> driveService.upload(PHOTOS, photo.getContentFileName(), photo.getContent().getStream()));
        Future<?> futureThumbnail = UPLOAD_EXECUTOR.submit(() -> driveService.upload(THUMBNAILS, photo.getThumbnailFileName(), photo.getThumbnail().getStream()));
        run(() -> futurePhoto.get());
        run(() -> futureThumbnail.get());

        return new FishingPhotoImpl(domain, _ -> photo.getContent(), _ -> photo.getThumbnail());
    }
}
