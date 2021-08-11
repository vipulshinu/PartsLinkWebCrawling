package com.partslink.crawlPartsLink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PartslinkService {

    Map<String,String> vehicleDetails = new HashMap<>();

    public void getAllUrls(String fetchedData, String companyName){
        String vehicleUrl = Arrays.stream(fetchedData.split("\n")).filter(e->e.contains(companyName+"_parts")).collect(Collectors.joining());
        System.out.println(vehicleUrl);
        getModelLinks(vehicleUrl, companyName);
    }

    private Node getURLData(String partsUrl, String companyName) throws IOException {
        Document doc = Jsoup.connect(partsUrl).get();
        String title = doc.title().split(" ")[0];
        System.out.println(title);
        if(!(companyName.contains(title.toLowerCase())))
        {
            System.out.println("Need to Login.....");
            System.exit(0);
        }
        this.vehicleDetails.put("BrandName",title);
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
//                    if(models.childNode(i).childNode(1).childNode(0).childNode(0).toString().toLowerCase().equals(modName)
//                        && models.childNode(i).childNode(0).childNode(0).toString().toLowerCase().equals(modCode)){
                        String modelCode = models.childNode(i).childNode(0).childNode(0).outerHtml();
                        String modelName = models.childNode(i).childNode(1).childNode(0).childNode(0).toString();
                        System.out.println(modelCode + " "+modelName);
                        vehicleDetails.put("ModelName",modelName);
                        vehicleDetails.put("ModelCode",modelCode);
//                    }
//                    else{
//                        continue;
//                    }
                }
            }
            for(int i=0;i< vehicleDetails.size();i++){
                System.out.println(vehicleDetails.entrySet());
            }
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
}
