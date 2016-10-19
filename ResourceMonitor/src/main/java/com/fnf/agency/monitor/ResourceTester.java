package com.fnf.agency.monitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnf.agency.monitor.tests.Tester;

@Service
public class ResourceTester {
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private Map<String, TestResult> results;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private Environment env;

	@Scheduled(fixedRateString = "${test.fixedRate}")
	public void runTests() throws JsonParseException, JsonMappingException, IOException {
		List<Map<String, Object>> map = mapper.readValue( new File( "config.json" ), new TypeReference<List<Map<String, Object>>>() {} );

		List<TestResult> stateChanges = new ArrayList<>();
		for ( Map<String, Object> testConfig : map ) {
			String testName = (String) testConfig.get( "name" );
			String testType = (String) testConfig.get( "type" );

			try {
				Tester tester = (Tester) Class.forName( "com.fnf.agency.monitor.tests." + testType + "Tester" ).getConstructor( Map.class )
						.newInstance( testConfig );

				TestResult lastResult = results.get( testName );
				if ( lastResult != null ) {
					lastResult.setLastResult( null );
				}

				TestResult result = tester.execute();
				result.setLastResult( lastResult );

				if ( lastResult == null || result.isPassed() != lastResult.isPassed() ) {
					stateChanges.add( result );
				}

				if ( result.isPassed() ) {
					results.put( testName, result );
				} else if ( lastResult == null || lastResult.isPassed() ) {
					results.put( testName, result );
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if ( stateChanges.size() > 0 ) {
			sendEmail( stateChanges );
		}
	}

	private void sendEmail(List<TestResult> stateChanges) {
		try {
			List<TestResult> failures = stateChanges.stream().filter( c -> c.isPassed() == false ).collect( Collectors.toList() );
			List<TestResult> passes = stateChanges.stream().filter( c -> c.isPassed() ).collect( Collectors.toList() );

			String subject = failures.stream().map( c -> "Failed: " + c.getName() ).collect( Collectors.joining( ", " ) );
			subject += " | ";
			subject += passes.stream().map( c -> "Passed: " + c.getName() ).collect( Collectors.joining( ", " ) );

			Context context = new Context();
			context.setVariable( "failures", failures );
			context.setVariable( "passes", passes );
			String emailBody = templateEngine.process( "StateChangeEmail", context );
			//System.out.println( emailBody );

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper( message, false, "utf-8" );
			helper.setFrom( env.getProperty( "email.from" ) );
			helper.setTo( env.getProperty( "email.to" ) );
			helper.setSubject( subject );
			message.setContent( emailBody, "text/html" );

			mailSender.send( message );
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
