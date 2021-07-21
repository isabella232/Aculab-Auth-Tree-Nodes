![Aculab Voisentry Logo](https://www.aculab.com/images/VoiSentry_logo_trans_small.png)
# VoiSentry Integration
### Identity Verification
Document version: 1.0 (July 2021)

# Table of Contents
1. Voisentry / Forgerock Integrated Solution Overview
2. Voisentry Overview
3. Voisentry / Forgerock integration description
4. Voisentry / Forgerock authentication nodes description
5. Voisentry Service configuration
6. Voisentry and Forgerock Prerequisites
7. ForgeRock Authentication Tree
8. Testing Your Configuration

## 1. Solution Overview

Aculab VoiSentry is a voice biometric engine which allows authentication using a claimant’s voice sample.
ForgeRock OpenAM software allows various methods of user’s authentication.
Integration of both solutions allows for the user to be authenticated by providing a voice sample.
The above is possible by using custom OpenAM authentication nodes which utilize VoiSentry functionality.

## 2. Voisentry Overview

Groundbreaking advances in Articial Intelligence (AI) and the science of Big Data, give VoiSentry the ability to identify a speaker, rapidly and reliably.
Aculab's speaker identification and verification (ID&V) system captures tens of thousands of unique voice and speech characteristics. VoiSentry uses a hybrid approach, leveraging the best of state-of-the-art artificial neural networks (ANNs) and analytical linguistic and signal processing technology, to achieve frictionless, multilingual authentication in real time.
Changing the future of remote authentication.

For more information, please go to [Voisentry](https://www.aculab.com/products/voice-biometrics/)

## 3. Voisentry / Forgerock integration description

![ForgeRock Voisentry integration](https://github.com/arturj83/AculabAuthNode/blob/main/images/forgerock_int.jpg?raw=true)

### Technical Process Steps:
1. User accesses his/her application
2. User's application interacts with ForgeRock via web redirection or REST API
3. Based on the **Authentication Tree** policy, Forgerock connects with the Voisentry server to get the verification result based on gathered voice sample
4. The verification result is being sent back to the application if REST API was used or displayed directly on the ForgeRock server if redirection was used

## 4. Voisentry / Forgerock authentication nodes description

The authentication nodes that create authentication process are as follows:
-	Voisentry Audio Collector – collects a voice sample from the user via a web interface and saves it in the user’s space
-	Voisentry EnrollID Collector – collects the enrolid used as the user’s identification number and saves it in the user’s space (other options include the use of username or specified field in the id repository as the enrolid)
-   Voisentry Check Voiceprint – checks if the specified voiceprint exists on the Voisentry external server
-   Voisentry Delete Voiceprint – deletes the specified voiceprint from the Voisentry external server
-   Voisentry Create Voiceprint – gathers a voice sample(s) from the user’s space, sends a enrollment request tp the Voisentry external server in order to create the voiceprint
-	Voisentry Verify User – gathers a voice sample from the user’s space, sends a verification request to the Voisentry external server and receives the verification result
-	Voisentry Update Voiceprint – gathers a voice sample from the user’s space, sends the update request to the Voisentry external server and receives the update result

### Voisentry Audio Collector node

The VoiSentry Audio Collector node collects a voice sample from the user and saves it in the user’s space to be used by other nodes for verification purposes.
The collection process is performed via the web interface with the use of Media Web APIs (getUserMedia()).
The OpenAM software package needs to be placed on the secure server (HTTPS/TLS) as this allows the proper functionality of the Media Web APIs.

This node uses the script callback to display the javascript code used to record the audio sample. This javascript code also displays the html code including '\<div\>' elements that can be used to display the configurable text or/and elements using HTML. Please, see the configuration below.

The configuration options:

* **Node Name** - ```Voisentry Audio Collector``` (Any name can be chosen)
* **Clear the audio sources** - Setting it to enabled will delete all previous audio sources kept in the shared state
* **Recording time** - Sets recording time in miliseconds. If 0 recording time is infinite and stopped with the click of the recording button
* **Header contents** - The contents of the ‘aculab_header’ div element. Text or/and HTML code can be used as the filler. The class name for the CSS is ‘acuDivHeader’
* **Footer contents** - The contents of the ‘aculab_footer’ div element. Text or/and HTML code can be used as the filler. The class name for the CSS is ‘acuDivFooter’
* **Mic button contents** - The contents of the ‘acuDivMic’ div element. Text or/and HTML code can be used as the filler. The class name for the CSS is ‘acuDivMic’. If this field is left blank, the default html code is being used, please see below.
* **CSS File URL** - The link to the CSS file styling the html elements.
The file needs to be placed on the secure server
* **Javascript File URL** - The link to the custom javascript file. The custom javascript code does not replace the default one, however it is possible to disable the functionalities of the default code with the custom one. Examples below. The file needs to be placed on the secure server

The default voisentryAudioCollector.js file can be found here:  
https://webdemo.aculab.com/forgerock/js/voisentryAudioCollector.js

The example CSS file voisentryAudioCollector.css file can be found here:  
https://webdemo.aculab.com/forgerock/css/voisentryAudioCollector.css

This example shows how to override the default behavior of the recording button:
-	Remove the default click listener:  
```
var customRecordButton = document.getElementById("acuDivMic");
customRecordButton.removeEventListener('click', pressRecording);
```
-	Add the custom click listener:  
```
customRecordButton.addEventListener('click', customPressRecording);
```

The ```customPressRecording()``` function shows how you can apply a custom animation along with the background color change on recording button click.  
Please, note the ```startRecording()``` and ```stopRecording()``` functions that are used to start and stop recording respectively.
These functions needs to be used when overriding the default recording behavior.

The default HTML code for the ‘acuMicDiv’ element:  
```
<div class='object'>
  <div class='button'></div>
  <div class='button' id='circlein'>
  <svg " + "class='mic-icon' version='1.1' xmlns='http://www.w3.org/2000/svg' " + "xmlns:xlink='http://www.w3.org/1999/xlink'
   x='0px' y='0px' viewBox='0 0 1000 1000' " + "enable-background='new 0 0 1000 1000' xml:space='preserve'
   style='fill:#FFFFFF'><g><path" + "d='M500,683.8c84.6,0,153.1-68.6,153.1-153.1V163.1C653.1,78.6,584.6,10,500,10c-84.6," +
   "0-153.1,68.6-153.1,153.1v367.5C346.9,615.2,415.4,683.8,500,683.8z M714.4,438.8v91.9C714" +
   ".4,649,618.4,745,500,745c-118.4,0-214.4-96-214.4-214.4v-91.9h-61.3v91.9c0,141.9,107.2," +
   "258.7,245,273.9v124.2H346.9V990h306.3v-61.3H530.6V804.5c137.8-15.2,245-132.1,245-273" +
   ".9v-91.9H714.4z'/></g>
  </svg>
  </div>
</div>
```


### Voisentry EnrollID Collector node

The VoiSentry EnrollID Collector node collects the enroll ID from a user via the web interface and saves it in their shared state, in the same way a username is shared between nodes.
The enroll ID is used in the verify and update node, please see below.

The configuration options:

* **Node Name** - ```Voisentry EnrollID Collector``` (Any name can be chosen)


### Voisentry Check Voiceprint node

The VoiSentry Check Voiceprint node checks if the voiceprint with the specified enroll id is present on the Voisentry server.
The outcome True is chosen if the voiceprint exists, if the voiceprint does not exists, the outcome False is chosen. It is possible to configure the outcome for each of the Voisentry errors codes. The outcome will be sent to the specific error code upon receipt of this error code from the Voisentry server. If an error code is returned from the VoiSentry server for which a custom outcome has not been configured, the default failure outcome will be chosen.

The configuration options:
* **Node Name** - ```Voisentry Check Voiceprint``` (Any name can be chosen)
* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server. If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- taken from the SERVICE configuration (needs to be configured beforehand)  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```
* **Error Code Outcomes** - Configures the outcome for specific error code. Upon reception of the specific error code from the Voisentry server, the result is being sent to this error code outcome if configured, if not to the Failure outcome. Allows for more detailed authentication process flow.


### Voisentry Delete Voiceprint node

The VoiSentry Delete Voiceprint node deletes the voiceprint for the specified enroll id.
If the deletion process was successful, the outcome True is chosen, if the deletion was unsuccessful, the outcome False is chosen. It is possible to configure the outcome for each of the Voisentry errors codes. The outcome will be sent to the specific error code upon receipt of this error code from the Voisentry server. If an error code is returned from the VoiSentry server for which a custom outcome has not been configured, the default failure outcome will be chosen.

The configuration options:
* **Node Name** - ```Voisentry Delete Voiceprint``` (Any name can be chosen)
* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server. If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- taken from the SERVICE configuration (needs to be configured beforehand)  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```
* **Error Code Outcomes** - Configures the outcome for specific error code. Upon reception of the specific error code from the Voisentry server, the result is being sent to this error code outcome if configured, if not to the Failure outcome. Allows for more detailed authentication process flow.


### Voisentry Create Voiceprint node

The VoiSentry Create Voiceprint node gathers voice sample(s) from the shared state, sends the enroll request to the Voisentry server along with the enroll ID in order to create the voiceprint.
The outcome True is chosen if the voiceprint was created esuccessfully, if not then the outcome False is chosen. It is possible to configure the outcome for each of the Voisentry errors codes. The outcome will be sent to the specific error code upon receipt of this error code from the Voisentry server. If an error code is returned from the VoiSentry server for which a custom outcome has not been configured, the default failure outcome will be chosen.

The configuration options:
* **Node Name** - ```Voisentry Create Voiceprint``` (Any name can be chosen)
* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server. If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- taken from the SERVICE configuration (needs to be configured beforehand)  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field  
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```
* **Error Code Outcomes** - Configures the outcome for specific error code. Upon reception of the specific error code from the Voisentry server, the result is being sent to this error code outcome if configured, if not to the Failure outcome. Allows for more detailed authentication process flow.


### Voisentry Verify User node

The VoiSentry Verify User node gathers a voice sample from the shared state, sends the verify request to the Voisentry server along with the enroll ID and gets the verification result.
The outcome is True if the verification passes. If the verification passes but Presentation Attack Detection (PAD) was enabled and detected, or if the verification fails the outcome is Failure. It is also possible to configure an outcome for each of the Voisentry errors codes. The outcome will be sent to the specific error code upon receipt of this error code from the Voisentry server. If an error code is returned from the VoiSentry server for which a custom outcome has not been configured, the default failure outcome will be chosen.

The configuration options:
* **Node Name** - ```Voisentry Verify``` (Any name can be chosen)
* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server. If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- taken from the SERVICE configuration (needs to be configured beforehand)  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field  
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```
* **Update voiceprint after successful verification** - Enabling this option will cause the Update request to be sent automatically (with the same audio sample as for the verification request) to the Voisentry server if the verification passed. Please, refer to the Voisentry manual to get more information about the purpose of this option.
* **Enable PAD** - Enable PAD (presentation attack detection). If the verification passes but the PAD was detected, the result will be sent to the Failure outcome. Please, contact voisentry@aculab.com to get more information about the purpose of this option.
* **Text Independent mode** - Sets the verification algorithm to work in text-independent mode, which allows for any phrase/statement to be spoken during the verification. This requires more voice data to be provided during the voiceprint creation process. Please, contact voisentry@aculab.com to get more information.
* **Error Code Outcomes** - Configures the outcome for specific error code. Upon reception of the specific error code from the Voisentry server, the result is being sent to this error code outcome if configured, if not to the Failure outcome. Allows for more detailed authentication process flow.


### Voisentry Update Voiceprint node

The VoiSentry Update Voiceprint node gathers a voice sample from the shared state, sends the update request to the Voisentry server along with the enroll ID and gets the update result.
The outcome True is chosen if the update was successful. It is possible to configure the outcome for each of the Voisentry errors codes. The outcome will be sent to the specific error code upon receipt of this error code from the Voisentry server. If an error code is returned from the VoiSentry server for which a custom outcome has not been configured, the default failure outcome will be chosen.

The configuration options:
* **Node Name** - ```Voisentry Update``` (Any name can be chosen)
* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server. If this field is left blank, the value will be taken from the Voisentry Service that needs to be configured in the Services menu beforehand.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- taken from the SERVICE configuration (needs to be configured beforehand)  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field  
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```
* **Error Code Outcomes** - Configures the outcome for specific error code. Upon reception of the specific error code from the Voisentry server, the result is being sent to this error code outcome if configured, if not to the Failure outcome. Allows for more detailed authentication process flow.


## 5. Voisentry Service configuration

Voisentry service provides configuration for all the nodes in the tree so there is no need to populate each node with the same configuration information. In other words, service is used to share configuration between the nodes

The configuration options:

* **VoiSentry Node URL** - The URL of the Voisentry server to send the requests to. The servers can be clustered hence the node term.
* **VoiSentry Dataset Key** - The dataset key configured on the Voisentry server.
* **Get the enrolid from** - The source of the enroll ID. It can be:  
-- used as username (username collector node needs to be used in the tree to save it in the shared state)  
-- taken from the enroll ID collector node (needs to be used in the tree to save it in the shared state)  
-- taken from the Id repository specific field  
* **Id repository enrolid field name** - If the Id repository option was chosen in the above config option, this option defines the field name in the id repository which should be used as the enroll ID, for example ```employeeNumber```


## 6. Voisentry and Forgerock Prerequisites

In order to use the voice biometric functionality, the Aculab Voisentry server needs to be deployed and configured.
Please, contact voisentry@aculab.com in order to obtain the information on how to deploy the Voisentry external server and costs involved with it.
The official information about the product can be found  on Aculab’s website:
https://www.aculab.com/products/voice-biometrics/

The instructions on how to use the OpenAM software to perform the authentication process is not part of this document. Please, refer to the official documentation which can be found here:
https://backstage.forgerock.com/docs/am/6.5

## 7. Forgerock Authentication Tree

### Authentication Tree Diagram
You can create many types of authentication tree to match your specific deployment. Below are two simple variants than can typically be used for simple setups.

![ForgeRock Authentication Tree](https://github.com/arturj83/AculabAuthNode/blob/main/images/node_tree_1.png?raw=true)

The above example creates the voiceprint for the specified enroll id, it works in the following way:
-	Requests the username from the user input
-   Checks if the voiceprint for this username is already created
-   If the voiceprint for this username is already created, it asks if the user wants to delete the voiceprint and create it again
-   If the voiceprint for this username is not already created, it requests 3 recordings of a voice sample, which will be used for the creation of the voiceprint.
-	Sends the voice samples in a enroll request to the Voisentry server.
-	If the creation is successful, the user is redirected to his account
-	Upon receipt of error 445, Bad Audio, the user is asked for the voice samples again.
-   In case of any other error, the creatio process is considered failed


![ForgeRock Authentication Tree](https://github.com/arturj83/AculabAuthNode/blob/main/images/node_tree_2.png?raw=true)

The above example verifies the user by the use of his voice sample, it works the following way:
-	Requests the username from the user input
-	Requests the recording of a voice sample, which will be used for verification.
-	Sends the voice sample in a verify request to the Voisentry server.
-	If the verification passes the user is considered successfully authenticated
-	If the verification fails, the user is verified via a secondary method, in this case, via their password.
-	Upon receipt of error 445, Bad Audio, the user is asked for a voice sample again.
-	If secondary verification is successful, and the password and username match the one stored in the id repository, the voiceprint is updated.
-	Whether the update passed or failed the user is considered successfully authenticated.

## 8. Testing Your Configuration

In order to obtain the Voisentry URL and Voisentry Dataset Key for the nodes configuration, please contact voisentry@aculab.com

Before testing the Voisentry nodes, it is assumed that the user accounts are already created in the repository.

