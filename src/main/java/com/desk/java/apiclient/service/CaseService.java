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

package com.desk.java.apiclient.service;

import com.desk.java.apiclient.DeskClientBuilder;
import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Attachment;
import com.desk.java.apiclient.model.Case;
import com.desk.java.apiclient.model.CaseLock;
import com.desk.java.apiclient.model.Embed;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.MacroResponse;
import com.desk.java.apiclient.model.Message;
import com.desk.java.apiclient.model.SortDirection;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CaseService {

    // URIs
    String FILTERS_URI = "filters";
    String CASES_URI = "cases";
    String REPLIES_URI = "replies";
    String DRAFT_URI = REPLIES_URI + "/draft";
    String NOTE_URI = "notes";
    String MACROS_URI = "macros";
    String ATTACHMENTS_URI = "attachments";

    // Fields
    String FIELD_ID = "id";
    String FIELD_SUBJECT = "subject";
    String FIELD_BLURB = "blurb";
    String FIELD_STATUS = "status";
    String FIELD_TYPE = "type";
    String FIELD_LABELS = "labels";
    String FIELD_LABEL_IDS = "label_ids";
    String FIELD_UPDATED_AT = "updated_at";
    String FIELD_PRIORITY = "priority";
    String FIELD_CUSTOM_FIELDS = "custom_fields";

    // Embeds
    String EMBED_CUSTOMER = "customer";
    String EMBED_ASSIGNED_USER = "assigned_user";
    String EMBED_ASSIGNED_GROUP = "assigned_group";
    String EMBED_MESSAGE = "message";
    String EMBED_DRAFT = "draft";
    String EMBED_SENT_BY = "sent_by";
    String EMBED_ENTERED_BY = "entered_by";

    /**
     * Retrieve a paginated list of all cases
     * @see <a href="http://dev.desk.com/API/cases/#list">http://dev.desk.com/API/cases/#list</a>
     *
     * @param perPage the total filters per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @param embed what to embed
     * @param fields the fields requested
     * @return a case api response
     */
    @GET(CASES_URI)
    Call<ApiResponse<Case>> getCases(@Query("per_page") int perPage, @Query("page") int page,
                                    @Query("sort_field") String sortField, @Query("sort_direction") SortDirection sortDirection,
                                    @Query("embed") Embed embed, @Query("fields") Fields fields);

    /**
     * Retrieves cases for a given filter
     * @see <a href="http://dev.desk.com/API/cases/#list">http://dev.desk.com/API/cases/#list</a>
     *
     * @param filterId the id of the filter
     * @param perPage the total filters per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @param embed what to embed
     * @param fields the fields requested
     * @return a case api response
     */
    @GET(FILTERS_URI + "/{id}/" + CASES_URI)
    Call<ApiResponse<Case>> getCasesByFilter(@Path("id") int filterId, @Query("per_page") int perPage, @Query("page") int page,
                                                              @Query("sort_field") String sortField, @Query("sort_direction") SortDirection sortDirection,
                                                              @Query("embed") Embed embed, @Query("fields") Fields fields);

    /**
     * Searches for cases provided a query
     * @see <a href="http://dev.desk.com/API/cases/#search">http://dev.desk.com/API/cases/#search</a>
     *
     * @param query the query to search for
     * @param perPage the total cases per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @param embed what to embed
     * @param fields the fields requested
     * @return a case api response
     */
    @GET(CASES_URI + "/search")
    Call<ApiResponse<Case>> searchCases(@Query("q") String query, @Query("per_page") int perPage, @Query("page") int page,
                     @Query("sort_field") String sortField, @Query("sort_direction") SortDirection sortDirection,
                     @Query("embed") Embed embed, @Query("fields") Fields fields);

    /**
     * Searches for cases by the updated_at date &amp; time
     * @see <a href="http://dev.desk.com/API/cases/#search">http://dev.desk.com/API/cases/#search</a>
     *
     * @param since_updated_at filters cases where the updated_at is &gt;= the specified date &amp; time (in Unix time)
     * @param perPage the total cases per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @param embed what to embed
     * @param fields the fields requested
     * @return a case api response
     */
    @GET(CASES_URI + "/search")
    Call<ApiResponse<Case>> searchCasesByUpdatedDate(@Query("since_updated_at") int since_updated_at,
        @Query("per_page") int perPage, @Query("page") int page, @Query("sort_field") String sortField,
        @Query("sort_direction") SortDirection sortDirection, @Query("embed") Embed embed,
        @Query("fields") Fields fields);

    @GET(CASES_URI + "/search")
    Call<ApiResponse<Case>> searchCasesById(@Query("since_id") int since_id, @Query("per_page") int perPage,
        @Query("page") int page, @Query("sort_field") String sortField,
        @Query("sort_direction") SortDirection sortDirection, @Query("embed") Embed embed,
        @Query("fields") Fields fields);

    /**
     * Gets the case by id
     * @see <a href="http://dev.desk.com/API/cases/#show">http://dev.desk.com/API/cases/#show</a>
     *
     * @param caseId the id of the case
     * @param embed what to embed
     * @param fields the fields requested
     * @return a case
     */
    @GET(CASES_URI + "/{id}")
    Call<Case> getCaseById(@Path("id") int caseId, @Query("embed") Embed embed, @Query("fields") Fields fields);

    /**
     * Locks or unlocks a case
     * @see <a href="http://dev.desk.com/API/cases/#update">http://dev.desk.com/API/cases/#update</a>
     *
     * @param caseId the id of the case
     * @param caseLock the case lock
     * @return a case
     */
    @PATCH(CASES_URI + "/{id}")
    Call<Case> updateCaseLock(@Path("id") int caseId, @Body CaseLock caseLock);

    /**
     * Updates a case
     * @see <a href="http://dev.desk.com/API/cases/#update">http://dev.desk.com/API/cases/#update</a>
     *
     * @param caseId the id of the case
     * @param updatedCase the updated case
     * @return a case
     */
    @PATCH(CASES_URI + "/{id}")
    Call<Case> updateCase(@Path("id") int caseId, @Body Case updatedCase);

    /**
     * Updates a case
     * @see <a href="http://dev.desk.com/API/cases/#update">http://dev.desk.com/API/cases/#update</a>
     *
     * @param caseId the id of the case
     * @param updatedCase the updated case
     * @param embed what to embed in the response
     * @param fields the fields requested in the response
     * @return a case
     */
    @PATCH(CASES_URI + "/{id}")
    Call<Case> updateCase(@Path("id") int caseId, @Body Case updatedCase, @Query("embed") Embed embed,
                    @Query("fields") Fields fields);

    /**
     * Creates a case
     * @param newCase the case to create
     * @param embed the fields to embed in the case response
     * @param fields the fields to return in the case response
     * @return the created case
     */
    @POST(CASES_URI)
    Call<Case> createCase(@Body Case newCase, @Query("embed") Embed embed, @Query("fields") Fields fields);

    /**
     * Updates a case message
     * @see <a href="http://dev.desk.com/API/cases/#message-update">http://dev.desk.com/API/cases/#message-update</a>
     *
     * @param caseId the id of the case
     * @param updatedMessage the updated message
     * @return a message
     */
    @PATCH(CASES_URI + "/{id}/message")
    Call<Message> updateCaseMessage(@Path("id") int caseId, @Body Message updatedMessage);

    /**
     * Updates a case reply
     * @see <a href="http://dev.desk.com/API/cases/#replies-update">http://dev.desk.com/API/cases/#replies-update</a>
     *
     * @param caseId the id of the case
     * @param replyId the id of the reply
     * @param updatedReply the updated reply
     * @return a message
     */
    @PATCH(CASES_URI + "/{caseId}/" + REPLIES_URI + "/{replyId}")
    Call<Message> updateCaseReply(@Path("caseId") int caseId, @Path("replyId") int replyId, @Body Message updatedReply);

    /**
     * Retrieves a case's feed
     * @see <a href="http://dev.desk.com/API/cases/#feed">http://dev.desk.com/API/cases/#feed</a>
     *
     * @param caseId the id of the case
     * @param perPage the total filters per page
     * @param page the page requested
     * @param sortDirection the direction to sort
     * @return a response
     */
    @GET(CASES_URI + "/{id}/feed")
    Call<ApiResponse<Message>> getCaseFeed(@Path("id") int caseId, @Query("per_page") int perPage, @Query("page") int page,
                     @Query("sort_direction") SortDirection sortDirection);

    /**
     * Retrieves a draft for a case if it exists
     * @see <a href="http://dev.desk.com/API/cases/#drafts-show">http://dev.desk.com/API/cases/#drafts-show</a>
     *
     * @param caseId the id of the case
     * @param embed what to embed
     * @return a message
     */
    @GET(CASES_URI + "/{id}/" + DRAFT_URI)
    Call<Message> getDraft(@Path("id") int caseId, @Query("embed") Embed embed);

    /**
     * Creates a draft
     * @see <a href="http://dev.desk.com/API/cases/#drafts-create">http://dev.desk.com/API/cases/#drafts-create</a>
     *
     * @param caseId the id of the case
     * @return a message
     */
    @POST(CASES_URI + "/{id}/" + DRAFT_URI)
    Call<Message> createDraft(@Path("id") int caseId);

    /**
     * Updates a draft
     * @see <a href="http://dev.desk.com/API/cases/#drafts-update">http://dev.desk.com/API/cases/#drafts-update</a>
     *
     * @param caseId the id of the case
     * @param draft the updated draft
     * @return a message
     */
    @PATCH(CASES_URI + "/{id}/" + DRAFT_URI)
    Call<Message> updateDraft(@Path("id") int caseId, @Body Message draft);

    /**
     * Creates a note
     * @see <a href="http://dev.desk.com/API/cases/#notes-create">http://dev.desk.com/API/cases/#notes-create</a>
     *
     * @param caseId the id of the case
     * @param note the note to create
     * @return a message
     */
    @POST(CASES_URI + "/{id}/" + NOTE_URI)
    Call<Message> createNote(@Path("id") int caseId, @Body Message note);

    /**
     * Retrieves a preview for applying a set of macros to a case
     * @see <a href="http://dev.desk.com/API/cases/#macros-preview">http://dev.desk.com/API/cases/#macros-preview</a>
     *
     * @param caseId the id of the case
     * @param body the desk case body
     * @return a macro response
     */
    @POST(CASES_URI + "/{id}/" + MACROS_URI + "/preview")
    Call<MacroResponse> previewMacro(@Path("id") int caseId, @Body Case body);

    /**
     * Retrieves a paginated list of all attachments for a case
     * @see <a href="http://dev.desk.com/API/cases/#attachments-list">http://dev.desk.com/API/cases/#attachments-list</a>
     *
     * @param caseId the id of the case
     * @param perPage the total attachments per page
     * @param page the page requested
     * @return an attachment api response
     */
    @GET(CASES_URI + "/{id}/" + ATTACHMENTS_URI)
    Call<ApiResponse<Attachment>> getAttachments(@Path("id") int caseId, @Query("per_page") int perPage, @Query("page") int page);
}
