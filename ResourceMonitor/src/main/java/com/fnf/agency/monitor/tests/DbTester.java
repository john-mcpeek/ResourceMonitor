package com.fnf.agency.monitor.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Map;

import com.fnf.agency.monitor.TestResult;

public class DbTester extends Tester {

	public DbTester(Map<String, String> config) {
		super( config );
	}

	@Override
	public TestResult execute() {
		String driverClass = config.get( "driverClass" );
		String connectionString = config.get( "connectionString" );
		String userName = config.get( "userName" );
		String password = config.get( "password" );
		String sql = config.get( "sql" );
		
		result =  new TestResult( config );
		
		try {
			Class.forName( driverClass );
			Connection con = DriverManager.getConnection( connectionString, userName, password );
			ResultSet res = con.createStatement().executeQuery( sql );
			boolean success = res.next();			
			result.setPassed( success );
			
			String columnValue = res.getString( 1 );
			result.setMessage( columnValue );
		} catch (Throwable e) {
			result.setMessage( e.getMessage() );
		}

		return result;
	}

}
