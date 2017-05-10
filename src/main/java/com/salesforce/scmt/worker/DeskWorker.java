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

package com.salesforce.scmt.worker;

import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.EXCHANGE_FORMULA1;
import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.QUEUE_DESK_ATTACHMENT_BACKGROUND;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.desk.java.apiclient.model.Article;
import com.salesforce.scmt.desk.DeskAccountMigration;
import com.salesforce.scmt.desk.DeskArticleMigration;
import com.salesforce.scmt.desk.DeskBase;
import com.salesforce.scmt.desk.DeskCaseMigration;
import com.salesforce.scmt.desk.DeskContactMigration;
import com.salesforce.scmt.desk.DeskNoteMigration;
import com.salesforce.scmt.desk.DeskUserMigration;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.service.DeskService;
//import com.salesforce.scmt.service.DeskService;
import com.salesforce.scmt.service.SalesforceService;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.JsonUtil;
import com.salesforce.scmt.utils.RabbitUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMessageFields;
import com.salesforce.scmt.utils.SalesforceConstants.UserFields;
import com.salesforce.scmt.utils.Utils;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.soap.partner.sobject.SObject;

public final class DeskWorker
{
    /**
     * default constructor
     */
    private DeskWorker() {}
    
	public static <S, D> void migrateData(String json)
	{
		Utils.log("[DESK] DeskWorker::migrateData() entered.");
		// Utils.log("JSON: " + json);

		// deserialize the data
		@SuppressWarnings("unchecked")
		Map<String, String> config = (Map<String, String>) JsonUtil.fromJson(json, Map.class);

        try
        {
        	System.out.println("config" + config.get("server_url"));
        	System.out.println("config" + config.get("deskUrl"));
            Map<String, Object> objMap = new HashMap<>(config);
            DeskUtil deskUtil = new DeskUtil(new DeskService((String)config.get("deskUrl"),
            		(String)config.get("consumerKey"), (String)config.get("consumerSecret"),
            		(String)config.get("accessToken"), (String)config.get("accessTokenSecret"),
                (config.containsKey("desk_migration_id") ? (String)config.get("desk_migration_id") : null),
                (String)config.get("server_url"), (String)config.get("session_id"),
                (config.containsKey("auditEnabled") ? Boolean.valueOf(config.get("auditEnabled")): false)));

			Integer startId = (config.containsKey("start_id") && !((String) config.get("start_id")).isEmpty() &&
                // avoid: java.lang.NumberFormatException: For input string: "null"
                !"null".equalsIgnoreCase((String) config.get("start_id")) &&
                !"undefined".equalsIgnoreCase((String) config.get("start_id")) ?
                Integer.parseInt(config.get("start_id")) : null);

			//update config
			config.put("start_id", String.valueOf(startId));
            
			Integer updatedAt = (config.containsKey("updated_at") && !((String) config.get("updated_at")).isEmpty() &&
                // avoid: java.lang.NumberFormatException: For input string: "null"
                !"null".equalsIgnoreCase((String) config.get("updated_at")) &&
                !"undefined".equalsIgnoreCase((String) config.get("updated_at")) ?
                Integer.parseInt(config.get("updated_at")) : null);
            
			//update config
			config.put("updated_at", String.valueOf(updatedAt));
            
			DeployResponse dr = new DeployResponse();

            // create a flag which indicates if we are migrating a large number of companies and need to use the 
            // alternate method
            boolean bigCompanies = (config.containsKey("bigCompanies") && Boolean.valueOf(config.get("bigCompanies")));            

            if (Boolean.valueOf(config.get("migrateUsers")))
            {
            	DeskUserMigration<com.desk.java.apiclient.model.User> userMigration = new DeskUserMigration<>(deskUtil, config);
            	userMigration.migrate();               
            }
            
            if (Boolean.valueOf(config.get("migrateGroupMembers")))
            {
            	//get set of desk groupIds
            	Set<Integer> groupIds = deskUtil.getDeskGroupIdAndName().keySet();            	
            	
            	 //get and create sfdc queue members from desk group members           	
            	dr.addDeployResponse(deskUtil.getDeskGroupMembers(groupIds));
            }
            
            if (Boolean.valueOf(config.get("migrateCompanies")) && !bigCompanies)
            {
            	// migrate companies to accounts
            	DeskAccountMigration<com.desk.java.apiclient.model.Company> accountMigration = new DeskAccountMigration<>(deskUtil, config);
            	accountMigration.migrate();
            }
			
            if (Boolean.valueOf(config.get("migrateCustomers")) || bigCompanies)
            {
            	// migrate customers to contacts
            	DeskContactMigration<com.desk.java.apiclient.model.Customer> contactMigration = new DeskContactMigration<>(deskUtil, config);
            	contactMigration.migrate();
            }

            if (Boolean.valueOf(config.get("migrateCases")))
            {
            	// migrate tickets to cases
            	DeskCaseMigration<com.desk.java.apiclient.model.Case> caseMigration = new DeskCaseMigration<>(deskUtil, config);
            	caseMigration.migrate();
            }
            
            if (Boolean.valueOf(config.get("migrateNotes")))
            {
            	// migrate notes to case comments
                DeskNoteMigration<com.desk.java.apiclient.model.Note> noteMigration = new DeskNoteMigration<>(deskUtil, config);
                config.put("startId", String.valueOf(startId));
            	noteMigration.migrate();
            }
            
            if (Boolean.valueOf(config.get("migrateInteractions")))
            {
            	// migrate interactions to case comments and email messages
                dr.addDeployResponse(deskUtil.migrateDeskInteractions(config, startId));
            }
            
            if (Boolean.valueOf(config.get("migrateArticles")))
            {
            	// migrate articles to articles
            	DeskArticleMigration<com.desk.java.apiclient.model.Article> articleMigration = new DeskArticleMigration<>(deskUtil, config);
                config.put("startId", String.valueOf(startId));
            	articleMigration.migrate();
            }

        }
        catch (Exception e)
        {
            Utils.logException(e);
        }
    }

