package com.partslink.crawlPartsLink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PartslinkService {

    Map<String,String> vehicleDetails = new LinkedHashMap<>();
    String vehicleParent;
    Map<String,String> vehicleModels = new LinkedHashMap<>();
    Map<String,String> modelLinks = new LinkedHashMap<>();

    public void getAllUrls(String fetchedData, String companyName){
        String vehicleUrl = Arrays.stream(fetchedData.split("\n")).filter(e->e.contains(companyName+"_parts")).collect(Collectors.joining());
        getModelLinks(vehicleUrl, companyName);
    }

    private Node getURLDataTable(String partsUrl, String companyName) throws IOException {
        Document doc = Jsoup.connect(partsUrl).get();
        String title = doc.title().split(" ")[0];
        if(!(companyName.contains(title.toLowerCase())))
        {
            System.out.println("Need to Login.....");
            System.exit(0);
        }
        this.vehicleDetails.put("BrandName",title);
        this.vehicleParent = doc.childNode(2).childNode(3).childNode(1).childNode(1).childNode(0).childNode(8).attr("href").split("/")[1];
        Node modelList = doc.childNode(2).childNode(3).childNode(2).childNode(0).childNode(0).childNode(0).childNode(1).childNode(0).childNode(1);
        return modelList;
    }

    public void getModelLinks(String partsUrl, String companyName){
        ObjectMapper objectMapper = new ObjectMapper();


        try {
            Node models = getURLDataTable(partsUrl, companyName);
            for(int i=0;i< models.childNodeSize();i++){
                if(models.childNode(i).childNodes().size()>1){
                        String modelCode = models.childNode(i).childNode(0).childNode(0).outerHtml();
                        String modelName = models.childNode(i).childNode(1).childNode(0).childNode(0).toString();
                        String modelUrl = models.childNode(i).attr("jsonurl");
                        this.modelLinks.put(modelName,modelUrl);
                        this.vehicleModels.put(modelCode,modelName);
                        //For Current Scenario
                        if(modelCode.equals("K12E")) {
                            this.vehicleDetails.put("ModelName", this.vehicleModels.get(modelCode));
                            this.vehicleDetails.put("ModelCode",modelCode);
                        }

                }
            }
            getSubUrlData(companyName);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getSubUrlData(String companyName) {
        //Sub Url
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder subData = new StringBuilder();
        String baseUrl = "https://www.partslink24.com/";
        subData.append(baseUrl).append(this.vehicleParent).append("/").append(companyName+"_parts").append("/").append(this.modelLinks.get("MICRA"));
        try {
            URL url = new URL(subData.toString());
            String jsonString = getJsonData(url);
            JsonNode subGroupJsonNode = mapper.readTree(jsonString);
            JsonNode childNode = subGroupJsonNode.get("vCfgData").get("children");

            //For Micra K12E model
            String carBody = childNode.get(1).get("name").asText();
            String engine = childNode.get(1).get("children").get(1).get("name").asText();
            String grade = childNode.get(1).get("children").get(1).get("children").get(0).get("name").asText();
            String gearBox = childNode.get(1).get("children").get(1).get("children").get(0).get("children").get(0).get("name").asText();
            String distributionArea =  childNode.get(1).get("children").get(1).get("children").get(0).get("children").get(0).get("children").get(0).get("name").asText();
            String type = childNode.get(1).get("children").get(1).get("children").get(0).get("children").get(0).get("children").get(0).get("children").get(0).get("name").asText();

            this.vehicleDetails.put("CarBody",carBody);
            this.vehicleDetails.put("Engine",engine);
            this.vehicleDetails.put("Grade",grade);
            this.vehicleDetails.put("GearBox",gearBox);
            this.vehicleDetails.put("DistributionArea",distributionArea);
            this.vehicleDetails.put("Type",type);


            //For SubGroup Data
            String subGroupUrl = childNode.get(1).get("children").get(1).get("children").get(0).get("children").get(0).get("children").get(0).get("children").get(0).get("url").asText();
            URL groupUrl = new URL(baseUrl + this.vehicleParent + "/" + companyName + "_parts" + "/" + subGroupUrl);
            Document subgroupDoc = Jsoup.connect(groupUrl.toString()).get();
            Node subGroupList = subgroupDoc.childNode(2).childNode(3).childNode(2).childNode(0).childNode(0).childNode(0).childNode(1).childNode(0).childNode(1);
            String mainGroupName = subGroupList.childNode(0).childNode(1).childNode(0).childNode(0).outerHtml();
            this.vehicleDetails.put("MainGroupName",mainGroupName);
            String mainGroupJsonUrl = subGroupList.childNode(0).attr("jsonurl");
            URL mainGroupLink = new URL(baseUrl + this.vehicleParent + "/" + companyName + "_parts" + "/" + mainGroupJsonUrl);
            String mainGroupJsonString = getJsonData(mainGroupLink);
            JsonNode mainGroupJsonNode = mapper.readTree(mainGroupJsonString);
            JsonNode subgroupsArray = mainGroupJsonNode.get("subgroups");
            List<JsonNode> groupNode = new ArrayList<>();
            for(int i=0;i<subgroupsArray.size();i++){
                if(subgroupsArray.get(i).get("caption").asText().equals("CAMSHAFT & VALVE MECHANISM")){
                    groupNode.add(subgroupsArray.get(i));
                }
            }
            if(groupNode.size()>1){
                this.vehicleDetails.put("SubGroupCode",groupNode.get(0).get("code").asText());
                this.vehicleDetails.put("SubGroupName",groupNode.get(0).get("caption").asText());
                this.vehicleDetails.put("SectionCode",groupNode.get(1).get("code").asText());
                this.vehicleDetails.put("SectionName",groupNode.get(1).get("caption").asText());
                String restrictionHtml = groupNode.get(1).get("restrictionHtml").asText();
                Document res = Jsoup.parse(restrictionHtml);
                Elements el = res.select("span");
                String restrictions = el.get(el.size()-1).childNode(0).outerHtml();
                this.vehicleDetails.put("Restrictions",restrictions);
                String restrictionFromDate = el.get(0).childNode(0).outerHtml();
                String restrictionToDate = el.get(2).childNode(0).outerHtml();
                this.vehicleDetails.put("FromDate",restrictionFromDate);
                this.vehicleDetails.put("ToDate",restrictionToDate);
            }

            exportVehicleDetailstoExcel();
            System.out.println(vehicleDetails.entrySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportVehicleDetailstoExcel() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Nissan Vehicle Details");
        Map<String, Object[]> data = new LinkedHashMap<>();
        data.put("0", this.vehicleDetails.keySet().toArray());
        data.put("1",this.vehicleDetails.values().toArray());
        int rownum = 0;
        for (String key : data.keySet()) {
            Row row = sheet.createRow(rownum++);
            Object[] objArr = data.get(key);
            String[] stringArray = Arrays.copyOf(objArr, objArr.length, String[].class);
            int cellnum = 0;
            for (String obj : stringArray) {
                Cell cell = row.createCell(cellnum++);
                cell.setCellValue(obj);
            }
        }

        try {
            FileOutputStream out
                    = new FileOutputStream(new File("src/main/resources/excel/Nissan_parts.xls"));
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getJsonData(URL url){
        HttpURLConnection http = null;
        StringBuilder jsonString = new StringBuilder();
        try {
            http = (HttpURLConnection)url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    http.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                jsonString.append(inputLine);
            in.close();
            http.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString.toString();
    }
}
