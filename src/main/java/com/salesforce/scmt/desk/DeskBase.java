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

package com.salesforce.scmt.desk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.desk.java.apiclient.model.ApiResponse;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.salesforce.scmt.utils.Utils;

public abstract class DeskBase<D extends Serializable>
{
    protected List<D> recList = new ArrayList<>();
    private DeskBaseResponse<ApiResponse<D>> dResp = new DeskBaseResponse<>();

    // deploy response
    protected DeployResponse dr = new DeployResponse();

    // declare iteration control variables
    private boolean bRetry = false;
    private int retryCount = 0;
    protected int requestCount = 0;
    protected boolean delta;
    protected String userEmailAddress;
    protected int page = 1;
    protected long lastRecordId = 1;

    protected long minTime;
    protected int now;
    protected Long updatedAt;

    protected String jobId = null;

    // flag which indicates if the job is being re-queued (to better handle the daily Heroku dyno restarts)
    private boolean bRequeued = false;
    
    protected DeskUtil du;
    protected Map<String, String> config;
    
    public DeskBase(DeskUtil du, Map<String, String> config)
    {
    	this.du = du;
    	this.config = config;
    }

    /*
     * Entry Point from DeskWorker, Starts migration.
     */
    public void migrate() throws Exception
    {
        Utils.log("Entered DeskBase::migrate()");

        // Set the custom label to 1, indicating bypass process builders. For Desk Trial Org.
        try {
            du.getSalesforceService().updateCustomLabel("BypassProcessBuilder", "1");
        } catch (Exception e) {
            Utils.logException(e);
        }

        // initialize a flag which indicates if this is a delta migration
        delta = (config.get("updated_at") != null && config.get("updated_at") != "null");
        config.put("delta", String.valueOf(delta));

        // declare last record id
        lastRecordId = (config.get("start_id") == null ? 1
            : (config.get("start_id") == "null" ? 1 : Long.valueOf(config.get("start_id"))));

        // declare the updatedAt time
        updatedAt = (config.get("updated_at") == null ? 1
            : (config.get("updated_at") == "null" ? 1 : Long.valueOf(config.get("updated_at"))));

        // get the client settings
        Map<String, Object> clientSettings = du.getDeskService().getClientSettings();

        // create initial bulk job, each object has implementation
        jobId = createJob(du);

        du.updateMigrationStatus(DeskMigrationFields.StatusRunning, "", null, jobId);

        // loop through retrieving records
        do
        {
            try
            {
                // reset the retry flag & increment request counter
                bRetry = false;
                requestCount++;


                if (!delta)
                {
                    dResp = callDesk(du);
                }
                else
                {
                    dResp = callDesk(du);
                }

                // check for success
                if (dResp.getIsSuccess())
                {                	
                    // log the Desk.com rate limiting headers
                    DeskUtil.logDeskRateHeaders(dResp.getHeaders());

                    // add the list of records to the return list
                    recList.addAll(((ApiResponse<D>) dResp.body).getEntriesAsList());

                    // check if we are on the last page
                    if (page >= DeskUtil.DESK_MAX_PAGES)
                    {
                        if (!delta)
                        {
                            // save the last record id so I can get the next set of pages increment by 1 as the
                            // 'since_at' filter is using a >= operator NOTE: Desk.com API documentation says the id
                            // could change to alphanumeric at some point, at which we will no longer be able to
                            // increment the id in this manner.
                            lastRecordId = getId(recList.get(recList.size() - 1)) + 1;
                        }
                        else
                        {
                            // update the time filter
                            updatedAt = getUpdatedAt(recList.get(recList.size() - 1));//
                        }

                        // reset the page counter
                        page = 0;
                    }

                    // implemented in object, can be extended for different processes
                    clientSettings = objectSpecificProcessing(clientSettings, dResp);

                    // general processing logic to 10k, object specific implementation is called
                    objectBulkUploadProcessing(du, config);

                    // increment the page counter
                    page++;  
                    userEmailAddress = (config.get("email_address") != null ? config.get("email_address") : "null");
                    if(!userEmailAddress.equals("") && !userEmailAddress.equals("null")){
//                        Utils.sendSuccessEmail(userEmailAddress);
                    }
                }
                else
                {
                    // check for 'too many requests' response                	
                    if (dResp.errorCode == 429)
                    {                    	
                        // get the reset seconds and sleep for that many seconds
                    	System.out.println("reset "+dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET));
                        Thread.sleep(Integer.parseInt(dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET)) * 1000);

                        // re-queue or retry
                        bRetry = true;
                    }
                    // java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
                    else if (dResp.code() == 500)
                    {
                        // when we run imports through the API with threaded requests we'll occasionally get a 500
                        // response and have to retry the request (which succeeds on the retry).
                        bRetry = true;
                    }
                    else if (dResp.code() == 504)
                    {
                    	// guard against 504 bad gateway
                    	Thread.sleep(Integer.parseInt(dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET)) * 1000);
                        bRetry = true;
                    }
                    else
                    {
                        Utils.log(dResp.getHeaders().toString());
                        // throw new Exception(String.format("Error (%d): %s\n%s", dResp.code(), dResp.message(),
                        // dResp.errorBody().toString()));
                        du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "", dr);
                        throw new Exception(String.format("Error %s", dResp.getMessage()));
                    }
                }
            }
            catch (Exception e)
            {
                // retry if we hit a socket timeout exception
                retryCount++;
                Utils.log("[EXCEPTION] Retry Attempt: " + retryCount);
                if (retryCount > 5)
                {
                    dr.setResumePoint(lastRecordId);
                    //du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "Cases", dr);
                    
//                    Utils.sendEmail();
                    // we retried 5 times, let exception go
                    du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "", dr);
                    throw e;
                }
                else
                {
                    bRetry = true;
                }
            }
        }
        // continue to loop while the request is successful and there are subsequent pages of results
        while (!bRequeued && (bRetry || (dResp.getIsSuccess() && ((ApiResponse<D>) dResp.body).hasNextPage()
            && SalesforceConstants.RETRIEVE_ALL)));

        // general processing for remaining objects over 10k or under10k, object specific is invoked.
        objectBulkUploadComplete(du, config);
    }

    private DeployResponse transformObject(String jobId, List<D> deskObjects, DeskUtil du)
    {
        DeployResponse dr = new DeployResponse();

        try
        {
            Utils.log("Bulk Upload");
            List<Map<String, Object>> sfRecs = new ArrayList<>();
            int counter = 0;

            for (D d : deskObjects)
            {
                // skip object
                if (skipObject(d)) continue;

                // convert the desk case to the Map for conversion to JSON
                // sfRecs.add(d);
            	List<Map<String, Object>> obj = deskObjectToSalesforceObject(du, d);
                sfRecs.addAll(obj);
                
                // increment the counter 
                counter = counter + obj.size();

                // submit a bulk job every 10k records
                if ((counter % SalesforceConstants.BULK_MAX_SIZE) == 0)
                {                	
                    du.getSalesforceService().addBatchToJob(jobId, sfRecs);
                    
                    //update dr success count
                    dr.incrementSuccessCount(sfRecs.size());
                    // clear the lists
                    sfRecs.clear();
                    //reset counter
                    counter = 0;
                }
            }

            // check if there are records that still need to be bulk upserted
            if (!sfRecs.isEmpty())
            {
                du.getSalesforceService().addBatchToJob(jobId, sfRecs);
                
                // update dr success count
                dr.incrementSuccessCount(sfRecs.size());                
            }
        }
        catch (Exception e)
        {
        	
            Utils.logException(e);
        }

        return dr;

    }

    private void objectBulkUploadProcessing(DeskUtil du, Map<String, String> config) throws Exception
    {
    	
        // every 10k records, pass to createCases() to bulk upsert them
        if (recList.size() >= SalesforceConstants.BULK_MAX_SIZE && !SalesforceConstants.READ_ONLY)
        {
            // check for valid job, job closes after 5k batches or 24 hours,
            // whichever comes first
            if (du.getSalesforceService().createNewJob(this.jobId))
            {

                // object specificBulk processing
                config = objectSpecificBulkProcessing(config);
                
                //close current job
                du.getSalesforceService().closeBulkJob(this.jobId, du.getDeskService().getMigrationId());

                // flip the flag indicating we have re-queued this message, and
                // we can exit this run...
                bRequeued = true;
                                
            }

            // create the cases, transformObject calls object specific method
            dr.addDeployResponse(transformObject(this.jobId, recList, du));

            // clear the records that were bulk inserted
            recList.subList(0, SalesforceConstants.BULK_MAX_SIZE).clear();
        }

    }

    private void objectBulkUploadComplete(DeskUtil du, Map<String, String> config) throws Exception
    {
        // process any records over the 10k chunk, or all if total is less than 10k.

        
        if (!recList.isEmpty() && !SalesforceConstants.READ_ONLY)
        {
            System.out.println("JobID" + this.jobId);
            // check for valid job, job closes after 5k batches or 24 hours,
            // whichever comes first
            if (du.getSalesforceService().createNewJob(this.jobId))
            {

                // close the current job
                du.getSalesforceService().closeBulkJob(this.jobId, du.getDeskService().getMigrationId());

                // object specific cleanup create new job
                objectSpecificBulkCleanup(du);
            }

            System.out.println("Upserting : " + recList.size());
            
            dr.addDeployResponse(transformObject(this.jobId, recList, du));
            recList.clear();

            // close the bulk job
            du.getSalesforceService().closeBulkJob(jobId, du.getDeskService().getMigrationId());
            
         // object specific completion code, eg. cases sends attachment ids
            dr.setResumePoint(lastRecordId);
//            du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "Cases", dr);
            
//            Utils.sendEmail();
            objectSpecificBulkComplete(du);
            
        }
    }

    protected boolean skipObject(D d) { return false; }

    protected abstract long getId(D d);

    protected abstract long getUpdatedAt(D d);

    protected abstract DeskBaseResponse<ApiResponse<D>> callDesk(DeskUtil du);

    protected abstract String createJob(DeskUtil du) throws Exception;

    protected abstract Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings,
        DeskBaseResponse<ApiResponse<D>> dResp) throws Exception;

    protected abstract Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception;

    protected abstract void objectSpecificBulkComplete(DeskUtil du) throws Exception;

    protected abstract void objectSpecificBulkCleanup(DeskUtil du) throws Exception;

    protected abstract List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception;
}
