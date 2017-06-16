/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.scmt.service;

import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.EXCHANGE_TRACTOR;
import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.QUEUE_DESK_DATA_MIGRATION;
import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.QUEUE_DESK_ATTACHMENT;
import static com.salesforce.scmt.utils.Utils.getPostParamsFromRequest;
import static java.lang.System.getenv;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.desk.java.apiclient.DeskClient;
import com.desk.java.apiclient.DeskClientBuilder;
import com.desk.java.apiclient.model.CustomField;
import com.desk.java.apiclient.model.Group;
import com.desk.java.apiclient.model.User;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.utils.*;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;

import spark.Request;
import spark.Response;

public class DeskService
{
    private static final String DESK_API_LOG_LEVEL = "DESK_API_LOG_LEVEL";
    private static final String DESK_API_LOG_LEVEL_NONE = "NONE";
    private static final String DESK_API_LOG_LEVEL_BASIC = "BASIC";
    private static final String DESK_API_LOG_LEVEL_HEADERS = "HEADERS";
    private static final String DESK_API_LOG_LEVEL_BODY = "BODY";
    private static final String DESK_API_MIGRATION_HEADER = "DESKCOM-SC-MIGRATION";

    private String _migrationId;
    private DeskClient _client;
    private Map<String, String> _clientSettings;
    private SalesforceService _sfService;
    
    // these are used for caching
    public List<Group> _deskGroups;
    public List<User> _deskUsers;
    public Map<Integer, String> _deskGroupId2Name;
    
    public DeskService(Map<String, Object> config)
    {
        this(config.get("deskUrl").toString(),
            config.get("consumerKey").toString(), config.get("consumerSecret").toString(),
            config.get("accessToken").toString(), config.get("accessTokenSecret").toString(),
            (config.containsKey("desk_migration_id") ? config.get("desk_migration_id").toString() : null),
            config.get("server_url").toString(), config.get("session_id").toString(),
            (config.containsKey("auditEnabled") ? Boolean.valueOf(config.get("auditEnabled").toString()) : false));
    }

    public DeskService(String deskUrl, String consumerKey, String consumerSecret, String accessToken,
        String accessTokenSecret, String migrationId, String serverUrl, String sessionId, Boolean auditEnabled)
    {
    	System.out.println(serverUrl+" "+ sessionId);
        // check if a desk migration id was passed
        if (migrationId != null)
        {
            setMigrationId(migrationId);
        }
        
        deskUrl = deskUrl.replaceFirst("(http|https):\\/\\/www\\.|www.|(http|https):\\/\\/", "");
        // create a desk client
        System.out.println("deskUrl" +deskUrl);

        createDeskClient(deskUrl, consumerKey, consumerSecret, accessToken, accessTokenSecret);
        
        // create Salesforce Service instance
        _sfService = new SalesforceService(serverUrl, sessionId);
        
        // check if auditEnabled was passed
        _sfService.setAuditFieldsEnabled(auditEnabled);
    }

    public SalesforceService getSalesforceService()
    {
        return _sfService;
    }
    
    public String getMigrationId()
    {
        return _migrationId;
    }
    
    public void setMigrationId(String migrationId)
    {
        _migrationId = migrationId;
    }

    public List<Group> getDeskGroups()
    {
        return _deskGroups;
    }

    public void setDeskGroups(List<Group> groups)
    {
        _deskGroups = groups;
    }

    public List<User> getDeskUsers()
    {
        return _deskUsers;
    }

    public void setDeskUsers(List<User> users)
    {
        _deskUsers = users;
    }
    
    public Map<Integer, String> getDeskGroupId2Name()
    {
        return _deskGroupId2Name;
    }
    
    public void setDeskGroupId2Name(Map<Integer, String> groupId2Name)
    {
        _deskGroupId2Name = groupId2Name;
    }
    
