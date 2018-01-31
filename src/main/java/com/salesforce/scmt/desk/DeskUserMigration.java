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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskUserToSalesforceJsonMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.User;
import com.desk.java.apiclient.service.UserService;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.UserFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.sforce.async.OperationEnum;

import retrofit.Response;

public class DeskUserMigration<D extends Serializable> extends DeskBase<D> {
	
	private static final int DESK_PAGE_SIZE_USER = 1000;

	private String profileId;
	
	public DeskUserMigration(DeskUtil du, Map<String, String> config) {
		super(du, config);
		if(config.containsKey("ProfileId")){
			profileId = config.get("ProfileId");
		}
		
	}
	
	@Override
	protected long getId(D d) {
		User u = (User)d;
		return u.getId();
	}

	@Override
	protected long getUpdatedAt(D d) {
		//User service does not support updated at
		return 0;
	}

	@Override
	protected Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings, DeskBaseResponse<ApiResponse<D>> dResp)
			throws Exception {
		return clientSettings;
	}

	@Override
	protected DeskBaseResponse callDesk(DeskUtil du) {
		
		// get a service
		UserService service = du.getDeskClient().users();
		Response<ApiResponse<User>> resp = null;

		try {
			resp = service.getUsers(DESK_PAGE_SIZE_USER, page).execute();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeskBaseResponse<ApiResponse<User>> d = new DeskBaseResponse<ApiResponse<User>>();
		d.errorCode = resp.code();
		d.setIsSuccess(resp.isSuccess());
		d.body = resp.body();
		d.setHeaders(resp.headers());
		d.setMessage(resp.message());

		return d;
		
	}

	@Override
	protected String createJob(DeskUtil du) throws Exception {
		return du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_USER, UserFields.DeskId,
				OperationEnum.upsert);
	}

	@Override
	protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	protected void objectSpecificBulkComplete(DeskUtil du) throws Exception {
		System.out.println("success size" + dr.getSuccessCount());
		du.updateMigrationStatus(DeskMigrationFields.StatusComplete, "User", dr);
		
	}

	@Override
	protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception {
		ArrayList<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
		try
		{		
			Map<String, Object> m = deskUserToSalesforceJsonMap((User) d);
			if(profileId != null)m.put(UserFields.ProfileId, profileId);
			a.add(m);
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
		// skip if user is deleted
		return ((User) d).getDeleted();
	}

}
