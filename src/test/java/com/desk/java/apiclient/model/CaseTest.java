package com.desk.java.apiclient.model;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

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
public class CaseTest {

    private Case kase;

    @Before
    public void setup() throws Exception {
        this.kase = new Case();
    }

    @Test
    public void testConstructor() throws Exception {
        HashMap<String, String> customFields = new HashMap<>();
        Case ticket = new Case(customFields, "Test", "Test");
        assertEquals(customFields, ticket.getCustomFields());
    }

    @Test
    public void testWithAssignedUser() throws Exception {
        User user = new User();
        String url = "foo.com";
        UserLinks userLinks = mock(UserLinks.class);
        when(userLinks.getSelf()).thenReturn(new Link(url));
        user.setLinks(userLinks);
        Case ticket = Case.withAssignedUser(user);
        assertEquals(url, ticket.getAssignedUserLinkUrl());
    }

    @Test
    public void testWithAssignedGroup() throws Exception {
        Group group = new Group();
        String url = "foo.com";
        Links groupLinks = mock(Links.class);
        when(groupLinks.getSelf()).thenReturn(new Link(url));
        group.setLinks(groupLinks);
        Case ticket = Case.withAssignedGroup(group);
        assertEquals(url, ticket.getAssignedGroupLinkUrl());
    }

    @Test
    public void testWithAssignedGroupUnassignedUser() throws Exception {
        Group group = new Group();
        String url = "foo.com";
        Links groupLinks = mock(Links.class);
        when(groupLinks.getSelf()).thenReturn(new Link(url));
        group.setLinks(groupLinks);
        Case ticket = Case.withAssignedGroupUnassignedUser(group);
        assertEquals(url, ticket.getAssignedGroupLinkUrl());
    }

    @Test
    public void testGetAgentName() throws Exception {}

    @Test
    public void testGetGroupName() throws Exception {}

    @Test
    public void testSetCustomField() throws Exception {}

    @Test
    public void testGetMessageLinkUrl() throws Exception {}

    @Test
    public void testGetAttachmentsLinkUrl() throws Exception {}

    @Test
    public void testGetEmbeddedMessage() throws Exception {}

    @Test
    public void testGetEmbeddedDraft() throws Exception {}

    @Test
    public void testIsQna() throws Exception {}

    @Test
    public void testIsEmail() throws Exception {}

    @Test
    public void testAreAnswersDisallowed() throws Exception {}

    @Test
    public void testGetAssignedUserLinkUrl() throws Exception {}

    @Test
    public void testGetAssignedUserLink() throws Exception {}

    @Test
    public void testGetAssignedUserId() throws Exception {}





}