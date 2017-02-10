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
import com.desk.java.apiclient.model.Site;
import com.desk.java.apiclient.model.SiteBilling;
import com.desk.java.apiclient.model.SiteLanguage;
import com.desk.java.apiclient.model.SiteSetting;

import retrofit.Call;
import retrofit.http.GET;

public interface SiteService {

    String SITE_URI = "site";

    /**
     * Retrieves the site
     * @return a site
     */
    @GET(SITE_URI)
    Call<Site> getSite();

    /**
     * Retrieves the billing information for a site
     * @return a site billing
     */
    @GET(SITE_URI + "/billing")
    Call<SiteBilling> getSiteBilling();

    /**
     * Retrieves the languages for a site
     * @return a list of site languages
     */
    @GET(SITE_URI + "_settings")
    Call<ApiResponse<SiteSetting>> getSiteSettings();

    /**
     * Retrieves the languages for a site
     * @return a list of site languages
     */
    @GET(SITE_URI + "/languages")
    Call<ApiResponse<SiteLanguage>> getSiteLanguages();
}
