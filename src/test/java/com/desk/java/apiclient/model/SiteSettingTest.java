package com.desk.java.apiclient.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Salesforce.com nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
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
public class SiteSettingTest {

    private SiteSetting setting;

    @Before
    public void setUp() throws Exception {
        setting = new SiteSetting();
    }

    @Test
    public void getValueAsString() throws Exception {
        setting.setValue(123);
        assertEquals("123", setting.getValueAsString());
    }

    @Test
    public void getValueAsInteger() throws Exception {
        setting.setValue("0");
        assertEquals((Integer) 0, setting.getValueAsInteger());
    }

    @Test
    public void getValueAsBoolean() throws Exception {
        setting.setValue("false");
        assertEquals(false, setting.getValueAsBoolean());
    }

    @Test
    public void getLinksDoesNotReturnNull() throws Exception {
        assertNotNull(setting.getLinks());
        setting.setLinks(null);
        assertNotNull(setting.getLinks());
    }

    @Test
    public void getSelfLinkUrlDoesNotReturnNull() throws Exception {
        assertNotNull(setting.getSelfLink());
        setting.setLinks(null);
        assertNotNull(setting.getSelfLink());
    }

    @Test
    public void getSelfLinkUrlDoesReturnSelfUrlWhenSet() throws Exception {
        String url = "foo.com";
        Links settingLinks = mock(Links.class);
        when(settingLinks.getSelf()).thenReturn(new Link(url));
        setting.setLinks(settingLinks);
        assertEquals(url, setting.getSelfLinkUrl());
    }

    @Test
    public void equals() throws Exception {
        SiteSetting a = new SiteSetting();
        SiteSetting b = new SiteSetting();

        assertTrue(a.equals(b));
        b.setName("test");
        assertFalse(a.equals(b));
        a.setName("test");
        assertTrue(a.equals(b));
    }
}