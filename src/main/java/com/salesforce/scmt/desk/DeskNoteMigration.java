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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskNoteToSalesforceJsonMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.Note;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.service.CustomerService;
import com.desk.java.apiclient.service.NoteService;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.Utils;
import com.sforce.async.OperationEnum;

import retrofit.Response;

public class DeskNoteMigration<D extends Serializable> extends DeskBase<D>
{
    public DeskNoteMigration(DeskUtil du, Map<String, String> config) {
		super(du, config);		
	}

	private static final int DESK_PAGE_SIZE_NOTE = 100;
	private static final int DESK_NOTES_MAX_PAGE = 30000;
	private boolean notesDelta = false;
	private int notesPage;

    @Override
    protected int getId(D d)
    {
        Note n = (Note) d;
        return n.getId();
    }

    @Override
    protected int getUpdatedAt(D d)
    {
        Note n = (Note) d;
        return Integer.valueOf(n.getUpdatedAt());
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
    	/*
    	 * ***Special Note*** - Notes migration does not use the page iVar from the base class. The notes api endpoint
    	 * does not support a since_id filter and does the max page var is 30k at 100 records per. The logic the rest
    	 * of the app uses will try and reset at 500 which will fail in an endless loop.   
    	
    	*/
        // get a service
        NoteService service = du.getDeskClient().notes();
        Response<ApiResponse<Note>> resp = null;

        // in a note migration the "start_id" param will be a page #. Desk API doesn't support starting at an ID.       
        if(notesDelta == false)
        {
        	// the first time through we need to set the page to start at the lastrecordId. 
        	notesPage = lastRecordId;
        	notesDelta = true;
        }
        
        try
        {
        	if(notesPage >= DESK_NOTES_MAX_PAGE)
        {
        		DeskBaseResponse<ApiResponse<D>> d = new DeskBaseResponse<>();
                d.errorCode = 200;
                d.setIsSuccess(false);
                d.setMessage("Max. Page accessible through DeskAPI is 30k. ");
                Utils.log("Max note page reached! Max page is 30k");
                return d;
        	}
            resp = service.getNotes(notesPage, DESK_PAGE_SIZE_NOTE).execute();
            notesPage ++;
            page = 0;
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
        return du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_CASE_COMMENT, null,
            OperationEnum.insert);
    }

    @Override
    protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception
    {
        // Note migration has no specific bulk upload behavior
        return config;
    }

    @Override
    protected void objectSpecificBulkComplete(DeskUtil du) throws Exception
    {
    	du.updateMigrationStatus("Complete", "Notes", dr);
    }

    @Override
    protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception
    {
        // Note migration has no specific bulk upload behavior

    }

    @Override
    protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception
    {
    	List<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
    	try
    	{
    		a = deskNoteToSalesforceJsonMap(du, (Note) d, new DeployResponse());  
    	}
    	catch(Exception e)
		{
			dr.incrementErrorCount(1);
			dr.addError(e.toString());
		}
        return a;

    }

}
