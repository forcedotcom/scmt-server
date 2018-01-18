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

package com.desk.java.apiclient;

import com.desk.java.apiclient.DeskClientBuilder.AuthType;
import com.desk.java.apiclient.model.CaseLock;
import com.desk.java.apiclient.service.*;
import com.desk.java.apiclient.util.ApiTokenSigningInterceptor;
import com.desk.java.apiclient.util.DeskClientUtils;
import com.desk.java.apiclient.util.ISO8601DateAdapter;
import com.desk.java.apiclient.util.OAuthSigningInterceptor;
import com.desk.java.apiclient.util.RetrofitHttpOAuthConsumer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import retrofit.CallAdapter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import static com.desk.java.apiclient.DeskClientBuilder.API_BASE_PATH;
import static com.desk.java.apiclient.DeskClientBuilder.AuthType.OAUTH;

public class DeskClient {

    private final String hostname;
    private final String apiToken;
    private final String consumerKey;
    private final String consumerSecret;
    private final String accessToken;
    private final String accessTokenSecret;
    private final Cache responseCache;
    private final List<Interceptor> applicationInterceptors;
    private final List<Interceptor> networkInterceptors;
    private final AuthType authType;

    private final Retrofit restAdapter;
    private final RetrofitHttpOAuthConsumer oAuthConsumer;

    private UserService userService;
    private SiteService siteService;
    private LabelService labelService;
    private CustomFieldsService customFieldsService;
    private GroupService groupService;
    private MacroService macroService;
    private OutboundMailboxService outboundMailboxService;
    private FilterService filterService;
    private CaseService caseService;
    private CompanyService companyService;
    private CustomerService customerService;
    private PermissionService permissionService;
    private TwitterUserService twitterUserService;
    private TopicService topicService;
    private ArticleService articleService;
    private InboundMailboxService inboundMailboxService;
    private BrandService brandService;
    private NoteService noteService;
    private InteractionService interactionService;

    /**
     * Creates a {@link DeskClient} using the provided {@link DeskClientBuilder}.
     *
     * @return the desk client
     */
    public static DeskClient create(DeskClientBuilder builder) {
        if (builder == null) {
            throw new IllegalStateException("DeskClientBuilder cannot be null.");
        }
        return new DeskClient(builder);
    }

    DeskClient(DeskClientBuilder builder) {
        this.authType = builder.authType;
        this.apiToken = builder.apiToken;
        this.hostname = builder.hostname;
        this.consumerKey = builder.consumerKey;
        this.consumerSecret = builder.consumerSecret;
        this.accessToken = builder.accessToken;
        this.accessTokenSecret = builder.accessTokenSecret;
        this.responseCache = builder.responseCache;
        this.applicationInterceptors = builder.applicationInterceptors;
        this.networkInterceptors = builder.networkInterceptors;
        this.oAuthConsumer = createOAuthConsumer();

        Retrofit.Builder retrofitBuilder = createRestAdapter();
        if (builder.callAdapters != null && !builder.callAdapters.isEmpty()) {
            for (CallAdapter.Factory callAdapter : builder.callAdapters) {
                retrofitBuilder.addCallAdapterFactory(callAdapter);
            }
        }

        this.restAdapter = retrofitBuilder.build();
    }

    /**
     * Gets the hostname
     * @return the hostname
     */
    public String getHostname() {
        return this.hostname;
    }

    /**
     * Gets the url with the path provided.
     * @param path the path
     * @return the url
     */
    public String getUrl(String path) {
        return DeskClientUtils.buildUrl(hostname, path);
    }