    private DeskClient createDeskClient(String deskUrl, String consumerKey, String consumerSecret,
        String accessToken, String accessTokenSecret)
    {
        // check if we need to create a desck client
        if (_client == null || !_client.getHostname().equalsIgnoreCase(deskUrl))
        {
            // DEBUG
            if (_client != null)
            {
                Utils.log("Static Desk Client Hostname: [" + _client.getHostname() + "], new Desk Hostname: [" + deskUrl + "]");
            }

            // check that required parameters are not empty
            if (deskUrl.isEmpty() || consumerKey.isEmpty() || consumerSecret.isEmpty() || accessToken.isEmpty()
                || accessTokenSecret.isEmpty())
            {
                throw new InvalidParameterException("All of the parameters are required!");
            }

            // save the settings so I can re-queue jobs
            _clientSettings = new HashMap<>();
            _clientSettings.put("deskUrl", deskUrl);
            _clientSettings.put("consumerKey", consumerKey);
            _clientSettings.put("consumerSecret", consumerSecret);
            _clientSettings.put("accessToken", accessToken);
            _clientSettings.put("accessTokenSecret", accessTokenSecret);

            // create client builder
            DeskClientBuilder clientBuilder = new DeskClientBuilder(deskUrl, consumerKey, consumerSecret, accessToken,
                accessTokenSecret);

            // set logging for desk client
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(Level.NONE); // default to NONE, which still produces a log, but very simple
            String deskApiLogLevel = getenv(DESK_API_LOG_LEVEL);

            // if a log level is specified in the environment config var, apply it
            if (deskApiLogLevel != null && !deskApiLogLevel.equalsIgnoreCase(DESK_API_LOG_LEVEL_NONE))
            {
                Utils.log("Setting logging level of Desk.com API to: [" + deskApiLogLevel + "]");
                if (deskApiLogLevel.equalsIgnoreCase(DESK_API_LOG_LEVEL_BASIC))
                {
                    logging.setLevel(Level.BASIC);
                }
                else if (deskApiLogLevel.equalsIgnoreCase(DESK_API_LOG_LEVEL_HEADERS))
                {
                    logging.setLevel(Level.HEADERS);
                }
                else if (deskApiLogLevel.equalsIgnoreCase(DESK_API_LOG_LEVEL_BODY))
                {
                    logging.setLevel(Level.BODY);
                }
            }

            // add the limit override header
            Interceptor requestHeader = new Interceptor()
            {
                @Override
                public com.squareup.okhttp.Response intercept(Interceptor.Chain chain) throws IOException
                {
                    com.squareup.okhttp.Request original = chain.request();

                    com.squareup.okhttp.Request request = original.newBuilder()                        
                        .method(original.method(), original.body())
                        .header(DESK_API_MIGRATION_HEADER, deskUrl)
                        .build();

                    return chain.proceed(request);
                }
            };

            // add the interceptors
            clientBuilder.applicationInterceptors(Arrays.asList(requestHeader, logging));
            
            // create the client and assign to private member variable
            _client = DeskClient.create(clientBuilder);
        }
        
        return _client;
    }

    public DeskClient getClient()
    {
        return _client;
    }

    public Map<String, Object> getClientSettings()
    {
        Map<String, Object> clientSettings = new HashMap<>(_clientSettings);

        // set Salesforce Service info
        clientSettings.put("server_url", _sfService.getServerUrl());
        clientSettings.put("session_id", _sfService.getSessionId());
        clientSettings.put("desk_migration_id", getMigrationId());

        /*/
        String msg = "Client Settings:";
        for (String key : clientSettings.keySet())
        {
            msg += "\n\t" + key + ": " + clientSettings.get(key);
        }
        Utils.log(msg);
        /**/

        return clientSettings;
    }

