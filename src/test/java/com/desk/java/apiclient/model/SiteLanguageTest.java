package com.desk.java.apiclient.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name of Salesforce.com nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class SiteLanguageTest {

    private SiteLanguage siteLanguage;

    @Before
    public void setUp() throws Exception {
        siteLanguage = new SiteLanguage();
    }

    @Test
    public void getSelfLink() throws Exception {
        assertNotNull(siteLanguage.getLinks());
        siteLanguage.setLinks(null);
        assertNotNull(siteLanguage.getLinks());
    }

    @Test
    public void getSelfLinkUrl() throws Exception {
        assertNotNull(siteLanguage.getSelfLink());
        siteLanguage.setLinks(null);
        assertNotNull(siteLanguage.getSelfLink());
    }

    @Test
    public void getSelfLinkUrlDoesReturnSelfUrlWhenSet() throws Exception {
        String url = "foo.com";
        Links siteLanguageLinks = mock(Links.class);
        when(siteLanguageLinks.getSelf()).thenReturn(new Link(url));
        siteLanguage.setLinks(siteLanguageLinks);
        assertEquals(url, siteLanguage.getSelfLinkUrl());
    }

    @Test
    public void equals() throws Exception {
        SiteLanguage a = new SiteLanguage();
        SiteLanguage b = new SiteLanguage();

        assertTrue(a.equals(b));
        b.setId("1");
        assertFalse(a.equals(b));
        a.setId("1");
        assertTrue(a.equals(b));
    }

}