    /**
     * Signs the provided URL with OAuth credentials
     * @param url the url to sign
     * @return the signed url
     * @throws OAuthCommunicationException if an error occurs communicating with the OAuth server
     * @throws OAuthExpectationFailedException if consumer key or consumer secret are not set in the OAuthConsumer
     * @throws OAuthMessageSignerException if an error occurs while signing the url
     */
    public String signUrl(String url) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        if (OAUTH == authType) {
            return oAuthConsumer.sign(url);
        } else {
            return url;
        }
    }

    /**
     * Clears all cached response objects from the response cache if one exists.
     */
    public void clearResponseCache() {
        if (responseCache != null) {
            try {
                responseCache.evictAll();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get the Desk User service
     *
     * @return the default Desk User service
     */
    @NotNull
    public UserService users() {
        if (userService == null) {
            userService = restAdapter.create(UserService.class);
        }
        return userService;
    }

    /**
     * Get the Desk Site service
     *
     * @return the default Desk Site service
     */
    @NotNull
    public SiteService sites() {
        if (siteService == null) {
            siteService = restAdapter.create(SiteService.class);
        }
        return siteService;
    }

    /**
     * Get the Desk Label service
     *
     * @return the default Desk Label service
     */
    @NotNull
    public LabelService labels() {
        if (labelService == null) {
            labelService = restAdapter.create(LabelService.class);
        }
        return labelService;
    }

    /**
     * Get the Desk Custom Fields service
     *
     * @return the default Desk Custom Fields service
     */
    @NotNull
    public CustomFieldsService customFields() {
        if (customFieldsService == null) {
            customFieldsService = restAdapter.create(CustomFieldsService.class);
        }
        return customFieldsService;
    }

    /**
     * Get the Desk Group service
     *
     * @return the default Desk Group service
     */
    @NotNull
    public GroupService groups() {
        if (groupService == null) {
            groupService = restAdapter.create(GroupService.class);
        }
        return groupService;
    }

    /**
     * Get the Desk Macro service
     *
     * @return the default Desk Macro service
     */
    @NotNull
    public MacroService macros() {
        if (macroService == null) {
            macroService = restAdapter.create(MacroService.class);
        }
        return macroService;
    }

    /**
     * Get the Desk Outbound Mailbox service
     *
     * @return the default Desk Outbound Mailbox service
     */
    @NotNull
    public OutboundMailboxService outboundMailboxes() {
        if (outboundMailboxService == null) {
            outboundMailboxService = restAdapter.create(OutboundMailboxService.class);
        }
        return outboundMailboxService;
    }

    /**
     * Get the Desk Filter service
     *
     * @return the default Desk Filter service
     */
    @NotNull
    public FilterService filters() {
        if (filterService == null) {
            filterService = restAdapter.create(FilterService.class);
        }
        return filterService;
    }

    /**
     * Get the Desk Case service
     *
     * @return the default Desk Case service
     */
    @NotNull
    public CaseService cases() {
        if (caseService == null) {
            caseService = restAdapter.create(CaseService.class);
        }
        return caseService;
    }

    /**
     * Get the Desk Company service
     *
     * @return the default Desk Company service
     */
    @NotNull
    public CompanyService companies() {
        if (companyService == null) {
            companyService = restAdapter.create(CompanyService.class);
        }
        return companyService;
    }

    /**
     * Get the Desk Customer service
     *
     * @return the default Desk Customer service
     */
    @NotNull
    public CustomerService customers() {
        if (customerService == null) {
            customerService = restAdapter.create(CustomerService.class);
        }
        return customerService;
    }

    /**
     * Get the Desk Permission service
     *
     * @return the default Desk Permission service
     */
    @NotNull
    public PermissionService permissions() {
        if (permissionService == null) {
            permissionService = restAdapter.create(PermissionService.class);
        }
        return permissionService;
    }

    /**
     * Get the Desk Twitter User service
     *
     * @return the default Desk Twitter User service
     */
    @NotNull
    public TwitterUserService twitterUsers() {
        if (twitterUserService == null) {
            twitterUserService = restAdapter.create(TwitterUserService.class);
        }
        return twitterUserService;
    }

    /**
     * Get the Desk Topic service
     *
     * @return the default Desk Topic service
     */
    @NotNull
    public TopicService topics() {
        if (topicService == null) {
            topicService = restAdapter.create(TopicService.class);
        }
        return topicService;
    }

    /**
     * Get the Desk Article service
     *
     * @return the default Desk Article service
     */
    @NotNull
    public ArticleService articles() {
        if (articleService == null) {
            articleService = restAdapter.create(ArticleService.class);
        }
        return articleService;
    }

    /**
     * Get the Desk Inbound Mailbox service
     *
     * @return the default Desk Inbound Mailbox service
     */
    @NotNull
    public InboundMailboxService inboundMailboxes() {
        if (inboundMailboxService == null) {
            inboundMailboxService = restAdapter.create(InboundMailboxService.class);
        }
        return inboundMailboxService;
    }

    /**
     * Get the Desk Brand service
     *
     * @return the default Desk Brand service
     */
    @NotNull
    public BrandService brands() {
        if (brandService == null) {
            brandService = this.getRestAdapter().create(BrandService.class);
        }
        return brandService;
    }

    /**
     * Get the Desk Note service
     *
     * @return the default Desk Note service
     */
    @NotNull
    public NoteService notes()
    {
        if (noteService == null)
        {
            noteService = this.getRestAdapter().create(NoteService.class);
        }
        return noteService;
    }

    /**
     * Get the Desk Interaction service
     *
     * @return the default Desk Interaction service
     */
    @NotNull
    public InteractionService interactions()
    {
        if (interactionService == null)
        {
            interactionService = this.getRestAdapter().create(InteractionService.class);
        }
        return interactionService;
    }

    protected Retrofit getRestAdapter() {
        return restAdapter;
    }

    private Retrofit.Builder createRestAdapter() {
        return new Retrofit.Builder()
                .baseUrl(getUrl(API_BASE_PATH))
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(createGson()));
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new ISO8601DateAdapter())
                .registerTypeAdapter(CaseLock.class, CaseLock.TYPE_ADAPTER)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();

        // if we have response cache let's use it!
        if (responseCache != null) {
            okHttpClient.setCache(responseCache);
        }

        // add auth interceptors
        switch (authType) {
            case OAUTH:
                if (oAuthConsumer == null) {
                    throw new IllegalStateException("a RetrofitHttpOAuthConsumer must be created before creating OKClient");
                }
                okHttpClient.interceptors().add(new OAuthSigningInterceptor(oAuthConsumer));
                break;
            case API_TOKEN:
                okHttpClient.interceptors().add(new ApiTokenSigningInterceptor(apiToken));
                break;
            default:
                throw new IllegalStateException("AuthType " + authType + " isn't supported.");
        }

        // add all other application interceptors
        if (applicationInterceptors != null && !applicationInterceptors.isEmpty()) {
            okHttpClient.interceptors().addAll(applicationInterceptors);
        }

        // add all other network interceptors
        if (networkInterceptors != null && !networkInterceptors.isEmpty()) {
            okHttpClient.networkInterceptors().addAll(networkInterceptors);
        }

        return okHttpClient;
    }

    private RetrofitHttpOAuthConsumer createOAuthConsumer() {
        if (OAUTH == authType) {
            RetrofitHttpOAuthConsumer consumer = new RetrofitHttpOAuthConsumer(consumerKey, consumerSecret);
            consumer.setTokenWithSecret(accessToken, accessTokenSecret);
            return consumer;
        } else {
            return null;
        }
    }
    
    public String getAuthInfo(){
    	return "API Token "+this.apiToken+" AuthType: "+ this.authType;
    }
}