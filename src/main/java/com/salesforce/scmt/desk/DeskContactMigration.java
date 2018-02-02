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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskCustomerToSalesforceJsonMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.internal.bind.util.ISO8601Utils;
import java.text.ParsePosition;
import java.text.ParseException;

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.service.CustomerService;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.rabbitmq.RabbitConfiguration;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.JsonUtil;
import com.salesforce.scmt.utils.RabbitUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.sforce.async.OperationEnum;

import retrofit.Response;

public class DeskContactMigration<D extends Serializable> extends DeskBase<D>
{

    public DeskContactMigration(DeskUtil du, Map<String, String> config) {
        super(du, config);
	}

	private static final int DESK_PAGE_SIZE_CUSTOMER = 100;

    @Override
    protected long getId(D d)
    {
        Customer c = (Customer) d;
        return c.getId();
    }

    @Override
    protected long getUpdatedAt(D d)
    {
        try {
            Customer c = (Customer) d;
            return (long) (ISO8601Utils.parse(c.getUpdatedAt(), new ParsePosition(0)).getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings,
        DeskBaseResponse<ApiResponse<D>> dResp) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected DeskBaseResponse<ApiResponse<D>> callDesk(DeskUtil du)
    {
        // get a service
        CustomerService service = du.getDeskClient().customers();
        Response<ApiResponse<Customer>> resp = null;
        try
        {
            if (!delta)
            {
                // false == bigCompanies TODO
                resp = service.getCustomers(lastRecordId, DESK_PAGE_SIZE_CUSTOMER, page, "id", SortDirection.ASC,
                        (false ? Fields.include("id", "_links") : null)).execute();
            }
            else
            {
                resp = service.searchCustomersByUpdatedAt(updatedAt, DESK_PAGE_SIZE_CUSTOMER, page, "updated_at",
                    SortDirection.ASC, (false ? Fields.include("updated_at", "_links") : null)).execute();
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
    protected String createJob(DeskUtil du) throws Exception
    {
        return du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_CONTACT, CaseFields.DeskId,
            OperationEnum.upsert);
    }

    @Override
    protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception
    {
    	// set the job to migrate cases and set the start_id
        config.put("migrateCustomers", "true");
        config.put("start_id", String.valueOf(getId(recList.get(recList.size() - 1)) + 1));

        RabbitUtil.publishToQueue(RabbitConfiguration.QUEUE_DESK_DATA_MIGRATION, RabbitConfiguration.EXCHANGE_TRACTOR,
            JsonUtil.toJson(config).getBytes());
        return config;
    }

    @Override
    protected void objectSpecificBulkComplete(DeskUtil du) throws Exception
    {
    	du.updateMigrationStatus(DeskMigrationFields.StatusComplete, "Contact", dr);
    }

    @Override
    protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception
    {
        // Contact migration has no specific bulk upload behavior
    }

    @Override
    protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception
    {    	
        ArrayList<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
        try
        {
        	a.add(deskCustomerToSalesforceJsonMap(du, (Customer) d, new DeployResponse(), config));
        }
        catch(Exception e)
		{
			dr.incrementErrorCount(1);
			dr.addError(e.toString());
		}
        return a;
    }

}
