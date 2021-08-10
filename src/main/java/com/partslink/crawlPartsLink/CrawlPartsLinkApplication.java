package com.partslink.crawlPartsLink;

import com.partslink.crawlPartsLink.service.PartslinkService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CrawlPartsLinkApplication {

	public static void main(String[] args) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash","src/main/resources/script/fetchLinks.sh");
		Process process = processBuilder.start();
		String fetchedLinks = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
		PartslinkService partslinkService = new PartslinkService();
//		partslinkService.getAllUrls(fetchedLinks);
		partslinkService.getLinks("https://www.partslink24.com/partslink24/launchCatalog.do?service=abarth_parts");
	}

}
