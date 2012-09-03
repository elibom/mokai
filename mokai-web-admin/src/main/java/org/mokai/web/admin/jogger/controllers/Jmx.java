package org.mokai.web.admin.jogger.controllers;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jogger.http.Request;
import org.jogger.http.Response;
import org.jogger.http.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mokai.web.admin.jogger.annotations.Secured;

@Secured
public class Jmx {

	public void index(Request request, Response response) throws MalformedObjectNameException {
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		List<Domain> domains = new ArrayList<Domain>();
		String[] domainsNames = mBeanServer.getDomains();
		
		for (String name : domainsNames) {
			
			Domain domain = new Domain( name );
			
			Set<ObjectInstance> mBeans = mBeanServer.queryMBeans( new ObjectName(name + ":*"), null );
			for (ObjectInstance mBean : mBeans) {
				domain.addmBean( mBean.getObjectName().getKeyPropertyListString() );
			}
		
			domains.add(domain);
			
		}
		
		Map<String,Object> root = new HashMap<String,Object>();
		root.put("tab", "jmx");
		root.put("domains", domains);
		
		response.render("jmx.ftl", root);
		
	}
	
	public void show(Request request, Response response) throws Exception {
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		String mBean = URLDecoder.decode( request.getPathVariable("mbean").asString(), "UTF-8" );
		
		MBeanInfo mBeanInfo = null;
		try {
			mBeanInfo = mBeanServer.getMBeanInfo( new ObjectName(mBean) );
		} catch (InstanceNotFoundException e) {
			response.notFound();
			return;
		}
		
		mBeanInfo.getAttributes();
		
		JSONObject json = new JSONObject()
			.put("timestamp", System.currentTimeMillis())
			.put("name", mBean)
			.put("className", mBeanInfo.getClassName())
			.put("description", mBeanInfo.getDescription());
		
		JSONArray jsonAttributes = new JSONArray();
		JSONArray jsonOperations = new JSONArray();
		
		MBeanAttributeInfo[] attributesInfo = mBeanInfo.getAttributes();
		for (MBeanAttributeInfo attributeInfo : attributesInfo) {
			
			JSONObject jsonAttribute = new JSONObject()
				.put("name", attributeInfo.getName())
				.put("type", attributeInfo.getType())
				.put("description", attributeInfo.getDescription());
			
			if (attributeInfo.isReadable()) {
				jsonAttribute.put( "value", getAttributeValue(mBeanServer, mBean, attributeInfo.getName()) );
			}
			
			jsonAttributes.put(jsonAttribute);
		}
		json.put("attributes", jsonAttributes);
		
		MBeanOperationInfo[] operationsInfo = mBeanInfo.getOperations();
		for (MBeanOperationInfo operationInfo : operationsInfo) {
			
			JSONObject jsonOperation = new JSONObject()
				.put( "name", operationInfo.getName() )
				.put( "description", operationInfo.getDescription() )
				.put( "returnType", operationInfo.getReturnType() );
			
			JSONArray jsonParams = new JSONArray();
			
			MBeanParameterInfo[] paramsInfo = operationInfo.getSignature();
			for (MBeanParameterInfo paramInfo : paramsInfo) {
				
				JSONObject jsonParam = new JSONObject()
					.put("name", paramInfo.getName())
					.put("type", paramInfo.getType())
					.put("description", paramInfo.getDescription());
				
				jsonParams.put(jsonParam);
			}
			
			jsonOperation.put("params", jsonParams);
			jsonOperations.put(jsonOperation);
			
		}
		json.put("operations", jsonOperations);
		
		response.print( json.toString() );
		
	}
	
	public void showAttribute(Request request, Response response) throws Exception {
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		String mBean = URLDecoder.decode( request.getPathVariable("mbean").asString(), "UTF-8" );
		String attributeName = URLDecoder.decode( request.getPathVariable("attribute").asString(), "UTF-8");
		
		MBeanInfo mBeanInfo = null;
		try {
			mBeanInfo = mBeanServer.getMBeanInfo( new ObjectName(mBean) );
		} catch (InstanceNotFoundException e) {
			response.notFound();
			return;
		}
		
		MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
		for (MBeanAttributeInfo attribute : attributes) {
			if (attribute.getName().equals(attributeName)) {
				
				String value = getAttributeValue(mBeanServer, mBean, attributeName);
				
				JSONObject json = new JSONObject()
					.put("timestamp", System.currentTimeMillis())
					.put("mbean", mBean)
					.put("name", attributeName)
					.put("value", value);
				
				response.print( json.toString() );
				return;
				
			}
		}
		
		response.notFound();
		
	}
	
	public void invoke(Request request, Response response) throws Exception {
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		String mBean = URLDecoder.decode( request.getPathVariable("mbean").asString(), "UTF-8" );
		String operationName = URLDecoder.decode( request.getPathVariable("operation").asString(), "UTF-8");
		
		List<Value> params = request.getParameter("params").asList();
		
		MBeanInfo mBeanInfo = null;
		try {
			mBeanInfo = mBeanServer.getMBeanInfo( new ObjectName(mBean) );
		} catch (InstanceNotFoundException e) {
			response.notFound();
			return;
		}
		
		MBeanOperationInfo[] operationsInfo = mBeanInfo.getOperations();
		for (MBeanOperationInfo operationInfo : operationsInfo) {
			if (operationInfo.getName().equals(operationName)) {
				
				MBeanParameterInfo[] paramsInfo = operationInfo.getSignature();
				
				
				
			}
		}
		
		response.notFound();
		
	}
	
	private String getAttributeValue(MBeanServer mBeanServer, String mBean, String attributeName) throws Exception {
		
		Object object = mBeanServer.getAttribute( new ObjectName(mBean), attributeName );
		
		if (object == null) {
			return null;
		}
		
		if (CompositeData.class.isInstance(object)) {
			CompositeData data = (CompositeData) object;
			
			JSONObject json = new JSONObject();
			
			Set<String> keys = data.getCompositeType().keySet();
			for (String key : keys) {
				json.put(key, data.get(key));
			}
			
			return json.toString();
			
		} else if (object.getClass().isArray()) {
			Object[] arr = (Object[]) getArray(object);
			
			JSONArray json = new JSONArray();
			for (Object a : arr) {
				json.put(a);
			}
			
			return json.toString();
			
		} else if (ObjectName.class.isInstance(object)) {
			ObjectName objectName = (ObjectName) object;
			return objectName.getCanonicalName();
		} else {
			return object.toString();
		}
		
	}
	
	private final Class<?>[] ARRAY_PRIMITIVE_TYPES = { 
	        int[].class, float[].class, double[].class, boolean[].class, 
	        byte[].class, short[].class, long[].class, char[].class };

	private Object[] getArray(Object val){
	    Class<?> valKlass = val.getClass();
	    Object[] outputArray = null;

	    for(Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES){
	        if(valKlass.isAssignableFrom(arrKlass)){
	            int arrlength = Array.getLength(val);
	            outputArray = new Object[arrlength];
	            for(int i = 0; i < arrlength; ++i){
	                outputArray[i] = Array.get(val, i);
	                            }
	            break;
	        }
	    }
	    if(outputArray == null) // not primitive type array
	        outputArray = (Object[])val;

	    return outputArray;
	}
	
}
