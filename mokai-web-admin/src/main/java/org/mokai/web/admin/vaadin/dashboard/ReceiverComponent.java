package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.Monitorable.Status;
import org.mokai.ReceiverService;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * A layout that contains the information of a receiver.
 * 
 * @author German Escobar
 */
public class ReceiverComponent extends VerticalLayout {

	private static final long serialVersionUID = -3895039976750394187L;

	private HorizontalLayout header;
	
	private Button startStopButton;
	
	private Label statusLabel;
	
	private ObjectProperty<String> status;
	
	private DashboardPresenter presenter;
	
	private ReceiverService receiverService;
	
	/**
	 * Constructor. 
	 * 
	 * @param presenter
	 * @param receiverService
	 */
	public ReceiverComponent(DashboardPresenter presenter, ReceiverService receiverService) {
		this.presenter = presenter;
		this.receiverService = receiverService;
		
		init();
	}
	
	/**
	 * Helper method. This method actually fills the layout with the information of the receiver.
	 */
	public void init() {
		
		// set the style of the layout
		setWidth("300px");
		addStyleName(Reindeer.LAYOUT_WHITE);
		addStyleName("border");
		
		// header
		header = createHeader();
		addComponent(header);
		
		// body
		HorizontalLayout status = createBody();
		addComponent(status);
		setComponentAlignment(status, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * Helper method. Creates the header of the receiver box. 
	 * 
	 * @return the layout that holds the header of the receiver box.
	 */
	private HorizontalLayout createHeader() {
		
		// create the layout and set the style
		HorizontalLayout header = new HorizontalLayout();
		header.setSpacing(true);
		header.setWidth("100%");
		header.addStyleName(Helper.getStateStyle(receiverService));
		header.addStyleName("header");
		
		// id and name of the receiver on the left
		VerticalLayout name = createIdAndType();
		header.addComponent(name);
		header.setExpandRatio(name, 1.0f);
		
		// details button
		Button detailsButton = createDetailsButton();
		header.addComponent(detailsButton);
		header.setComponentAlignment(detailsButton, Alignment.MIDDLE_RIGHT);
		
		// start or stop button
		Button processorButton = createStartStopButton();
		header.addComponent(processorButton);
		header.setComponentAlignment(processorButton, Alignment.MIDDLE_RIGHT);
		
		return header;
		
	}
	
	/**
	 * Helper method. Creates part of the header.
	 * 
	 * @return a layout that contains the id and the type of the receiver.
	 */
	private VerticalLayout createIdAndType() {
		VerticalLayout name = new VerticalLayout();
		
		// id
		Label idLabel = new Label("<strong>" + receiverService.getId() + "</strong>", Label.CONTENT_XHTML);
		name.addComponent(idLabel);
		
		// type
		Label typeLabel = new Label(Helper.getComponentName(receiverService.getReceiver()));
		typeLabel.setStyleName(Reindeer.LABEL_SMALL);
		name.addComponent(typeLabel);
		
		return name;
	}
	
	/**
	 * Helper method. Creates the "details" button of the receiver box.
	 * 
	 * @return a Button object that will open a popup window with the details of the receiver.
	 */
	@SuppressWarnings("serial")
	private Button createDetailsButton() {
		
		// create the button and set the style
		Button detailsButton = new Button("Details");
		detailsButton.addStyleName(BaseTheme.BUTTON_LINK);
		
		// add a click listener that shows a popup window with the details
		detailsButton.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				createAndShowDetailsWindow();
			}
			
		});
		
		return detailsButton;
	}
	
	/**
	 * Helper method. Creates the "start/stop" button of the receiver box.
	 * 
	 * @return a Button object that will start or stop the receiver.
	 */
	@SuppressWarnings("serial")
	private Button createStartStopButton() {
		
		// set the caption
		String caption = "Start";
		if (receiverService.getState().isStoppable()) {
			caption = "Stop";
		}
		
		// create the button and add the listener that will start or stop the processor
		startStopButton = new Button(caption);
		startStopButton.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.changeReceiverServiceState(receiverService);
				update();
			}
			
		});
		
		return startStopButton;
	}
	
	/**
	 * Helper method. Creates the body of the receiver box. 
	 * 
	 * @return the layout that holds the body of the receiver box.
	 */
	private HorizontalLayout createBody() {
		
		// create the layout and set the style
		HorizontalLayout body = new HorizontalLayout();
		body.addStyleName("header");
		
		// add the status
		String statusCaption = getStatusCaption();
		status = new ObjectProperty<String>(statusCaption);
		statusLabel = new Label(status, Label.CONTENT_XHTML);
		cleanAndSetStatusStyle();
		
		return body;
	}

	/**
	 * Helper method. Updates the processor box with the receiver information.
	 */
	private void update() {
		
		// update button caption
		String caption = "Stop";
		if (receiverService.getState().isStartable()) {
			caption = "Start";
		}
		startStopButton.setCaption(caption);
		
		// set the background color of the header
		header.removeStyleName("green");
		header.removeStyleName("red");
		header.addStyleName(Helper.getStateStyle(receiverService));
		
		// set the status
		String statusCaption = getStatusCaption();
		status.setValue(statusCaption);
		cleanAndSetStatusStyle();
		
	}
	
	/**
	 * Helper method. Sets the style of the status label.
	 */
	private void cleanAndSetStatusStyle() {
		statusLabel.addStyleName("right");
		
		statusLabel.removeStyleName("red");
		if (receiverService.getStatus().equals(Status.FAILED)) {
			statusLabel.addStyleName("red");
		}
	}
	
	/**
	 * Helper method. Decides which caption to use for the status depending on the status of the receiver.
	 * 
	 * @return the text for the status label.
	 */
	private String getStatusCaption() {
		String statusCaption = "";
		if (!receiverService.getStatus().equals(Status.UNKNOWN)) {
			statusCaption = "Status: <strong>" + receiverService.getStatus() + "</strong>";
		}
		
		return statusCaption;
	}
	
	/**
	 * Helper method. Displays a popup window that shows the details of the receiver.
	 */
	private void createAndShowDetailsWindow() {
		
		// create the window and set the style
		Window detailsWindow = new Window("Receiver: " + receiverService.getId());
		detailsWindow.setModal(true);
		detailsWindow.setWidth("500px");
		detailsWindow.setHeight("500px");
		
		// get the layout and set the style
		VerticalLayout layout = (VerticalLayout) detailsWindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        
        // add the type of the processor
        Label nameLabel = new Label("<strong>Type:</strong> " + Helper.getComponentName(receiverService.getReceiver()), Label.CONTENT_XHTML);
        layout.addComponent(nameLabel);
		
        // create a tree with the configuration and actions information
		Tree infoTree = new Tree();
		
		// add the configuration properties to the tree
		Helper.addConfigurationToTree(infoTree, receiverService.getReceiver(), null);
		
		// add post-receiving actions to the tree
		Helper.addActionsToTree(infoTree, receiverService.getPostReceivingActions(), "Post-receiving actions");
		
		layout.addComponent(infoTree);
        
        getWindow().addWindow(detailsWindow);
	}
	
}
