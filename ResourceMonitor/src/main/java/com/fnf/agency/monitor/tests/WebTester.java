package com.fnf.agency.monitor.tests;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fnf.agency.monitor.TestResult;

public class WebTester extends Tester {
	private static RestTemplate restTemplate = new RestTemplate();

	public WebTester(Map<String, String> config) {
		super( config );
	}

	@Override
	public TestResult execute() {
		result = new TestResult( config );

		try {
			HttpHeaders headers = new HttpHeaders();
			String headersStr = config.get( "headers" );
			if ( headersStr != null && headersStr.trim().length() > 0 ) {
				for ( String nvp : headersStr.split( ";" ) ) {
					String[] parts = nvp.split( "=" );
					headers.set( parts[ 0 ], parts[ 1 ] );
				}
			}

			HttpEntity<?> entity = new HttpEntity<>( headers );
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl( config.get( "url" ) );
			ResponseEntity<String> response = restTemplate.exchange( builder.build().encode().toUri(), HttpMethod.GET, entity, String.class );

			HttpStatus statusCode = response.getStatusCode();
			String body = response.getBody();
			if ( statusCode == HttpStatus.OK ) {
				String regex = config.get( "regex" );
				Pattern ptn = Pattern.compile( regex, Pattern.DOTALL | Pattern.MULTILINE );
				Matcher mtr = ptn.matcher( body );
				boolean found = mtr.find();

				result.setPassed( found );

				String message = statusCode.name() + ( found ? " Found: " + mtr.group() : " NOT Found: " + regex );
				result.setMessage( message );
			}
			else {
				result.setMessage( statusCode.name() + " Body: " + body );
			}
		} catch (RestClientException e) {
			try {
				String decodedMessage = UriUtils.decode( e.getMessage(), "UTF-8" );
				result.setMessage( decodedMessage );
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

}