    public static void migrateCaseFeedData(String json)
    {
        Utils.log("[DESK] DeskWorker::migrateCaseFeedData() entered.");
        // Utils.log("JSON: " + json);

        try
        {
            // deserialize the data
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) JsonUtil.fromJson(json, Map.class);

            DeskUtil deskUtil = new DeskUtil(new DeskService(config));
            
            // define the list of desk messages for saving the attachments so I can migrate them later
            List<SObject> deskMessages = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            // casting to Integer produced entries like '5.0', so explicitly parse to Integer
            List<Integer> caseIdsWithAttachments = Utils.listDoubleToListInteger(
                (List<Double>) config.get("case_ids_with_attachments"));

            // make sure the values are integers
            for (Integer caseId : caseIdsWithAttachments)
            {
                // add the attachment ids to the custom object for later processing
                SObject deskMessage = new SObject(SalesforceConstants.OBJ_DESK_MESSAGE);
                deskMessage.setField(DeskMessageFields.Name, String.format("%s%d",
                    DeskUtil.DESK_MESSAGE_ATTACHMENT_PREFIX, caseId));
                deskMessage.setField(DeskMessageFields.Status, DeskMessageFields.StatusNew);
                deskMessages.add(deskMessage);
            }
            
            // check if there are desk messages
            if (!deskMessages.isEmpty())
            {
                // upsert the records
                deskUtil.getSalesforceService().upsertData(DeskMessageFields.Name, deskMessages);
            }
        }
        catch (Exception e)
        {
            Utils.logException(e);
        }
    }

    public static void migrateBigCompanyData(String json)
    {
        Utils.log("[DESK] DeskWorker::migrateBigCompanyData() entered.");
        // Utils.log("JSON: " + json);

        try
        {
            // deserialize the data
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) JsonUtil.fromJson(json, Map.class);

            DeskUtil deskUtil = new DeskUtil(new DeskService(config));
            
            @SuppressWarnings("unchecked")
            // casting to Integer produced entries like '5.0', so explicitly parse to Integer
            // Utils.log("company_ids (double) size: [" + idDoubles.size() + "] Values: " + idDoubles);
            Set<Integer> ids = new HashSet<>(Utils.listDoubleToListInteger((List<Double>) config.get("company_ids")));

            if (ids.isEmpty())
            {
                Utils.log("[WARN] Empty list of Company Id's passed to migrateBigCompanyData()!");
            }
            else
            {
                // retrieve the desk companies and migrate them to Salesforce
//                deskUtil.getDeskCompanies(ids);
            }
        }
        catch (Exception e)
        {
            Utils.logException(e);
        }
    }
    
    public static void queryDeskMessageAttachments(String json)
    {
        Utils.log("[DESK] DeskWorker::queryDeskMessageAttachments() entered.");

        // deserialize the data
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) JsonUtil.fromJson(json, Map.class);
        DeskUtil deskUtil = new DeskUtil(new DeskService(config));
        
        try
        {
            // initialize the id
            String sfId = SalesforceConstants.MIN_ID; 

            // build the query
            String queryBase = String.format(
                "SELECT %s, %s FROM %s WHERE %s IN ('%s', '%s') AND %s > '__ID__' AND %s LIKE '%s%%' ORDER BY %s LIMIT %d",
                DeskMessageFields.Id, DeskMessageFields.Name,
                SalesforceConstants.OBJ_DESK_MESSAGE,
                DeskMessageFields.Status, DeskMessageFields.StatusNew, DeskMessageFields.StatusFailed,
                DeskMessageFields.Id,
                DeskMessageFields.Name, DeskUtil.DESK_MESSAGE_ATTACHMENT_PREFIX,
                DeskMessageFields.Id,
                //100);
                SalesforceConstants.SOQL_MAX_SIZE);
            
            // list of case id's with attachments
            List<Integer> caseIdsWithAttachments = new ArrayList<>();
            
            // attachments list
            List<SObject> deskMessages = null;
            
//            int page = 0;
            
            // flag which indicates if the SOQL query is returning results to process
            boolean hasResults = false;
            
            do
            {
                // update the query with the Id filter
                String query = queryBase.replaceAll("__ID__", sfId);

                // query for the results
                deskMessages = deskUtil.getSalesforceService().query(query, false);
                
                // check if the results are empty
                if (deskMessages == null || deskMessages.isEmpty())
                {
                    // check if we had results at least once
                    if (!hasResults)
                    {
                        Utils.log("[ERROR] Query returned no results! Please try again.");
                    }
                    
                    // flip the flag so we can exit gracefully
                    hasResults = false;
                }
                else
                {
                    // flip the flag indicating we received results
                    hasResults = true;
                    
                    // clear the attachment id holder
                    caseIdsWithAttachments.clear();
                    
                    // loop through the results and build the list of case id's with attachments
                    for (SObject deskMessage : deskMessages)
                    {
                        // get the name (can we assume it is never null?)
                        String name = deskMessage.getField(DeskMessageFields.Name).toString();
                        
                        // // get the case Id as string
                        String caseId = name.substring(name.lastIndexOf("-") + 1);
                        
                        // add the integer value of the case id to the list
                        caseIdsWithAttachments.add(Integer.valueOf(caseId));
                    }
                    
                    // add the case id's to the map with the config
                    config.put("desk_case_ids_with_attachments", caseIdsWithAttachments);
                    
                    // publish the job to RabbitMQ
                    RabbitUtil.publishToQueue(QUEUE_DESK_ATTACHMENT_BACKGROUND, EXCHANGE_FORMULA1,
                        JsonUtil.toJson(config).getBytes());
                    
                    // update the filter
                    sfId = deskMessages.get(deskMessages.size() - 1).getField(DeskMessageFields.Id).toString();
                    
//                    // increment page counter
//                    page++;
                }
//                // DEBUG
//                if (page >= 1)
//                {
//                    return;
//                }
            }
            while (hasResults);
        }
        catch (UnexpectedErrorFault e)
        {
            DeployResponse dr = new DeployResponse();
            dr.addError(String.format("[%s] %s", e.getExceptionCode().name(), e.getExceptionMessage()));
            deskUtil.updateMigrationStatus(e.getExceptionCode().name(), "Attachments", dr);
            Utils.logException(e);
        }
        catch (Exception e)
        {
            DeployResponse dr = new DeployResponse();
            dr.addError(e.getMessage());
            deskUtil.updateMigrationStatus("Failed", "Attachments", dr);
            Utils.logException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void migrateAttachments(String json)
    {
        Utils.log("[DESK] DeskWorker::migrateAttachments() entered.");

        // deserialize the data
        Map<String, Object> config = (Map<String, Object>) JsonUtil.fromJson(json, Map.class);

        try
        {
            DeskUtil deskUtil = new DeskUtil(new DeskService(config));
            
            deskUtil.migrateDeskAttachments(Utils.listDoubleToListInteger(
                (List<Double>)config.get("desk_case_ids_with_attachments")));
        }
        catch (Exception e)
        {
            Utils.logException(e);
        }
    }
}
