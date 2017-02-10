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
import com.desk.java.apiclient.model.Company;
import com.desk.java.apiclient.model.SortDirection;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CompanyService {

    String COMPANY_URI = "companies";

    /**
     * Retrieve a single company
     * @see <a href="http://dev.desk.com/API/companies/#show">http://dev.desk.com/API/companies/#show</a>
     *
     * @param companyId the company id
     * @return a company
     */
    @GET(COMPANY_URI + "/{id}")
    Call<Company> getCompany(@Path("id") int companyId);

    /**
     * Search for companies using the search parameter 'q' to specify search terms.
     * The 'q' parameter can contain a company name, customer name or the value of a custom company field.
     * @see <a href="http://dev.desk.com/API/companies/#show">http://dev.desk.com/API/companies/#show</a>
     *
     * @param query the search query
     * @param perPage the total companies per page
     * @param page the page requested
     * @param sortField the field to sort on
     * @param sortDirection the direction to sort
     * @return a company api response
     */
    @GET(COMPANY_URI + "/search")
    Call<ApiResponse<Company>> searchCompanies(@Query("q") String query, @Query("per_page") int perPage,
                                         @Query("page") int page, @Query("sort_field") String sortField,
                                         @Query("sort_direction") SortDirection sortDirection);
    
    @GET(COMPANY_URI+"?fields=id,name,domains,external_id,custom_fields")    
    Call<ApiResponse<Company>> getCompanies(@Query("per_page") int perPage,
            @Query("page") int page);
    
    /**
     * Retrieve multiple companies by Id.
     * https://company.desk.com/api/v2/companies?ids=1,2,4,7
     * @param ids Comma-separated list of company id's to retrieve.
     * @return a company api response
     */
    // Specifying 'external_id' in the 'fields' causes only companies with an 'external_id' to be returned.
    //@GET(COMPANY_URI+"?fields=id,name,domains,created_at,updated_at,external_id,custom_fields")
    @GET(COMPANY_URI+"?fields=id,name,domains,created_at,updated_at,custom_fields")
    Call<ApiResponse<Company>> getCompaniesByIds(@Query("ids") String ids);
    
}
