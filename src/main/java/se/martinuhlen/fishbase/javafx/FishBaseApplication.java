package se.martinuhlen.fishbase.javafx;

import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.geometry.Side.BOTTOM;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;
import static se.martinuhlen.fishbase.javafx.View.EMPTY_VIEW;
import static se.martinuhlen.fishbase.javafx.utils.Images.getImages;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.drive.DriveFactory;
import se.martinuhlen.fishbase.drive.DrivePersistence;
import se.martinuhlen.fishbase.drive.DriveService;
import se.martinuhlen.fishbase.drive.photo.PhotoService;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.ReplaceableAction;
import se.martinuhlen.fishbase.javafx.utils.Images;

public class FishBaseApplication extends Application
{
	/*
	 * http://fxexperience.com/controlsfx/features/
	 * FontAwesomeFX
	 *
	 * https://developers.google.com/drive/v3/web/about-sdk
	 * https://stackoverflow.com/questions/35143283/google-drive-api-v3-migration
	 *
	 * Use org.controlsfx.control.StatusBar
	 *
	 * Package org.controlsfx.validation
	 *
	 * Fish icon: https://www.iconexperience.com/v_collection/search/?q=fish
	 */

	public static void main(String[] args)
	{
		Application.launch(args);
	}

	private final Map<Tab, View> tabToView = new LinkedHashMap<>();
	private final ReplaceableAction addAction = new ReplaceableAction();
	private final ReplaceableAction saveAction = new ReplaceableAction();
	private final ReplaceableAction refreshAction = new ReplaceableAction();
	private final ReplaceableAction deleteAction = new ReplaceableAction();
	private final Scene scene;
	private final TabPane tabPane;

	private PhotoService photoService;
	private DriveService driveService;
	private DrivePersistence drivePersistence;
	private FishBaseDao dao;

	private final Map<Class<? extends View>, Supplier<? extends View>> viewSuppliers = Map.of(
			TripView.class, () -> new TripView(dao, photoService),
			SpecimenView.class, () -> new SpecimenView(dao, photoService, tripId -> openTab(TripView.class).selectTrip(tripId)),
			SpecieView.class, () -> new SpecieView(dao),
			PhotoView.class, () -> new PhotoView(photoService));

	public FishBaseApplication()
	{
		scene = new Scene(new Label());
		tabPane = new TabPane();
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		DriveFactory factory = DriveFactory.get();
		photoService = PhotoService.create(factory.create());
		driveService = new DriveService(factory.create());
		drivePersistence = new DrivePersistence(driveService);
		dao = FishBaseDao.create(drivePersistence);

		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> selectView());
		tabPane.setTabClosingPolicy(ALL_TABS);

		Button startButton = createStartButton();
		Button addButton = createActionButton("Add", "add.png", "CTRL+N", addAction);
		Button saveButton = createActionButton("Save", "save.png", "CTRL+S", saveAction);
		Button refreshButton = createActionButton("Refresh", "refresh.png", "F5", refreshAction);
		Button deleteButton = createActionButton("Delete", "delete.png", "CTRL+D", deleteAction);
		saveAction.enabledProperty().addListener((obs, old, enabled) ->	refreshButton.setGraphic(image(enabled ? "undo.png" : "refresh.png")));
		HBox toolbar = new HBox(startButton, addButton, saveButton, refreshButton, deleteButton);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(toolbar);
		borderPane.setCenter(tabPane);

		scene.setRoot(borderPane);
		stage.setScene(scene);
		stage.setTitle("FishBase");
		stage.getIcons().setAll(getImages("fish.png"));
		stage.setMaximized(true);
		stage.show();

		stage.onCloseRequestProperty().set(e ->
		{
			if (tabPane.getTabs().stream().map(tab -> tabToView.getOrDefault(tab, EMPTY_VIEW)).anyMatch(View::hasChanges))
			{
				Alert alert = new Alert(CONFIRMATION);
				alert.setTitle("Discard changes?");
				alert.setHeaderText("There are unsaved changes, discard them and shutdown anyway?");
				ButtonType shutdown = new ButtonType("Shutdown", OK_DONE);
				alert.getButtonTypes().setAll(shutdown, CANCEL);
				alert.showAndWait()
					.filter(b -> b != shutdown)
					.ifPresent(b -> e.consume());
			}
		});
	}

	private Button createStartButton()
    {
	    ContextMenu menu = new ContextMenu(
                createOpenItem("Trips", TripView.class),
                createOpenItem("Specimens", SpecimenView.class),
                createOpenItem("Species", SpecieView.class),
                createOpenItem("Photos", PhotoView.class));
	    menu.setAutoHide(true);

	    Button button = createButton("Menu", "menu.png", "CTRL+SPACE");
	    button.setOnAction(e ->
	    {
	        menu.show(button, BOTTOM, 0, 0);
	        menu.requestFocus();
	    });
	    return button;
    }

    private <V extends View> MenuItem createOpenItem(String text, Class<V> typeOfView)
    {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> openTab(typeOfView));
        return item;
    }

    private Button createActionButton(String text, String imageName, String shortcut, Action action)
	{
        Button button = createButton(text, imageName, shortcut);
        button.setOnAction(action);
		button.disableProperty().bind(action.enabledProperty().not());
		return button;
	}

    private Button createButton(String text, String imageName, String shortcut)
    {
        KeyCombination keyCombination = KeyCombination.valueOf(shortcut);
        Button button = new Button();
        button.setGraphic(image(imageName));
        button.setTooltip(new Tooltip(text + " (" + keyCombination.getDisplayText() + ")"));
        scene.getAccelerators().put(keyCombination, () -> button.fire());
        return button;
    }

	private ImageView image(String name)
	{
		return Images.getImageView32(name);
	}

	private void selectView()
	{
		View view = selectedView();
		addAction.setAction(view.addAction());
		saveAction.setAction(view.saveAction());
		refreshAction.setAction(view.refreshAction());
		deleteAction.setAction(view.deleteAction());
	}

	private View selectedView()
	{
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
		return tabToView.getOrDefault(selectedTab, EMPTY_VIEW);
	}

	private <V extends View> V openTab(Class<V> typeOfView)
	{
		Optional<Entry<Tab, View>> tabWithView = tabToView
				.entrySet()
				.stream()
				.filter(e -> typeOfView.isInstance(e.getValue()))
				.findAny();

		tabWithView.ifPresentOrElse(
				e -> selectTab(e.getKey()),
				() -> addTab(typeOfView));

		@SuppressWarnings("unchecked")
		V view = (V) selectedView();
		return view;
	}

    private <V extends View> void addTab(Class<V> typeOfView)
    {
        @SuppressWarnings("unchecked")
        Supplier<V> supplier = (Supplier<V>) viewSuppliers.get(typeOfView);
        V view = supplier.get();
        view.refreshAction().handle(null);

        Tab tab = new Tab("", view.getContent());
        tabToView.put(tab, view);
        tab.textProperty().bind(createStringBinding(
                () -> view.getTitle() + (view.hasChanges() ? " *" : ""),
                view.titleProperty(), saveAction.enabledProperty()));

        tab.setOnCloseRequest(e ->
        {
            if (view.hasChanges() && !view.discardChanges())
            {
                e.consume();
            }
        });
        tab.setOnClosed(e -> tabToView.remove(tab));

        tabPane.getTabs().add(tab);
        selectTab(tab);
    }

	private void selectTab(Tab tab)
	{
		tabPane.getSelectionModel().select(tab);
	}

	@Override
	public void stop() throws Exception
	{
		super.stop();
		if (drivePersistence != null)
		{
			drivePersistence.shutdown();
		}
	}
}