    public static Object authenticateTokens(Request req, Response res) throws Exception
    {
        // get the post parameters in a hash map
        Map<String, String> postParams = getPostParamsFromRequest(req, new String[] { "deskUrl", "consumerKey", "consumerSecret", "accessToken",
        "accessTokenSecret" });

        // response map
        Map<String, Object> response = new TreeMap<>();
        DeskService deskService;
        try
        {
        	// create a DeskService instance based on the data posted (e.g. tokens, url, session id, etc.)
        	deskService = new DeskService(postParams.get("deskUrl"), postParams.get("consumerKey"),
	            postParams.get("consumerSecret"), postParams.get("accessToken"), postParams.get("accessTokenSecret"),
	            (postParams.containsKey("desk_migration_id") ? postParams.get("desk_migration_id") : null),
	            postParams.get("server_url"), postParams.get("session_id"),
	            (postParams.containsKey("auditEnabled") ? Boolean.valueOf(postParams.get("auditEnabled")) : false));
	        
        
	        // build a DeskUtil (this has some non-static pieces so we can cache desk groups)
	        DeskUtil deskUtil = new DeskUtil(deskService);
	
	        // get the languages
	        Map<String, Object> languages = deskUtil.getDeskSiteLanguagesMap();
	        
	        // get the groups
	        Set<Integer> groupIds = deskUtil.getDeskGroupIds();
	        
	        // get the custom fields
	        List<CustomField> custom_fields = deskUtil.getDeskCustomFields();
	
	        // build response and return it
	        response.put("languages", languages);
	        response.put("groups", groupIds);
	        response.put("custom_fields", custom_fields);
        }
        catch(Exception unknownHost)
        {
        	throw new Exception("Unable to connect to Desk.com API");
        }
        return response;
    }
    
    public static Object migrateMetadata(Request req, Response res) throws Exception
    {
        // get the post parameters in a hash map
        Map<String, String> postParams = getPostParamsFromRequest(req, new String[] { "server_url", "session_id",
            "deskUrl", "consumerKey", "consumerSecret", "accessToken", "accessTokenSecret"});
        
        // create a DeskService instance based on the data posted (e.g. tokens, url, session id, etc.)
        DeskService deskService = new DeskService(postParams.get("deskUrl"), postParams.get("consumerKey"),
            postParams.get("consumerSecret"), postParams.get("accessToken"), postParams.get("accessTokenSecret"),
            (postParams.containsKey("desk_migration_id") ? postParams.get("desk_migration_id") : null),
            postParams.get("server_url"), postParams.get("session_id"),
            (postParams.containsKey("auditEnabled") ? Boolean.valueOf(postParams.get("auditEnabled")) : false));
        
        // build a DeskUtil (this has some non-static pieces so we can cache desk groups)
        DeskUtil deskUtil = new DeskUtil(deskService);
        
        // response map
        DeployResponse response = new DeployResponse();
        
        // check if custom fields were passed
        if (postParams.containsKey("custom_fields"))
        {
            // create the custom fields that were passed
            response.addDeployResponse(deskUtil.createCustomFields(postParams.get("custom_fields")));
            response.addDeployResponse(deskUtil.createFieldPermissions(postParams.get("custom_fields")));
        }
        
        // check if groups were passed
        if (postParams.containsKey("groups"))
        {
            // create the groups that were passed
            response.addDeployResponse(deskUtil.createQueues(postParams.get("groups")));            
        }
        
        return response;
    }
    
