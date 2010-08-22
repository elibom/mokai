package org.mokai.impl.camel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mokai.Account;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.ProcessorService;

public class CamelAccount implements Account {
	
	private String id;
	
	private int priority;
	
	private Map<String,ProcessorService> processors;
	
	public CamelAccount(String id, int priority) {
		this.id = id;
		this.priority = priority;
		
		this.processors = new HashMap<String,ProcessorService>();
	}
	
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public ProcessorService createProcessor(String id, int priority,
			Processor processor) throws ObjectAlreadyExistsException {
		if (processors.containsKey(id)) {
			throw new ObjectAlreadyExistsException();
		}
		
		CamelProcessorService ps = new CamelProcessorService(id, priority, processor, null);
		processors.put(id, ps);
		
		return ps;
	}

	@Override
	public Account deleteProcessor(String id) throws ObjectNotFoundException {
		ProcessorService ps = processors.get(id);
		if (ps == null) {
			 throw new ObjectNotFoundException();
		}
		
		// stop
		if (ps.isServiceable()) {
			ps.stop();
		}
		
		// TODO - destroy
		
		// remove from processors
		processors.remove(id);
		
		return this;
	}

	@Override
	public ProcessorService getProcessor(String id) {
		return processors.get(id);
	}

	@Override
	public List<ProcessorService> getProcessors() {
		List<ProcessorService> processorsList = new ArrayList<ProcessorService>();
		processorsList.addAll(processors.values());
		
		Collections.sort(processorsList, new Comparator<ProcessorService>() {

			@Override
			public int compare(ProcessorService ps1, ProcessorService ps2) {
				if (ps1.getPriority() > ps2.getPriority()) {
					return 1;
				} else if (ps1.getPriority() < ps2.getPriority()) {
					return -1;
				} 
				
				return 0;
			}
			
		});
		
		return processorsList;
	}

}
