/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Class used to send http requests to Voisentry server.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryRequest {
    
    private final Logger logger = LoggerFactory.getLogger(VoisentryRequest.class);
    
    private final String voisentryNodeUrl;
    private final String voisentryDatasetKey;


    public VoisentryRequest(String voisentryNodeUrl, String voisentryDatasetKey) {
        this.voisentryNodeUrl    = voisentryNodeUrl;
        this.voisentryDatasetKey = voisentryDatasetKey;
    }
    
    //send get request
    public VoisentryResponseCheck sendCheck(String enrolId) throws NodeProcessException {
        
        logger.debug("Sending check request...");
        
        String reqUrl = this.voisentryNodeUrl + "/check?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendGetRequest(reqUrl);
        logger.debug("Send check get request result: " + result);
        
        
        return (new VoisentryResponseCheck(result));
        
    }
    
    //send enrol request
    public VoisentryResponseEnrol sendEnrol(String enrolId, List<String> audioSource) throws NodeProcessException {
        
        logger.debug("Sending enrol request...");
        
        String reqUrl = this.voisentryNodeUrl + "/enrol?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendPostRequest(reqUrl, createMpartBody(audioSource));
        logger.debug("Send enrol post request result: " + result);
        
        
        return (new VoisentryResponseEnrol(result));
        
    }

    //send verify request
    public VoisentryResponseVerify sendVerify(String enrolId,
                                              List<String> audioSource,
                                              boolean textIndependent) throws NodeProcessException {
        
        logger.debug("Sending verify request...");
        
        String reqUrl = this.voisentryNodeUrl + "/verify?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        if (textIndependent) {
            reqUrl += "&textdependent=F";
        }
        
        String result = this.sendPostRequest(reqUrl, createMpartBody(audioSource));
        logger.debug("Send verify post request result: " + result);
        
        
        return (new VoisentryResponseVerify(result));
        
    }

    //send update request
    public VoisentryResponseUpdate sendUpdate(String enrolId, List<String> audioSource) throws NodeProcessException {
        
        logger.debug("Sending update request...");
        
        String reqUrl = this.voisentryNodeUrl + "/update?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendPostRequest(reqUrl, createMpartBody(audioSource));
        logger.debug("Send update post request result: " + result);
        
        
        return (new VoisentryResponseUpdate(result));
        
    }
    
    //send delete request
    public VoisentryResponseDelete sendDelete(String enrolId) throws NodeProcessException {
        
        logger.debug("Sending delete request...");
        
        String reqUrl = this.voisentryNodeUrl + "/delete?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendGetRequest(reqUrl);
        logger.debug("Send delete get request result: " + result);
        
        
        return (new VoisentryResponseDelete(result));
        
    }
    
    //send get request
    private String sendGetRequest (String url) throws NodeProcessException {

        String result = "";
        
        try {
        
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                
                HttpGet httpget = new HttpGet(url);
                
                logger.debug("executing get request " + httpget.getRequestLine());
                try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                    
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(response.getEntity());
                    }
                    EntityUtils.consume(resEntity);
                    
                }
                
            }
            
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new NodeProcessException(e);
        }
        
	return result;
        
    }

    //send post request
    private String sendPostRequest(String url, MultipartEntityBuilder mpart) throws NodeProcessException {

        try {

            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

                HttpPost httppost = new HttpPost(url);

                HttpEntity reqEntity = mpart.build();
                httppost.setEntity(reqEntity);

                logger.debug("executing post request " + httppost.getRequestLine());
                try (CloseableHttpResponse response = httpclient.execute(httppost)) {

                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        return EntityUtils.toString(response.getEntity());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new NodeProcessException(e);
        }
        return null;

    }
    
    
    //create multipart body
    private MultipartEntityBuilder createMpartBody (List<String> audioSource) {
        
        MultipartEntityBuilder mpart = MultipartEntityBuilder.create();
        if (audioSource != null && !audioSource.isEmpty()) {
            for (int i = 0; i < audioSource.size(); i++) {
                String argName = "source";
                if (audioSource.size() > 1) {
                    argName += "" + (i+1);
                }
                mpart.addBinaryBody(argName,
                                    Base64.getDecoder().decode(audioSource.get(i)),
                                    ContentType.DEFAULT_BINARY,
                                    "audiosource" + (i+1) + ".wav");
                
            }
 
        }
        
        return mpart;
        
    }
    
}
