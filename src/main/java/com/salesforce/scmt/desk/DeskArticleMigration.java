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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskArticleToSalesforceJsonMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Article;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.service.ArticleService;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.utils.DeskUtil;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.sforce.async.OperationEnum;

import retrofit.Response;

public class DeskArticleMigration<D extends Serializable> extends DeskBase<D> {
	private static final int DESK_PAGE_SIZE_ARTICLE = 500;
	
	private int articleCounter = 0;
	
	public DeskArticleMigration(DeskUtil du, Map<String, String> config) {
		super(du, config);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int getId(D d) {
		Article a = (Article) d;
		return a.getId();
	}

	public void migrate() throws Exception
	{
		Utils.log("Entered DeskArticleMigration::migrate()");

		// initialize a flag which indicates if this is a delta migration
		delta = (config.get("updated_at") != null && config.get("updated_at") != "null");
		config.put("delta", String.valueOf(delta));

		// get a list of all languages

		Set languages = du.getDeskSiteLanguagesMap().keySet()

		getKnowledgeLanguageSettings

		// declare last record id
		lastRecordId = (config.get("start_id") == null ? 1
				: (config.get("start_id") == "null" ? 1 : Integer.valueOf(config.get("start_id"))));

		// declare the updatedAt time
		updatedAt = (config.get("updated_at") == null ? 1
				: (config.get("updated_at") == "null" ? 1 : Integer.valueOf(config.get("updated_at"))));

		// get the client settings
		Map<String, Object> clientSettings = du.getDeskService().getClientSettings();

		// create initial bulk job, each object has implementation
		jobId = createJob(du);

		du.updateMigrationStatus(DeskMigrationFields.StatusRunning, "", null, jobId);

		//loop through each language
		for { String language : languages } {
		// loop through retrieving records
		do {
			try {
				// reset the retry flag & increment request counter
				bRetry = false;
				requestCount++;


				if (!delta) {
					dResp = callDesk(du, language);
				} else {
					dResp = callDesk(du, language);
				}

				// check for success
				if (dResp.getIsSuccess()) {
					// log the Desk.com rate limiting headers
					DeskUtil.logDeskRateHeaders(dResp.getHeaders());

					// add the list of records to the return list
					recList.addAll(((ApiResponse<D>) dResp.body).getEntriesAsList());

					// check if we are on the last page
					if (page >= DESK_MAX_PAGES) {
						if (!delta) {
							// save the last record id so I can get the next set of pages increment by 1 as the
							// 'since_at' filter is using a >= operator NOTE: Desk.com API documentation says the id
							// could change to alphanumeric at some point, at which we will no longer be able to
							// increment the id in this manner.
							lastRecordId = getId(recList.get(recList.size() - 1)) + 1;
						} else {
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
					if (!userEmailAddress.equals("") && !userEmailAddress.equals("null")) {
//                        Utils.sendSuccessEmail(userEmailAddress);
					}
				} else {
					// check for 'too many requests' response
					if (dResp.errorCode == 429) {
						// get the reset seconds and sleep for that many seconds
						System.out.println("reset " + dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET));
						Thread.sleep(Integer.parseInt(dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET)) * 1000);

						// re-queue or retry
						bRetry = true;
					}
					// java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
					else if (dResp.code() == 500) {
						// when we run imports through the API with threaded requests we'll occasionally get a 500
						// response and have to retry the request (which succeeds on the retry).
						bRetry = true;
					} else if (dResp.code() == 504) {
						// guard against 504 bad gateway
						Thread.sleep(Integer.parseInt(dResp.getHeaders().get(DeskUtil.DESK_HEADER_LIMIT_RESET)) * 1000);
						bRetry = true;
					} else {
						Utils.log(dResp.getHeaders().toString());
						// throw new Exception(String.format("Error (%d): %s\n%s", dResp.code(), dResp.message(),
						// dResp.errorBody().toString()));
						du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "", dr);
						throw new Exception(String.format("Error %s", dResp.getMessage()));
					}
				}
			} catch (Exception e) {
				// retry if we hit a socket timeout exception
				retryCount++;
				Utils.log("[EXCEPTION] Retry Attempt: " + retryCount);
				if (retryCount > 5) {
					dr.setResumePoint(lastRecordId);
					//du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "Cases", dr);

//                    Utils.sendEmail();
					// we retried 5 times, let exception go
					du.updateMigrationStatus(DeskMigrationFields.StatusFailed, "", dr);
					throw e;
				} else {
					bRetry = true;
				}
			}
		}
		// continue to loop while the request is successful and there are subsequent pages of results
		while (!bRequeued && (bRetry || (dResp.getIsSuccess() && ((ApiResponse<D>) dResp.body).hasNextPage()
				&& SalesforceConstants.RETRIEVE_ALL)));
	}
		// general processing for remaining objects over 10k or under10k, object specific is invoked.
		objectBulkUploadComplete(du, config);
	}

	@Override
	protected int getUpdatedAt(D d) {
		Article a = (Article) d;
		return Integer.valueOf(a.getUpdatedAt().toString());
	}

	@Override
	protected DeskBaseResponse<ApiResponse<D>> callDesk(DeskUtil du, String language) {
		// get a service
        ArticleService service = du.getDeskClient().articles();
        Response<ApiResponse<Article>> resp = null;
        try
        {
            if (!delta)
            {
                // false == bigCompanies TODO
                resp = service.getArticles(language, page, DESK_PAGE_SIZE_ARTICLE, true).execute();
            }
            else
            {
                resp = service.getArticles(language, page, DESK_PAGE_SIZE_ARTICLE, true).execute();
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
	protected String createJob(DeskUtil du) throws Exception {
		return du.getSalesforceService().createBulkJob(SalesforceConstants.OBJ_ARTICLE, CaseFields.Id,
	            OperationEnum.upsert);
	}

	@Override
	protected Map<String, Object> objectSpecificProcessing(Map<String, Object> clientSettings,
			DeskBaseResponse<ApiResponse<D>> dResp) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Map<String, String> objectSpecificBulkProcessing(Map<String, String> config) throws Exception {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	protected void objectSpecificBulkComplete(DeskUtil du) throws Exception {
		du.updateMigrationStatus(DeskMigrationFields.StatusComplete, "Articles", dr);
		
	}

	@Override
	protected void objectSpecificBulkCleanup(DeskUtil du) throws Exception {
		
		
	}

	@Override
	protected List<Map<String, Object>> deskObjectToSalesforceObject(DeskUtil du, D d) throws Exception {
		ArrayList<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
        try
        {
        	a.add(deskArticleToSalesforceJsonMap((Article) d, new DeployResponse(), articleCounter));
        }
        catch(Exception e)
		{
			dr.incrementErrorCount(1);
			dr.addError(e.toString());
		}
        return a;
		
	}

}
