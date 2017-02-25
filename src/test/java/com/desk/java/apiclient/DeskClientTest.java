/*******************************************************************************
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.desk.java.apiclient;

import static com.desk.java.apiclient.DeskClientBuilder.PROTOCOL_CONNECT;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.squareup.okhttp.Cache;

import java.io.File;
import java.net.URI;

public class DeskClientTest {

    private static final String TEST_HOST_NAME = "test.test.com";

    private DeskClient oAuthDeskClient;
    private DeskClient apiTokenDeskClient;

    @Before
    public void setup() {
        oAuthDeskClient = DeskClient.create(new DeskClientBuilder(
                TEST_HOST_NAME,
                "1234",
                "5678",
                "1234",
                "5678"
        ));
        apiTokenDeskClient = DeskClient.create(new DeskClientBuilder(
                TEST_HOST_NAME,
                "1234"
        ));
    }
    
    @Test(expected=IllegalStateException.class)
    public void noBuilderExceptionTest() throws Exception {
    	DeskClient.create(null);
    }
    
    @Test
    public void getHostNameReturnsHostName() throws Exception {
        assertEquals(TEST_HOST_NAME, oAuthDeskClient.getHostname());
    }

    @Test
    public void signUrlSignsUrl() throws Exception {
        final String url = "http://test.desk.com";
        String signed = oAuthDeskClient.signUrl(url);
        assertNotEquals(url, signed);
        URI uri = new URI(signed);
        assertTrue(uri.getQuery().contains("oauth_signature"));
    }

    @Test
    public void signUrlDoesNotSignUrl() throws Exception {
        final String url = "http://test.desk.com";
        String signed = apiTokenDeskClient.signUrl(url);
        assertEquals(url, signed);
    }

    @Test
    public void usersIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.users());
    }

    @Test
    public void sitesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.sites());
    }

    @Test
    public void labelsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.labels());
    }

    @Test
    public void customFieldsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.customFields());
    }

    @Test
    public void groupsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.groups());
    }

    @Test
    public void macrosIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.macros());
    }

    @Test
    public void outboundMailboxesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.outboundMailboxes());
    }

    @Test
    public void filtersIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.filters());
    }

    @Test
    public void casesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.cases());
    }

    @Test
    public void companiesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.companies());
    }

    @Test
    public void customersIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.customers());
    }

    @Test
    public void permissionsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.permissions());
    }

    @Test
    public void twitterUsersIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.twitterUsers());
    }

    @Test
    public void topicsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.topics());
    }

    @Test
    public void articlesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.articles());
    }

    @Test
    public void brandsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.brands());
    }

    @Test
    public void inboundMailboxesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.inboundMailboxes());
    }

    @Test
    public void notesIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.notes());
    }

    @Test
    public void interactionsIsNotNull() throws Exception {
        assertNotNull(apiTokenDeskClient.interactions());
    }

    @Test
    public void getUrlReturnsCorrectUrl() throws Exception {
        assertEquals(PROTOCOL_CONNECT + TEST_HOST_NAME + "/testpath", apiTokenDeskClient.getUrl("/testpath"));
    }
    
    @Test
    public void clearResponseCacheTest() throws Exception {
    	DeskClientBuilder builder = new DeskClientBuilder(null, null);
    	builder.responseCache = new Cache(new File(System.getProperty("java.io.tmpdir")), 10);
    	DeskClient client = new DeskClient(builder);
    	client.clearResponseCache();
    	assertEquals(builder.responseCache.getSize(), 0);
    }
    
    @Test
    public void getAuthInfoTest() throws Exception {
    	assertEquals(apiTokenDeskClient.getAuthInfo(), "API Token 1234 AuthType: API_TOKEN");
    }

}
