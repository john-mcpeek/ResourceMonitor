package com.fnf.agency.monitor.tests;

import java.util.Date;
import java.util.Map;

import com.fnf.agency.monitor.TestResult;

public abstract class Tester {
	protected Map<String, String> config;
	protected String testName;
	protected TestResult result;
	
	public Tester(Map<String, String> config) {
		this.config = config;
		testName = (String) config.get( "name" );

		System.out.println( testName + " " + new Date() );
	}
	
	public abstract TestResult execute();
}
