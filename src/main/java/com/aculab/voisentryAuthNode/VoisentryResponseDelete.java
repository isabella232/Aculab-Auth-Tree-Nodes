/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import java.util.ArrayList;
import java.util.List;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to process Update response, inherits from the VoisentryResponse class.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryResponseDelete extends VoisentryResponse {
    
    private List<String> enrolids;


    public VoisentryResponseDelete (String response) throws NodeProcessException {
        
        super(response);
        
        if (this.status != VoisentryResponseCode.ALL_GOOD) {
            return;
        }

        try {

            JSONObject jsonResult   = new JSONObject(this.result);
            JSONArray  jsonEnrolids = jsonResult.getJSONArray("enrolids");
            
            this.enrolids = new ArrayList<String>();
            for (int i = 0; i < jsonEnrolids.length(); i++) {
                this.enrolids.add(jsonEnrolids.getString(i));
            }

        } catch (JSONException e) {
            Logger logger = LoggerFactory.getLogger(VoisentryResponseDelete.class);
            logger.error("Exception message: " + e.getMessage());
            throw new NodeProcessException(e);
        }
           
    }
    
    @Override
    public int getStatus() {
        return this.status;
    }
    
    public List<String> getEnrolids() {
        return this.enrolids;
    }
    
}