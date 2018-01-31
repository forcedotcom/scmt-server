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
import com.desk.java.apiclient.model.Article;
import com.desk.java.apiclient.model.Brand;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.model.Topic;

import org.jetbrains.annotations.Nullable;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TopicService {

    // URIs
    String TOPICS_URI = "topics";

    // Fields
    String FIELD_ID = "id";
    String FIELD_POSITION = "position";

    /**
     * Retrieve a list of all topics.
     * @see <a href="http://dev.desk.com/API/topics/#list">http://dev.desk.com/API/topics/#list</a>
     *
     * @param perPage the total filters per page
     * @param page the page requested
     * @param language the ISO language code. If null default to en
     * @param inSupportCenter true to include only articles that are in the support center
     * @param brandId the brand ID to limit results to or null for all brands
     * @param sortField the field to sort the topics on
     * @param sortDirection the direction to sort the topics
     * @return a topic api response
     */
    @GET(TOPICS_URI)
    Call<ApiResponse<Topic>> getTopics(@Query("per_page") int perPage, @Query("page") int page,
        @Nullable @Header("Accept-Language") String language,
        @Nullable @Query("in_support_center") Boolean inSupportCenter,
        @Nullable @Query("brand_id") Integer brandId,
        @Nullable @Query("sort_field") String sortField,
        @Nullable @Query("sort_direction") SortDirection sortDirection);

    /**
     * Retrieve a list of all articles within a topic
     *
     * @param language the ISO language code. If null default to en
     * @param topicId the topic ID to get the articles for
     * @param inSupportCenter true to include only articles that are in the support center
     * @return a article api response
     */
    @GET(TOPICS_URI + "/{topicId}/articles")
    Call<ApiResponse<Article>> getArticlesOfTopic(@Nullable @Header("Accept-Language") String language,
        @Path("topicId") long topicId,
        @Nullable @Query("in_support_center") Boolean inSupportCenter);

    /**
     * Retrieve a list of all brands within a topic
     *
     * @param topicId the topic ID to get the articles for
     * @return a brand api response
     */
    @GET(TOPICS_URI + "/{topicId}/brands")
    Call<ApiResponse<Brand>> getBrandsOfTopic(@Path("topicId") long topicId);

    /**
     * Retrieve a list of all translations of a topic
     *
     * @param topicId the topic ID to get the articles for
     * @return a topic api response
     */
    @GET(TOPICS_URI + "/{topicId}/translations")
    Call<ApiResponse<Topic>> getTranslationsOfTopic(@Path("topicId") long topicId);
}
