package com.partslink.crawlPartsLink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.grabz.grabzit.GrabzItClient;
import it.grabz.grabzit.enums.TableFormat;
import it.grabz.grabzit.parameters.TableOptions;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class PartslinkService {

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    private static String getURLData(String partsUrl){
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(partsUrl);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    http.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            http.disconnect();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void getLinks(String partsUrl){
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder finalUrl = new StringBuilder();
        String restUrl = "";
        String vehicleCompanyName = "";
        List<String> vehicleSubTypeUrls = new ArrayList<>();

        try {

            String vehicleSubtype = partsUrl.toString().split("=")[1];
            

            String baseUrl = "https://www.partslink24.com/";
            String filePath = "src/main/resources/json/abarth_parts.json";
            JsonNode map = objectMapper.readValue(Paths.get(filePath).toFile(), JsonNode.class);
            String partsName = filePath.split("/")[3].split("\\.")[0];
            for(JsonNode a : map.get("links")){
                if(a.get("text").asText().equals("Portal")){
                    vehicleCompanyName = a.get("href").asText().split("/")[1];
                }
            }
            //String partsName = map.get("links").get(5).get("href").asText().split("/")[2];
            finalUrl.append(baseUrl).append(vehicleCompanyName).append("/").append(partsName).append("/");
            JsonNode rowsData = map.get("tables").get(0).get("rows");
            for(int i=0;i<rowsData.size()-1;i++){
                if(rowsData.get(i).get("attr") == null || rowsData.get(i).get("attr").size()<4 || rowsData.get(i).get("attr").get("url").asText().equals("")){
                    continue;
                }
                else{
                    restUrl = rowsData.get(i).get("attr").get("url").asText();
                    if(restUrl.charAt(0) == 'v'){
                        vehicleSubTypeUrls.add(finalUrl + restUrl);
                    }
                    else{
                        if(restUrl.contains("mdl")){
                            vehicleSubTypeUrls.add(finalUrl + restUrl.replace(restUrl.split("\\.")[0],"vehicle"));
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Vehicle SubType: ");
        vehicleSubTypeUrls.forEach(System.out::println);


    }
}
