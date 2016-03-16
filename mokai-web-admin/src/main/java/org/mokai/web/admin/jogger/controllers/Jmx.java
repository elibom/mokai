package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.web.admin.jogger.annotations.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMX controller.
 *
 * @author German Escobar
 */
@Secured
public class Jmx {

	private Logger log = LoggerFactory.getLogger(Jmx.class);

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

        String mBean = URLDecoder.decode(request.getPathVariable("mbean"), "UTF-8");

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
				.put("description", attributeInfo.getDescription())
				.put("writable", attributeInfo.isWritable());

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

        response.write(json.toString());
	}

	public void showAttribute(Request request, Response response) throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        String mBean = URLDecoder.decode(request.getPathVariable("mbean"), "UTF-8");
        String attributeName = URLDecoder.decode(request.getPathVariable("attribute"), "UTF-8");

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
					.put("type", attribute.getType())
					.put("writable", attribute.isWritable())
					.put("value", value);

                response.write(json.toString());
				return;
			}
		}

		response.notFound();
	}

	public void updateAttribute(Request request, Response response) throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        String mBean = URLDecoder.decode(request.getPathVariable("mbean"), "UTF-8");
        String attributeName = URLDecoder.decode(request.getPathVariable("attribute"), "UTF-8");

        String strJson = request.getBody().asString();
		if ( "".equals(strJson) ) {
			response.badRequest();
			return;
		}

		try {
			JSONObject jsonData = new JSONObject(strJson);

			MBeanAttributeInfo[] attributesInfo = mBeanServer.getMBeanInfo(new ObjectName(mBean)).getAttributes();
			MBeanAttributeInfo attributeInfo = null;
			for (MBeanAttributeInfo ai : attributesInfo) {
				if (ai.getName().equals(attributeName)) {
					attributeInfo = ai;
				}
			}

			if (attributeInfo == null) {
				throw new AttributeNotFoundException(attributeName);
			}

			String type = attributeInfo.getType();
			Object value = null;
			if (type.contains("long")) {
				value = jsonData.getLong("value");
			} else {
				value = jsonData.get("value");
			}

			Attribute attribute = new Attribute(attributeName, value);
			mBeanServer.setAttribute(new ObjectName(mBean), attribute);
		} catch (JSONException e) {
            response.badRequest().write("{\"message\": \"" + e.getMessage() + "\"}");
		} catch (InstanceNotFoundException e) {
			response.notFound();
			return;
		} catch (AttributeNotFoundException e) {
			response.notFound();
			return;
		}
	}

	public void invoke(Request request, Response response) throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        String mBean = URLDecoder.decode(request.getPathVariable("mbean"), "UTF-8");
        String operationName = URLDecoder.decode(request.getPathVariable("operation"), "UTF-8");

		String[] signature = new String[0];
		Object[] params = new Object[0];

		// retrieve signature and params
		String strJson = request.getBody().asString();
		if ( !"".equals(strJson) ) {
			try {
				JSONObject jsonData = new JSONObject(strJson);
				signature = getSignature(jsonData);
				if (signature.length > 0) {
					params = getParams(jsonData, signature);
				}
			} catch (Exception e) {
                response.badRequest().write("{\"message\": \"" + e.getMessage() + "\"}");
			}
		}

		// retrieve mbean
		MBeanInfo mBeanInfo = null;
		try {
			mBeanInfo = mBeanServer.getMBeanInfo( new ObjectName(mBean) );
		} catch (InstanceNotFoundException e) {
			response.notFound();
			return;
		}

		// find the operation info
		MBeanOperationInfo operationInfo = null;
		for (MBeanOperationInfo oi : mBeanInfo.getOperations()) {
			if (oi.getName().equals(operationName) && isSameSignature(oi.getSignature(), signature)) {
				operationInfo = oi;
			}
		}

		if (operationInfo == null) {
			response.notFound();
			return;
		}

		try {
			Object ret = mBeanServer.invoke( new ObjectName(mBean), operationName, params, signature);
			if (ret != null) {
                response.write(toJson(ret).toString());
			}
		} catch (MBeanException e) {
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
            response.write(writer.toString());
		}

		return;
	}

	private String[] getSignature(JSONObject jsonData) throws JSONException {
		if (!jsonData.has("signature")) {
			return new String[0];
		}

		JSONArray jsonSignature = jsonData.getJSONArray("signature");
		String[] signature = new String[jsonSignature.length()];
		for (int i=0; i < jsonSignature.length(); i++) {
			signature[i] = jsonSignature.getString(i);
		}

		return signature;
	}

	private Object[] getParams(JSONObject jsonData, String[] signature) throws JSONException {
		JSONArray jsonParams = jsonData.getJSONArray("params");
		Object[] params = new Object[jsonParams.length()];

		if (params.length != signature.length) {
			throw new RuntimeException("Params doesn't match signature, expecting " + signature.length
					+ " but there are " + params.length + " params.");
		}

		for (int i=0; i < signature.length; i++) {
			params[i] = parseParam(jsonParams, i, signature[i]);
		}

		return params;
	}

	private boolean isSameSignature(MBeanParameterInfo[] paramsInfo, String[] signature) {
		if (paramsInfo.length != signature.length) {
			return false;
		}

		for (int i=0; i < paramsInfo.length; i++) {
			if ( !paramsInfo[i].getType().equals(signature[i]) ) {
				return false;
			}
		}

		return true;
	}

	private Object parseParam(JSONArray jsonParams, int index, String type) throws JSONException {
		if ("int".equals(type) || "java.lang.Integer".equals(type)) {
			return jsonParams.getInt(index);
		} else if ("long".equals(type) || "java.lang.Long".equals(type)) {
			return jsonParams.getLong(index);
		} else if ("double".equals(type) || "java.lang.Double".equals(type)) {
			return jsonParams.getDouble(index);
		} else if ("boolean".equals(type) || "java.lang.Boolean".equals(type)) {
			return jsonParams.getBoolean(index);
		} else {
			return jsonParams.getString(index);
		}
	}

	private String getAttributeValue(MBeanServer mBeanServer, String mBean, String attributeName) {
		try {
			Object object = mBeanServer.getAttribute( new ObjectName(mBean), attributeName );

			if (object == null) {
				return null;
			}

			return toJson(object).toString();
		} catch (Exception e) {
			log.error("Exception while retreiving attribute '" + attributeName + "' from mbean '" + mBean + "':" + e.getMessage(), e);
		}

		return null;
	}

	private Object toJson(Object object) throws Exception {
		if (object == null) {
			return "null";
		}

		if (CompositeData.class.isInstance(object)) {
			CompositeData data = (CompositeData) object;

			JSONObject json = new JSONObject();

			Set<String> keys = data.getCompositeType().keySet();
			for (String key : keys) {
				json.put(key, toJson(data.get(key)));
			}

			return json;

		} else if (object.getClass().isArray()) {
			Object[] arr = (Object[]) getArray(object);

			JSONArray json = new JSONArray();
			for (Object a : arr) {
				json.put( toJson(a) );
			}

			return json;

		} else if (ObjectName.class.isInstance(object)) {
			ObjectName objectName = (ObjectName) object;
			return objectName.getCanonicalName();
		} else {
			return object;
		}
	}

	private final Class<?>[] ARRAY_PRIMITIVE_TYPES = {
	        int[].class, float[].class, double[].class, boolean[].class,
	        byte[].class, short[].class, long[].class, char[].class };

	private Object[] getArray(Object val) {
		Class<?> valKlass = val.getClass();
		Object[] outputArray = null;

		for (Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
			if (valKlass.isAssignableFrom(arrKlass)) {
				int arrlength = Array.getLength(val);
				outputArray = new Object[arrlength];
				for(int i = 0; i < arrlength; ++i) {
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
