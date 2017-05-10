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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskCaseToSalesforceJsonMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Case;
import com.desk.java.apiclient.model.CaseStatus;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.service.CaseService;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.rabbitmq.RabbitConfiguration;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.JsonUtil;
import com.salesforce.scmt.utils.RabbitUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.salesforce.scmt.utils.Utils;
import com.sforce.async.OperationEnum;

import retrofit.Response;

public class DeskCaseMigration<D extends Serializable> extends DeskBase<D>
{
    public DeskCaseMigration(DeskUtil du, Map<String, String> config) {
		super(du, config);
		// TODO Auto-generated constructor stub
	}

	private static final int DESK_PAGE_SIZE_CASE = 100; // API doc report this as 500, but the max size is really 100

    private List<Integer> attachmentIdList = new ArrayList<>();

    @Override
    protected DeskBaseResponse<ApiResponse<D>> callDesk(DeskUtil du)
    {
        // get a service
        CaseService service = du.getDeskClient().cases();
        Response<ApiResponse<Case>> resp = null;
        try
        {
            if (!delta)
            {
                resp = service
                    .searchCasesById(lastRecordId, DESK_PAGE_SIZE_CASE, page, "id", SortDirection.ASC, null, null)
                    .execute();
            }
            else
            {
                resp = service.searchCasesByUpdatedDate(updatedAt, DESK_PAGE_SIZE_CASE, page, "updated_at",
                    SortDirection.ASC, null, null).execute();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DeskBaseResponse<ApiResponse<D>> d = new DeskBaseResponse<>();
        d.errorCode = resp.code();
        d.setIsSuccess(resp.isSuccess());
        d.body = (ApiResponse<D>) resp.body();
        d.setHeaders(resp.headers());
        d.setMessage(resp.message());        
        return d;
    }

    @Override
    protected int getId(D d)
    {
        return ((Case) d).getId();
    }

    @Override
    protected int getUpdatedAt(D d)
    {
        return (int) (((Case) d).getUpdatedAt().getTime() / 1000);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings,
        DeskBaseResponse<ApiResponse<D>> dResp) throws Exception
    {
        // build list of case id's
        for (Case c : ((ApiResponse<Case>) dResp.body).getEntriesAsList())
        {
            // check if this case has attachments
            if (c.getActiveAttachmentsCount() > 0)
            {
                // add the case id so I can retrieve those on a separate process
                attachmentIdList.add(c.getId());
            }
        }

        Utils.log(String.format("Request Count: [%d], Case List Size: [%d], Attachment Id List Size: [%d]",
            requestCount, recList.size(), attachmentIdList.size()));

        // check if there are enough attachment id's to send to queue
        if (attachmentIdList.size() >= SalesforceConstants.API_MAX_SIZE)
        {
            // add the case id's with attachments to the feed settings
            clientSettings.put("case_ids_with_attachments",
                attachmentIdList.subList(0, SalesforceConstants.API_MAX_SIZE));

            // push the list of case id's to the rabbit mq to retrieve the feed
            // for these cases
            RabbitUtil.publishToQueue(RabbitConfiguration.QUEUE_DESK_FEED_MIGRATION,
                RabbitConfiguration.EXCHANGE_FORMULA1, JsonUtil.toJson(clientSettings).getBytes());

            // clear the items sent to the queue
            attachmentIdList.subList(0, SalesforceConstants.API_MAX_SIZE).clear();
        }
        return clientSettings;
    }

    @Override
    protected String createJob(DeskUtil du) throws Exception
    {
        return du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_CASE, CaseFields.DeskId,
            OperationEnum.upsert);
    }

    @Override
    protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception
    {
        // set the job to migrate cases and set the start_id
        config.put("migrateCases", "true");
        // config.put("updated_at", (delta ?
        // String.valueOf(recList.get(recList.size() -
        // 1).getUpdatedAt().getTime()) : ""));
        config.put("start_id", String.valueOf(getId(recList.get(recList.size() - 1)) + 1));

        RabbitUtil.publishToQueue(RabbitConfiguration.QUEUE_DESK_DATA_MIGRATION, RabbitConfiguration.EXCHANGE_TRACTOR,
            JsonUtil.toJson(config).getBytes());
        return config;
    }

    @Override
    protected void objectSpecificBulkComplete(DeskUtil du) throws Exception
    {
        System.out.println("AttachmentIdListSize: " + attachmentIdList.size());
        while (!attachmentIdList.isEmpty())
        {
            // find the upper list index
            int iMax = (attachmentIdList.size() > SalesforceConstants.API_MAX_SIZE ? SalesforceConstants.API_MAX_SIZE
                : attachmentIdList.size());

            // add the case id's with attachments to the feed settings
            Map<String, Object> clientSettings = du.getDeskService().getClientSettings();
            clientSettings.put("case_ids_with_attachments", attachmentIdList.subList(0, iMax));

            // push the list of case id's to the rabbit mq to retrieve the feed
            // for these cases
            RabbitUtil.publishToQueue(RabbitConfiguration.QUEUE_DESK_FEED_MIGRATION,
                RabbitConfiguration.EXCHANGE_FORMULA1, JsonUtil.toJson(clientSettings).getBytes());

            // clear the items sent to the queue
            attachmentIdList.subList(0, iMax).clear();
        }
        
        du.updateMigrationStatus(DeskMigrationFields.StatusComplete, "Cases", dr);
    }

    @Override
    protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception
    {
        // create new job
        this.jobId = du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_CASE, CaseFields.DeskId,
            OperationEnum.upsert);

        // get the upper boundary for the created/updated timestamp
        Case record = (Case) recList.get(recList.size() - 1);
        int lastTimestamp = (int) ((delta ? record.getUpdatedAt().getTime() : record.getCreatedAt().getTime()) / 1000);

        // close the bulk job
        du.getSalesforceService().closeBulkJob(jobId);

        // log the upper timestamp boundary
        dr.addError(String.format("Migrated all records created/updated before: [%d]", lastTimestamp));        
    }

    @Override
    protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception
    {
        ArrayList<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
        try
        {
        	a.add(deskCaseToSalesforceJsonMap(du, (Case) d, config));
        }
        catch(Exception e)
		{
			dr.incrementErrorCount(1);
			dr.addError(e.toString());
		}
        return a;
    }

    @Override
    protected boolean skipObject(D d) {
        return ((Case) d).getStatus() == CaseStatus.DELETED;
    }
}
