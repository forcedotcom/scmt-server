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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.desk.java.apiclient.model.ApiResponse;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.SalesforceConstants;

public class DeskInteractionMigration<D extends Serializable> extends DeskBase<D> {
	
	final Set<String> soTypes = new HashSet<String>(Arrays.asList(SalesforceConstants.OBJ_EMAIL_MESSAGE, SalesforceConstants.OBJ_CASE_COMMENT));
	
	public DeskInteractionMigration(DeskUtil du, Map<String, String> config) {
		super(du, config);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int getId(D d) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getUpdatedAt(D d) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings, DeskBaseResponse<ApiResponse<D>> dResp)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeskBaseResponse callDesk(DeskUtil du) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String createJob(DeskUtil du) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void objectSpecificBulkComplete(DeskUtil du) throws Exception {
		// TODO Auto-generated method stub
		du.updateMigrationStatus(DeskMigrationFields.StatusComplete, "Interaction", dr);
	}

	@Override
	protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
