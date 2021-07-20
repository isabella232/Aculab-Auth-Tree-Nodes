/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
// */

package com.aculab.voisentryAuthNode;

import javax.inject.Inject;

import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOException;
import com.sun.identity.shared.validation.URLValidator;
import com.sun.identity.sm.RequiredValueValidator;
import com.sun.identity.sm.SMSException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonValueException;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.OutcomeProvider;

import org.forgerock.openam.sm.AnnotatedServiceRegistry;
import org.forgerock.util.i18n.PreferredLocales;

/**
 * A node that sends the Verify request to the Voisentry server
 * @author artur.jablonski@aculab.com
 */
@Node.Metadata(outcomeProvider  = VoisentryNodeVerify.VerifyNodeOutcomeProvider.class,
               configClass      = VoisentryNodeVerify.Config.class)
public class VoisentryNodeVerify implements Node {

    private final Logger logger = LoggerFactory.getLogger(VoisentryNodeVerify.class);
    private final VoisentryService serviceConfig;
    private final Config config;
    private final Realm realm;
    

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The voisentry node URL used to receive the audio sample.
         */
        @Attribute(order = 10, validators = URLValidator.class)
        default String voisentryNodeUrl() {
            return "";
        }

        /**
         * The voisentry dataset key used to store the voiceprints.
         */
        @Attribute(order = 20)
        default String voisentryDatasetKey() {
            return "";
        }
        
        /**
         * The enrolid source.
         */
        @Attribute(order = 30)
        default VoisentryConstants.GetEnrolId getEnrolId() {
            return VoisentryConstants.GetEnrolId.SERVICE;
        }

        /**
         * The id repository field name for the enrolid source
         */
        @Attribute(order = 40)
        default String idRepoEnrolidName() {
            return "";
        }
        
        /**
         * Automatically update the voiceprint with the audio sample after successful verification.
         */
        @Attribute(order = 50, validators = RequiredValueValidator.class)
        default boolean updateVoiceprint() {
            return false;
        }
        
        /**
         * Enable the PAD for the verification request
         */
        @Attribute(order = 60, validators = RequiredValueValidator.class)
        default boolean enablePAD() {
            return false;
        }

        /**
         * Send the verification request in text-independent mode, default is text-dependent
         */
        @Attribute(order = 70, validators = RequiredValueValidator.class)
        default boolean textIndependent() {
            return false;
        }
        
        /**
         * The configurable error outcomes for this node
         */
        @Attribute(order = 80)
        default Set<VoisentryErrorCode> errorCodeOutcomes() {
            return new LinkedHashSet<>();
        }
                
    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public VoisentryNodeVerify(@Assisted Config config, @Assisted Realm realm, AnnotatedServiceRegistry serviceRegistry) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
        try {
            this.serviceConfig = serviceRegistry.getRealmSingleton(VoisentryService.class, realm).get();
        } catch (SSOException | SMSException e) {
            throw new NodeProcessException(e);
        }
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        
        logger.info("VoisentryVerifyNode started");

        //get voisentry node URL
        String voisentryNodeUrl = VoisentryConstants.getVoisentryNodeUrl(config.voisentryNodeUrl(),
                                                                         serviceConfig.voisentryNodeUrl(),
                                                                         logger);

        //get voisentry dataset key
        String voisentryDatasetKey = VoisentryConstants.getVoisentryDatasetKey(config.voisentryDatasetKey(),
                                                                               serviceConfig.voisentryDatasetKey(),
                                                                               logger);
        
        logger.info("Get the shared state");
        JsonValue sharedState = context.sharedState;
        
        //get the enrolId from the config
        String enrolId = VoisentryConstants.getEnrolIdFromConfig(sharedState,
                                                                 realm,
                                                                 config.getEnrolId(),
                                                                 config.idRepoEnrolidName(),
                                                                 serviceConfig,
                                                                 logger);

        if (enrolId == null || enrolId.isEmpty()) {
            throw new NodeProcessException("Failed to get the enrolid from the config");
        }

        logger.info("Get the audiosource from the shared state: ");
        List<String> audioSource = sharedState.get(VoisentryConstants.AUDIOSOURCE).asList(String.class);
        if (audioSource == null || audioSource.isEmpty()) {
            throw new NodeProcessException("Audio sample not provided");
        }
        logger.debug("Audiosource samples: " + audioSource.size());
        
        //create voisentry request object
        VoisentryRequest vsRequest = new VoisentryRequest(voisentryNodeUrl,
                                                          voisentryDatasetKey);
        VoisentryResponseVerify vsResponse = vsRequest.sendVerify(enrolId, audioSource, config.textIndependent());
        
        int responseCode = vsResponse.getStatus();
        if (responseCode == VoisentryResponseCode.ALL_GOOD) {
                
            //check the verification result
            logger.info("Verified result: " + vsResponse.getVerified());
            boolean verified = false;
            if (vsResponse.getVerified()) {
                verified = true;
                
            }
            
            //if PAD enabled check if detected
            if (config.enablePAD()) {
                logger.info("PAD enabled, checking if present:");
                if (vsResponse.getPadType() != null) {
                    logger.error("PAD detected: " + vsResponse.getPadType());
                    verified = false;
                }
            }
            
            //check if verified
            if (verified) {
                logger.info("Verification successful");
                
                if (config.updateVoiceprint()) {
                    logger.info("Updating the voiceprint");
                    VoisentryResponseUpdate vsResponseUpdate = vsRequest.sendUpdate(enrolId, audioSource);
                    if (vsResponseUpdate.getStatus() != VoisentryResponseCode.ALL_GOOD) {
                        logger.error("Update error: " + vsResponseUpdate.getStatus());
                    }
                    logger.info("Update success");
                }
                
                return goTo("true").build();
                
            }
            logger.info("Verification unsuccessful");
                
        } else {
            
            logger.error("Verification error: "  + responseCode);
            
            for (VoisentryErrorCode errorCode : config.errorCodeOutcomes()) {
                if (errorCode.getCode() == responseCode) {
                    return goTo("Err"+errorCode.getCode()).build();
                }
            }
            
        }
        
        return goTo("false").build();
                
    }
    
    private Action.ActionBuilder goTo(String outcome) {
        return Action.goTo(outcome);
    }
    
    public static class VerifyNodeOutcomeProvider implements OutcomeProvider {
        private static final String BUNDLE = VoisentryNodeVerify.class.getName().replace(".", "/");
        
        private static final Logger logger = LoggerFactory.getLogger(VoisentryNodeVerify.class);

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            
            List<Outcome> outcomes = Arrays.asList(new Outcome("true",  bundle.getString("True")),
                                                   new Outcome("false", bundle.getString("False")));
            
            try {
                logger.debug("Getting the error outcomes list");
                List<Outcome> errorOutcomes = nodeAttributes.get("errorCodeOutcomes").required()
                                                            .asList(String.class)
                                                            .stream()
                                                            .map(outcome -> new Outcome(outcome, outcome.replace("Err", "Error ")))
                                                            .collect(Collectors.toList());
                
                logger.debug("Adding the error outcomes list to the node outcome: " + errorOutcomes.toString());
                errorOutcomes.add(0, new Outcome("false", bundle.getString("False")));
                errorOutcomes.add(0, new Outcome("true",  bundle.getString("True")));
                
                return errorOutcomes;
            } catch (JsonValueException e) {
                logger.error("Outcome exception: " + e.getMessage());
            }
            logger.debug("Returning outcomes: " + outcomes.toString());
            return outcomes;
            
        }
        
    }
    
}
