package org.mokai.config.jpf;

import java.util.Collection;

import org.mokai.config.Configuration;
import org.mokai.config.ConfigurationException;
import org.mokai.spi.type.AcceptorType;
import org.mokai.spi.type.ProcessorType;
import org.mokai.spi.type.ReceiverType;
import org.mokai.type.ObjectTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTypeConfiguration implements Configuration {
	
	private Logger log = LoggerFactory.getLogger(ObjectTypeConfiguration.class);
	
	private ObjectTypeRegistry objectTypeRegistry;
	
	private ExtensionService extensionService;

	@Override
	public void load() throws ConfigurationException {
		
		try {
			
			Collection<AcceptorType> acceptorTypes = extensionService.getAcceptorTypes();
			log.info("loaded " + acceptorTypes.size() + " acceptor types");
			for (AcceptorType acceptorType : acceptorTypes) {
				objectTypeRegistry.addAcceptorType(acceptorType);
			}
			
			Collection<ReceiverType> receiverTypes = extensionService.getReceiverTypes();
			log.info("loaded " + receiverTypes.size() + " receiver types");
			for (ReceiverType receiverType : receiverTypes) {
				objectTypeRegistry.addReceiverType(receiverType);
			}
			
			Collection<ProcessorType> processorTypes = extensionService.getProcessorTypes();
			log.info("loaded " + processorTypes.size() + " processor types");
			for (ProcessorType processorType : processorTypes) {
				objectTypeRegistry.addProcessorType(processorType);
			}
			
		} catch (Exception e) {
			log.error("Exception loading object types: " + e.getMessage(), e);
			throw new ConfigurationException(e);
		}
		
	}

	@Override
	public void save() {
		// no op
	}

	public void setObjectTypeRegistry(ObjectTypeRegistry objectTypeRegistry) {
		this.objectTypeRegistry = objectTypeRegistry;
	}

	public void setExtensionService(ExtensionService extensionService) {
		this.extensionService = extensionService;
	}

}