    public static Object migrateData(Request req, Response res) throws Exception
    {
        // get the post parameters in a hash map
        Map<String, String> postParams = getPostParamsFromRequest(req, new String[] { "server_url", "session_id",
                "deskUrl", "consumerKey", "consumerSecret", "accessToken", "accessTokenSecret"});

        // create a DeskService instance based on the data posted (e.g. tokens, url, session id, etc.)
        DeskService deskService = new DeskService(postParams.get("deskUrl"), postParams.get("consumerKey"),
                postParams.get("consumerSecret"), postParams.get("accessToken"), postParams.get("accessTokenSecret"),
                (postParams.containsKey("desk_migration_id") ? postParams.get("desk_migration_id") : null),
                postParams.get("server_url"), postParams.get("session_id"),
                (postParams.containsKey("auditEnabled") ? Boolean.valueOf(postParams.get("auditEnabled")) : false));

        // build a DeskUtil (this has some non-static pieces so we can cache desk groups)
        DeskUtil deskUtil = new DeskUtil(deskService);

        deskUtil.updateMigrationStatus(DeskMigrationFields.StatusQueued, "", null);

        // publish the job to RabbitMQ
        RabbitUtil.publishToQueue(QUEUE_DESK_DATA_MIGRATION, EXCHANGE_TRACTOR, JsonUtil.toJson(postParams).getBytes());

        return "SUBMITTED";
    }
    
    public static Object migrateAttachments(Request req, Response res) throws Exception
    {
        // get the post parameters in a hash map
        Map<String, String> postParams = getPostParamsFromRequest(req, new String[] { "server_url", "session_id",
                "deskUrl", "consumerKey", "consumerSecret", "accessToken", "accessTokenSecret"});

        // create a DeskService instance based on the data posted (e.g. tokens, url, session id, etc.)
        DeskService deskService = new DeskService(postParams.get("deskUrl"), postParams.get("consumerKey"),
                postParams.get("consumerSecret"), postParams.get("accessToken"), postParams.get("accessTokenSecret"),
                (postParams.containsKey("desk_migration_id") ? postParams.get("desk_migration_id") : null),
                postParams.get("server_url"), postParams.get("session_id"),
                (postParams.containsKey("auditEnabled") ? Boolean.valueOf(postParams.get("auditEnabled")) : false));

        // build a DeskUtil (this has some non-static pieces so we can cache desk groups)
        DeskUtil deskUtil = new DeskUtil(deskService);

        deskUtil.updateMigrationStatus(DeskMigrationFields.StatusQueued, "", null);

        // publish the job to RabbitMQ
        RabbitUtil.publishToQueue(QUEUE_DESK_ATTACHMENT, EXCHANGE_TRACTOR, JsonUtil.toJson(postParams).getBytes());

        return "SUBMITTED";
    }

    public static Object retrieveMetadata(Request req, Response res) throws Exception
    {
        // get the post parameters in a hash map
        Map<String, String> postParams = getPostParamsFromRequest(req, new String[] { "deskUrl", "consumerKey", "consumerSecret", "accessToken",
                "accessTokenSecret" });

        // response map
        Map<String, Object> response = new TreeMap<>();
        DeskService deskService;
        try
        {
            // create a DeskService instance based on the data posted (e.g. tokens, url, session id, etc.)
            deskService = new DeskService(postParams.get("deskUrl"), postParams.get("consumerKey"),
                    postParams.get("consumerSecret"), postParams.get("accessToken"), postParams.get("accessTokenSecret"),
                    (postParams.containsKey("desk_migration_id") ? postParams.get("desk_migration_id") : null),
                    postParams.get("server_url"), postParams.get("session_id"),
                    (postParams.containsKey("auditEnabled") ? Boolean.valueOf(postParams.get("auditEnabled")) : false));


            // build a DeskUtil (this has some non-static pieces so we can cache desk groups)
            DeskUtil deskUtil = new DeskUtil(deskService);

            // get the groups
            List<Group> groups = deskUtil.getDeskGroups();

            // get the users
            List<User> users = deskUtil.getDeskUsers();

            // get the custom fields
            List<CustomField> custom_fields = deskUtil.getDeskCustomFields();

            // build response and return it
            response.put("groups", groups);
            response.put("users", users);
            response.put("custom_fields", custom_fields);
        }
        catch(Exception unknownHost)
        {
            throw new Exception("Unable to connect to Desk.com API");
        }
        return response;
    }
}
