package com.partslink.crawlPartsLink;

import com.partslink.crawlPartsLink.service.PartslinkService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CrawlPartsLinkApplication {

	public static void main(String[] args) throws IOException {
		//For fetching all vehicle base links
		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash","src/main/resources/script/fetchLinks.sh");
		Process process = processBuilder.start();
		String fetchedLinks = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
				.lines()
				.filter(v->v.contains("launchCatalog"))
				.collect(Collectors.joining("\n"));

		//User input vehicle name
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please Select the Company: ");
		String companyName = scanner.next();

		PartslinkService partslinkService = new PartslinkService();
		partslinkService.getAllUrls(fetchedLinks,companyName);
		//partslinkService.getModelLinks("https://www.partslink24.com/partslink24/launchCatalog.do?service=nissan_parts");
	}

}
