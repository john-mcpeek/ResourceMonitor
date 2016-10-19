package com.fnf.agency.monitor;

import java.util.Date;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestResult {
	private String name;
	private boolean passed;
	private String message;
	private Date time = new Date();
	
	private Map<String, String> config;
	
	private TestResult lastResult;
	
	public TestResult(Map<String, String> config) {
		this.config = config;
		this.name = config.get( "name" );
	}
}
