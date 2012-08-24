package org.mokai.connector.smpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.SmppServer;
import net.gescobar.smppserver.SmppServer.Status;
import net.gescobar.smppserver.packet.Bind;
import net.gescobar.smppserver.packet.SmppPacket;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SubmitSm;

import org.mokai.Configurable;
import org.mokai.ConnectorContext;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;

@Name("SMPP Server Connector")
@Description("Allows SMPP clients to connect, send and receive messages")
public class SmppServerConnector implements Processor, Configurable, Serviceable, 
		ExposableConfiguration<SmppServerConfiguration> {
	
	/**
	 * The folder in which we will save the file with the sequence number.
	 */
	private static final String SEQUENCE_NUMBER_FOLDER = "data/connectors/smpp-server/";
	
	/**
	 * The extension of the file that will store the sequence number.
	 */
	private static final String SEQUENCE_NUMBER_EXT = ".seq";
	
	@Resource
	private ConnectorContext context;
	
	@Resource
	private MessageProducer messageProducer;
	
	private SmppServerConfiguration configuration;
	
	private SmppServer smppServer;
	
	public SmppServerConnector() {
		this(new SmppServerConfiguration());
	}
	
	public SmppServerConnector(SmppServerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void process(Message message) throws Exception {
		
	}

	@Override
	public boolean supports(Message message) {
		
		String to = message.getProperty("to", String.class);
		
		if (to == null || "".equals(to)) {
			return false;
		}
		
		return to.matches("[0-9]*");
		
	}

	@Override
	public SmppServerConfiguration getConfiguration() {
		return configuration;
	}
	
	@Override
	public void configure() throws Exception {
		smppServer = new SmppServer(configuration.getPort(), new CustomPacketProcessor());
	}

	@Override
	public void doStart() throws Exception {
		smppServer.start();
	}

	@Override
	public void doStop() throws Exception {
		smppServer.stop();
		
		while (!smppServer.getStatus().equals(Status.STOPPED)) {
			Thread.sleep(100);
		}
	}

	@Override
	public void destroy() throws Exception {
	}
	
	/**
	 * Helper method that will return a sequence number used to return the message id of a submit_sm response. 
	 * It reads the number from a file, increments it, saves it again in the file and
	 * returns it. 
	 * 
	 * @return the sequence number used to return the message id of the submit_sm response
	 * @throws Exception if anything goes wrong.
	 */
	private synchronized int nextMessageId() throws Exception {
		
		int sequence = 1;
		
		// check if the file exists and read the number
		File file = new File(SEQUENCE_NUMBER_FOLDER + context.getId() + SEQUENCE_NUMBER_EXT);
		if (file.exists()) {
			
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new FileReader(file));
				sequence = Integer.parseInt(in.readLine());
			} finally {
				if (in != null) {
					try { in.close(); } catch (Exception e) {}
				}
			}
			
		}
		
		// create the directories if they aren't created already
		File fDir = new File(SEQUENCE_NUMBER_FOLDER);
		fDir.mkdirs();
		
		// increment the number and save it in the file
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(SEQUENCE_NUMBER_FOLDER + context.getId() + SEQUENCE_NUMBER_EXT));
			out.println(sequence + 1);
		} finally {
			if (out != null) {
				try { out.close(); } catch (Exception e) {}
			}
		}
		
		return sequence;
		
	}
	
	private class CustomPacketProcessor implements PacketProcessor {

		@Override
		public void processPacket(SmppRequest packet, ResponseSender responseSender) {
			
			if (packet.isBind()) {
				
				Bind bind = (Bind) packet;
				String systemId = bind.getSystemId();
				String password = bind.getPassword();
				
				if (systemId == null || "".equals(systemId)) {
					responseSender.send( Response.INVALID_PARAMETER_VALUE );
					return;
				}
				
				if (password == null || "".equals(password)) {
					responseSender.send( Response.INVALID_PARAMETER_VALUE );
					return;
				}
				
				if (!configuration.getUsers().containsKey(systemId)) {
					responseSender.send( Response.INVALID_SYSTEM_ID );
					return;
				}
				
				if (!password.equals(configuration.getUsers().get(systemId))) {
					responseSender.send( Response.INVALID_PASSWORD );
					return;
				}
				
				responseSender.send( Response.OK );
				return;
				
			}  else if (packet.getCommandId() == SmppPacket.SUBMIT_SM) {
				
				SubmitSm submitSm = (SubmitSm) packet;
				
				Message message = new Message();
				message.setProperty("to", submitSm.getDestAddress().getAddress());
				message.setProperty("from", submitSm.getSourceAddress().getAddress());
				message.setProperty("text", submitSm.getShortMessage());
				message.setProperty("receiptDestination", context.getId());
				
				messageProducer.produce(message);
				
				try {
					responseSender.send( Response.OK.withMessageId(nextMessageId() + "") );
					return;
				} catch (Exception e) {
					responseSender.send( Response.SYSTEM_ERROR );
					return;
				}
				
			}
			
			responseSender.send( Response.OK );
		}
		
	}

}
