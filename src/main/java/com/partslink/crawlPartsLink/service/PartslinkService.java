package com.partslink.crawlPartsLink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PartslinkService {

    Map<String,String> vehicleDetails = new HashMap<>();
    String vehicleParent;
    Map<String,String> vehicleModels = new LinkedHashMap<>();
    Map<String,String> modelLinks = new LinkedHashMap<>();

    public void getAllUrls(String fetchedData, String companyName){
        String vehicleUrl = Arrays.stream(fetchedData.split("\n")).filter(e->e.contains(companyName+"_parts")).collect(Collectors.joining());
        System.out.println(vehicleUrl);
        getModelLinks(vehicleUrl, companyName);
    }

    private Node getURLData(String partsUrl, String companyName) throws IOException {
        Document doc = Jsoup.connect(partsUrl).get();
        String title = doc.title().split(" ")[0];
        if(!(companyName.contains(title.toLowerCase())))
        {
            System.out.println("Need to Login.....");
            System.exit(0);
        }
        this.vehicleDetails.put("BrandName",title);
        this.vehicleParent = doc.childNode(2).childNode(3).childNode(1).childNode(1).childNode(0).childNode(8).attr("href").split("/")[1];
        System.out.println("Vehicle Parent:: "+ vehicleParent);
        Node modelList = doc.childNode(2).childNode(3).childNode(2).childNode(0).childNode(0).childNode(0).childNode(1).childNode(0).childNode(1);
        return modelList;
    }

    public void getModelLinks(String partsUrl, String companyName){
        ObjectMapper objectMapper = new ObjectMapper();


        try {
            Node models = getURLData(partsUrl, companyName);
//            Scanner sc = new Scanner(System.in);
//            System.out.println("Enter Model Name: ");
//            String modName = sc.next();
//            System.out.println("Enter Model Code: ");
//            String modCode = sc.next();
            for(int i=0;i< models.childNodeSize();i++){
                if(models.childNode(i).childNodes().size()>1){
                        String modelCode = models.childNode(i).childNode(0).childNode(0).outerHtml();
                        String modelName = models.childNode(i).childNode(1).childNode(0).childNode(0).toString();
                        String modelUrl = models.childNode(i).attr("url");
                        this.modelLinks.put(modelName,modelUrl);
                        this.vehicleModels.put(modelCode,modelName);
                }
            }
            getSubUrlData(companyName);
                System.out.println(modelLinks.entrySet());

//            String vehicleSubtype = partsUrl.toString().split("=")[1];
//
//            String baseUrl = "https://www.partslink24.com/";
//            String filePath = "src/main/resources/json/abarth_parts.json";
//            JsonNode map = objectMapper.readValue(Paths.get(filePath).toFile(), JsonNode.class);
//            String partsName = filePath.split("/")[3].split("\\.")[0];
//            for(JsonNode a : map.get("links")){
//                if(a.get("text").asText().equals("Portal")){
//                    vehicleCompanyName = a.get("href").asText().split("/")[1];
//                }
//            }
//            //String partsName = map.get("links").get(5).get("href").asText().split("/")[2];
//            finalUrl.append(baseUrl).append(vehicleCompanyName).append("/").append(partsName).append("/");
//            JsonNode rowsData = map.get("tables").get(0).get("rows");
//            for(int i=0;i<rowsData.size()-1;i++){
//                if(rowsData.get(i).get("attr") == null || rowsData.get(i).get("attr").size()<4 || rowsData.get(i).get("attr").get("url").asText().equals("")){
//                    continue;
//                }
//                else{
//                    restUrl = rowsData.get(i).get("attr").get("url").asText();
//                    if(restUrl.charAt(0) == 'v'){
//                        vehicleSubTypeUrls.add(finalUrl + restUrl);
//                    }
//                    else{
//                        if(restUrl.contains("mdl")){
//                            vehicleSubTypeUrls.add(finalUrl + restUrl.replace(restUrl.split("\\.")[0],"vehicle"));
//                        }
//                    }
//                }
//
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getSubUrlData(String companyName) {
        //Sub Url
        StringBuilder sb = new StringBuilder();
        String baseUrl = "https://www.partslink24.com/";
        sb.append(baseUrl).append(this.vehicleParent).append("/").append(companyName+"_parts").append("/").append(this.modelLinks.get("MICRA"));
        System.out.println(sb);
        try {
            URL url = new URL(sb.toString());
            Document subDoc = Jsoup.connect(String.valueOf(url)).get();
            System.out.println(subDoc);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
