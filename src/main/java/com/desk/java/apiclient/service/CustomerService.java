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
import com.desk.java.apiclient.model.Case;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.Embed;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.SortDirection;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CustomerService {

    // URIs
    String COMPANIES_URI = "companies";
    String CUSTOMERS_URI = "customers";

    // Embeds
    String EMBED_FACEBOOK_USER = "facebook_user";
    String EMBED_TWITTER_USER = "twitter_user";

    /**
     * Retrieve a single customer
     * @see <a href="http://dev.desk.com/API/customers/#show">http://dev.desk.com/API/customers/#show</a>
     *
     * @param customerId the customer id
     * @param embed the objects to embed
     * @return a customer
     */
    @GET(CUSTOMERS_URI + "/{id}")
    Call<Customer> getCustomer(@Path("id") int customerId, @Query("embed") Embed embed);

    /**
     * Updates a customer
     * @see <a href="http://dev.desk.com/API/customers/#update">http://dev.desk.com/API/customers/#update</a>
     *
     * @param customerId the customer id
     * @param updatedCustomer the updated customer
     * @return a customer
     */
    @PATCH(CUSTOMERS_URI + "/{id}")
    Call<Customer> updateCustomer(@Path("id") int customerId, @Body Customer updatedCustomer);

    /**
     * Creates a customer
     * @see <a href="http://dev.desk.com/API/customers/#create">http://dev.desk.com/API/customers/#create</a>
     *
     * @param newCustomer the customer to create
     * @return a customer
     */
    @POST(CUSTOMERS_URI)
    Call<Customer> createCustomer(@Body Customer newCustomer);

    /**
     * Creates a case for the customer
     * @see <a href="http://dev.desk.com/API/cases/#create">http://dev.desk.com/API/cases/#create</a>
     *
     * @param customerId the customer id
     * @param deskCase the case to create
     * @return a case
     */
    @POST(CUSTOMERS_URI + "/{id}/cases")
    Call<Case> createCaseForCustomer(@Path("id") int customerId, @Body Case deskCase);

    /**
     * Searches for customers
     * @see <a href="http://dev.desk.com/API/customers/#search">http://dev.desk.com/API/customers/#search</a>
     *
     * @param query the query searching across the following fields: firstname, lastname, name, email &amp; phone
     * @param embed the fields to embed
     * @return a customer api response
     */
    @GET(CUSTOMERS_URI + "/search")
    Call<ApiResponse<Customer>> searchCustomers(@Query("q") String query, @Query("embed") Embed embed);

    /**
     * Retrieves customers by the company provided.
     *
     * @param companyId the company Id
     * @param perPage the total cases per page
     * @param page the page requested
     * @return a customer response
     */
    @GET(COMPANIES_URI + "/{id}/" + CUSTOMERS_URI)
    Call<ApiResponse<Customer>> getCustomersByCompany(@Path("id") int companyId, @Query("per_page") int perPage, @Query("page") int page);
    
    /**
     * Retrieves all customers.
     *
     * @param perPage the total cases per page
     * @param page the page requested
     * @return a customer response
     */
    @GET(CUSTOMERS_URI)
    Call<ApiResponse<Customer>> getCustomers(@Query("per_page") int perPage, @Query("page") int page);
    
    /**
     * Searches for customers by the updated_at date &amp; time
     *
     * @param since_id filters customers where the id is &gt;= the specified id
     * @param perPage the total customers per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @param fields the fields requested
     * @return a customer api response
     */
    @GET(CUSTOMERS_URI + "/search")
    Call<ApiResponse<Customer>> searchCustomersById(@Query("since_id") Integer since_id,
        @Query("per_page") int perPage, @Query("page") int page, @Query("sort_field") String sortField, 
        @Query("sort_direction") SortDirection sortDirection, @Query("fields") Fields fields);
    
    @GET(CUSTOMERS_URI + "/search")
    Call<ApiResponse<Customer>> searchCustomersByUpdatedAt(@Query("since_updated_at") Integer since_updated_at,
        @Query("per_page") int perPage, @Query("page") int page, @Query("sort_field") String sortField, 
        @Query("sort_direction") SortDirection sortDirection, @Query("fields") Fields fields);
}
