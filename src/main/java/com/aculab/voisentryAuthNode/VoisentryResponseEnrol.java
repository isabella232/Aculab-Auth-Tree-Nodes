/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to process Verify response, inherits from the VoisentryResponse class.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryResponseEnrol extends VoisentryResponse {

    private double     confidence;
    private boolean    verified;
    private String     pad_type;


    public VoisentryResponseEnrol(String response) throws NodeProcessException {

        super(response);

        if (this.status != VoisentryResponseCode.ALL_GOOD) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(VoisentryResponseEnrol.class);
        try {

            JSONObject jsonResult = new JSONObject(this.result);
            JSONObject jsonEnrol  = new JSONObject(jsonResult.getString(VoisentryResponseConstants.ENROLMENT));

        } catch (JSONException e) {
            logger.error("Exception message: " + e.getMessage());
            throw new NodeProcessException(e);
        }

    }

    @Override
    public int getStatus() {
        return this.status;
    }

}