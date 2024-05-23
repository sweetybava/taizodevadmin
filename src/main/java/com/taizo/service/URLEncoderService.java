package com.taizo.service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;

@Service
public class URLEncoderService {

	public String jobEncodedLink() throws URISyntaxException {
		URIBuilder ub = new URIBuilder("https://web.taizo.in/console");
		ub.addParameter("jobId", "23");
		String url = ub.toString();
        System.out.println( url ) ;  
        return url.toString();
	}
}
