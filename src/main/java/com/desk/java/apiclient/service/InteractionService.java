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

import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Interaction;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface InteractionService
{

    String INTERACTION_URI = "interactions"
    private String fields = "body,body_html,body_text,id,created_at,updated_at,sent_at,direction,sent_at,to,from,cc,bcc,status,subject,hidden_at,type,entered_at,twitter_status_id,facebook_id,facebook_id,facebook_name,liked,event_type,public_url,answers_disallowed_at,agent_answer_count,customer_answer_count,rating,rating_count,rating_score";

    /**
     * Retrieve a page of notes
     *
     * @param since_id the id that will be the lower boundary of the results 
     * @param per_page how many notes to return per page (max 100)
     * @return an interaction api response
     */
    @GET(INTERACTION_URI + "")
    Call<ApiResponse<Interaction>> getInteractions(@Query("per_page") int perPage, @Query("since_id") int since_id, @Query("fields") String fields);
}
