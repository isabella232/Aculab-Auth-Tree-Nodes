/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;

import com.iplanet.sso.SSOException;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
import java.util.Collections;


/**
 * Constant values used by VoisentryNode.
 * @author artur.jablonski@aculab.com
 */
public final class VoisentryConstants {
    
    /**
     * Private constructor.
     */
    private VoisentryConstants() {
    }
    
    /** The node version. */
    public static final String VERSION = "1.0.0";
    
    /** The enrolId. */
    public static final String ENROLID = "voisentry_enrolid";
    
    /** The audiosource. */
    public static final String AUDIOSOURCE = "voisentry_audiosource";
    
    /** The enrolid source config. */
    public static final byte ENROLID_USERNAME    = 0;
    public static final byte ENROLID_SHAREDSTATE = 1;
    public static final byte ENROLID_IDREPO      = 2;
    
    /** Config - enrolid source. */
    public enum GetEnrolId {
        SERVICE,
        USERNAME,
        ENROLID,
        ID_REPO
    }
    
    /** Config - service enrolid source. */
    public enum ServiceGetEnrolId {
        USERNAME,
        ENROLID,
        ID_REPO
    }

    public static final String getVoisentryNodeUrl (String configNodeUrl,
                                                    String serviceNodeUrl,
                                                    Logger logger) throws NodeProcessException {
        
        logger.info("Get the Voisentry node URL: ");
        
        String voisentryNodeUrl = configNodeUrl;
        if (voisentryNodeUrl == null || voisentryNodeUrl.isEmpty()) {
            voisentryNodeUrl = serviceNodeUrl;
        }
        if (voisentryNodeUrl == null || voisentryNodeUrl.isEmpty()) {
            throw new NodeProcessException("Failed to get voisentry node URL from the config");
        }
        logger.debug("Voisentry node URL: " + voisentryNodeUrl);
        return voisentryNodeUrl;

    }

    public static final String getVoisentryDatasetKey (String configDatasetKey,
                                                       String serviceDatasetKey,
                                                       Logger logger) throws NodeProcessException {
        
        logger.info("Get the Voisentry dataset key");
        
        String voisentryDatasetKey = configDatasetKey;
        if (voisentryDatasetKey == null || voisentryDatasetKey.isEmpty()) {
            voisentryDatasetKey = serviceDatasetKey;
        }
        if (voisentryDatasetKey == null || voisentryDatasetKey.isEmpty()) {
            throw new NodeProcessException("Failed to get voisentry dataset key from the config");
        }
        logger.debug("Voisentry dataset key: " + voisentryDatasetKey);
        return voisentryDatasetKey;

    }
    
    
    public static final String getEnrolIdFromConfig (JsonValue          sharedState,
                                                     Realm                    realm,
                                                     GetEnrolId       configEnrolId,
                                                     String           idRepoEnrolId,
                                                     VoisentryService serviceConfig,
                                                     Logger                  logger) throws NodeProcessException {
        
        logger.info("Get the Enrol ID from the config");
        
        String enrolId           = null;
        String idRepoEnrolidName = null;
                
        byte enrolidSource = VoisentryConstants.ENROLID_USERNAME;
        
        //get the enrolid source config from the service config
        if (configEnrolId == VoisentryConstants.GetEnrolId.SERVICE) {
           
            if (serviceConfig.getEnrolId() == VoisentryConstants.ServiceGetEnrolId.ENROLID) {
                enrolidSource = VoisentryConstants.ENROLID_SHAREDSTATE;
            }
            
            if (serviceConfig.getEnrolId() == VoisentryConstants.ServiceGetEnrolId.ID_REPO) {
                enrolidSource     = VoisentryConstants.ENROLID_IDREPO;
                idRepoEnrolidName = serviceConfig.idRepoEnrolidName();
            }
            
        }
        
        //get the enrolid source config from the node config
        if (configEnrolId == VoisentryConstants.GetEnrolId.ENROLID) {
            enrolidSource = VoisentryConstants.ENROLID_SHAREDSTATE;
        }
        if (configEnrolId == VoisentryConstants.GetEnrolId.ID_REPO) {
            enrolidSource = VoisentryConstants.ENROLID_IDREPO;
            idRepoEnrolidName = idRepoEnrolId;
        }
        
        if (enrolidSource == VoisentryConstants.ENROLID_SHAREDSTATE) {
            
            logger.info("Get the enrolid from the shared state");
            try {
                enrolId = sharedState.get(VoisentryConstants.ENROLID).asString();
            } catch (Exception e) {
                logger.error("Get enrolid exception: " + e.getMessage());
                throw new NodeProcessException(e);
            }
            
        } else {
            
            logger.info("Get the username from the shared state");
            String username = null;
            try {
                username = sharedState.get(USERNAME).asString();
                logger.debug("Username: " + username);
            } catch (Exception e) {
                logger.error("Get username exception: " + e.getMessage());
                throw new NodeProcessException(e);
            }
            if (username == null || username.isEmpty()) {
                throw new NodeProcessException("Username not provided");
            }
            
            if (enrolidSource == VoisentryConstants.ENROLID_USERNAME) {
                enrolId = username;
            } else {
                
                //Code to get the employee Number
                try {
                    logger.debug("Get enrolid from the id repo");
                    if (idRepoEnrolidName == null || idRepoEnrolidName.isEmpty()) {
                        throw new NodeProcessException("Id repository enrol id field name not provided");
                    }
                    
                    enrolId = IdUtils.getIdentity(username, realm.asPath(), Collections.singleton("uid")).getAttribute(idRepoEnrolidName).iterator()
                                     .next();
                } catch (IdRepoException | SSOException e) {
                    logger.error("Get id repo exception: " + e.getMessage());
                    throw new NodeProcessException(e);
                }
                
            }
        }
        
        logger.debug("Enrolid: " + enrolId);
        return enrolId;
        
    }
    
}
