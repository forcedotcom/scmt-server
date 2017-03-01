/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Salesforce.com, Inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.desk.java.apiclient.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 *     Unit tests for {@link User}
 * </p>
 *
 * Created by Jerrell Mardis
 * Copyright (c) 2016 Desk.com. All rights reserved.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() throws Exception {
        user = new User();
    }

    @Test
    public void getLinksDoesNotReturnNull() {
        assertNotNull(user.getLinks());
        user.setLinks(null);
        assertNotNull(user.getLinks());
    }

    @Test
    public void getFiltersUrlReturnsUrlWhenSet() {
        UserLinks userLinks = new UserLinks();
        String url = "foo.com";
        Link filters = new Link(url);
        userLinks.setFilters(filters);
        user.setLinks(userLinks);
        assertEquals(url, user.getFiltersUrl());
    }

    @Test
    public void getFiltersUrlReturnsNullWhenNotSet() {
        user.setLinks(null);
        assertNull(user.getFiltersUrl());
    }

    @Test
    public void getSelfLinkUrlDoesNotReturnNull() {
        user.setLinks(null);
        assertNotNull(user.getSelfLink());
    }

    @Test
    public void getSelfLinkUrlDoesReturnSelfUrlWhenSet() {
        String url = "foo.com";
        UserLinks userLinks = mock(UserLinks.class);
        when(userLinks.getSelf()).thenReturn(new Link(url));
        user.setLinks(userLinks);
        assertEquals(url, user.getSelfLinkUrl());
    }

    @Test
    public void getIdReturnsId() {
        user.setId(1);
        assertEquals(1, user.getId());
    }

    @Test
    public void getNameReturnsEmptyStringWhenNotSet() {
        user.setName(null);
        assertEquals("", user.getName());
    }

    @Test
    public void getAvatarReturnsEmptyStringWhenNotSet() {
        user.setAvatar(null);
        assertEquals("", user.getAvatar());
    }

    @Test
    public void equalsOnUserId() {
        User a = new User();
        User b = new User();

        assertTrue(a.equals(b));
        b.setId(1);
        assertFalse(a.equals(b));
        a.setId(1);
        assertTrue(a.equals(b));
    }

    @Test
    public void hashCodeReturnsId() {
        user.setId(123);
        assertEquals(123, user.hashCode());
    }
}