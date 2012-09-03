package org.mokai.web.admin.jogger.controllers;

import java.util.ArrayList;
import java.util.List;

public class Domain {

	private String name;
	
	private List<String> mBeans = new ArrayList<String>();
	
	public Domain(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getmBeans() {
		return mBeans;
	}

	public void setmBeans(List<String> mBeans) {
		this.mBeans = mBeans;
	}

	public void addmBean(String mBean) {
		mBeans.add(mBean);
	}
	
}
