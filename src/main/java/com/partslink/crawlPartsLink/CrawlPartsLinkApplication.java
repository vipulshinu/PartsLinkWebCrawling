package com.partslink.crawlPartsLink;

import com.partslink.crawlPartsLink.service.PartslinkService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class CrawlPartsLinkApplication {

	public static void main(String[] args) {
		PartslinkService partslinkService = new PartslinkService();
		partslinkService.getLinks("https://www.partslink24.com/partslink24/launchCatalog.do?service=abarth_parts");
	}

}
