package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.Acceptor;
import org.mokai.Monitorable.Status;
import org.mokai.ProcessorService;

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
 * A layout that contains the information of a processor.
 * 
 * @author German Escobar
 */
public class ProcessorComponent extends VerticalLayout {
	
	private static final long serialVersionUID = -979408995104262235L;

	private HorizontalLayout header;
	
	private Button startStopButton;
	
	private ObjectProperty<String> status;
	
	private Label statusLabel;
	
	private DashboardPresenter presenter;
	
	private ProcessorService processorService;

	/**
	 * Constructor. 
	 * 
	 * @param presenter
	 * @param processorService
	 */
	public ProcessorComponent(DashboardPresenter presenter, ProcessorService processorService) {
		this.presenter = presenter;
		this.processorService = processorService;
		
		init();
	}
	
	/**
	 * Helper method. This method actually fills the layout with the information of the processor
	 */
	private void init() {
		
		// set the style
		setWidth("300px");
		addStyleName(Reindeer.LAYOUT_WHITE);
		addStyleName("border");
		
		// header
		header = createHeader();
		addComponent(header);
		
		// body
		HorizontalLayout status = createBody();
		addComponent(status);
		
	}
	
	/**
	 * Helper method. Creates the header of the processor box.
	 * 
	 * @return the layout that holds the header of the processor box.
	 */
	private HorizontalLayout createHeader() {
		
		// create the layout and set the styles
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setSpacing(true);
		header.addStyleName(Helper.getStateStyle(processorService));
		header.addStyleName("header");
		
		// name and class on the left
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
	 * @return a layout that contains the id and the type of the processor.
	 */
	private VerticalLayout createIdAndType() {
		VerticalLayout name = new VerticalLayout();
		
		// id
		Label idLabel = new Label("<strong>" + processorService.getId() + "</strong>", Label.CONTENT_XHTML);
		name.addComponent(idLabel);
		
		// type
		Label typeLabel = new Label(Helper.getComponentName(processorService.getProcessor()));
		typeLabel.setStyleName(Reindeer.LABEL_SMALL);
		name.addComponent(typeLabel);
		
		return name;
	}
	
	/**
	 * Helper method. Creates the "details" button of the processor box.
	 * 
	 * @return a Button object that will open a popup window with the details of the processor.
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
	 * Helper method. Creates the "start/stop" button of the processor box.
	 * 
	 * @return a Button object that will start or stop the processor.
	 */
	@SuppressWarnings("serial")
	private Button createStartStopButton() {
		
		// set the caption
		String caption = "Start";
		if (processorService.getState().isStoppable()) {
			caption = "Stop";
		}
		
		// create the button and add the listener that will start or stop the processor
		startStopButton = new Button(caption);
		startStopButton.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.changeProcessorServiceState(processorService);
				update();
			}
			
		});
		
		return startStopButton;
	}
	
	/**
	 * Helper method. Creates the body of the processor box. 
	 * 
	 * @return the layout that holds the body of the processor box.
	 */
	private HorizontalLayout createBody() {
		
		// create the layout and set the style
		HorizontalLayout body = new HorizontalLayout();
		body.setWidth("100%");
		body.setStyleName("header");
		
		// add the number of queued messages
		Label queuedMsgs = new Label("Queued: <strong>" + processorService.getNumQueuedMessages() + "</strong>", Label.CONTENT_XHTML);
		body.addComponent(queuedMsgs);
		
		// add the status
		String statusCaption = getStatusCaption();
		status = new ObjectProperty<String>(statusCaption);
		statusLabel = new Label(status, Label.CONTENT_XHTML);
		cleanAndSetStatusStyle();
		
		// add the status label
		body.addComponent(statusLabel);
		body.setComponentAlignment(statusLabel, Alignment.MIDDLE_RIGHT);
		
		return body;
	}

	/**
	 * Helper method. Updates the processor box with the processor information.
	 */
	private void update() {
		
		// update button caption
		String caption = "Stop";
		if (processorService.getState().isStartable()) {
			caption = "Start";
		}
		startStopButton.setCaption(caption);
		
		// set the background color of the header
		header.removeStyleName("green");
		header.removeStyleName("red");
		header.addStyleName(Helper.getStateStyle(processorService));
		
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
		if (processorService.getStatus().equals(Status.FAILED)) {
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
		if (!processorService.getStatus().equals(Status.UNKNOWN)) {
			statusCaption = "Status: <strong>" + processorService.getStatus() + "</strong>";
		}
		
		return statusCaption;
	}
	
	/**
	 * Helper method. Displays a popup window that shows the details of the processor.
	 */
	private void createAndShowDetailsWindow() {
		
		// create the window and set the style
		Window detailsWindow = new Window("Processor: " + processorService.getId());
		detailsWindow.setModal(true);
		detailsWindow.setWidth("500px");
		detailsWindow.setHeight("500px");
		
		// get the layout and set the style
		VerticalLayout layout = (VerticalLayout) detailsWindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        
        // add the type of the processor
        Label nameLabel = new Label("<strong>Type:</strong> " + Helper.getComponentName(processorService.getProcessor()), Label.CONTENT_XHTML);
        layout.addComponent(nameLabel);
        
        // add the priority of the processor
        Label priorityLabel = new Label("<strong>Priority:</strong> " + processorService.getPriority(), Label.CONTENT_XHTML);
        layout.addComponent(priorityLabel);
		
        // create a tree with the configuration, acceptors and actions information
		Tree infoTree = new Tree();
		
		// add the configuration properties to the tree
		Helper.addConfigurationToTree(infoTree, processorService.getProcessor(), null);
		
		// add acceptors to the tree
		addAcceptorsToTree(infoTree);
		
		// add pre-processing, post-processing and post-receiving actions to tree
		Helper.addActionsToTree(infoTree, processorService.getPreProcessingActions(), "Pre-processing actions");
		Helper.addActionsToTree(infoTree, processorService.getPostProcessingActions(), "Post-processing actions");
		Helper.addActionsToTree(infoTree, processorService.getPostReceivingActions(), "Post-receiving actions");
		
		layout.addComponent(infoTree);
        
        getWindow().addWindow(detailsWindow);
	}
	
	/**
	 * Helper method. Adds the acceptors information to tree that is shown in the details window.
	 * 
	 * @param tree the Tree object to which we are adding the acceptors information
	 */
	private void addAcceptorsToTree(Tree tree) {
		
		tree.addItem("Acceptors");
		for (Acceptor acceptor : processorService.getAcceptors()) {
			String acceptorClass = acceptor.getClass().getSimpleName(); 
			
			tree.addItem(acceptorClass);
			tree.setParent(acceptorClass, "Acceptors");
			
			Helper.addConfigurationToTree(tree, acceptor, acceptorClass);
		}
	}
	
}
