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
public class InteractionTest {

    private Interaction interaction;

    @Before
    public void setup() {
        this.interaction = new Interaction();
    }

    @Test
    public void getLinksDoesNotReturnNull() throws Exception {
        assertNotNull(interaction.getLinks());
        interaction.setLinks(null);
        assertNotNull(interaction.getLinks());
    }

    @Test
    public void getCaseIdIsNoId() throws Exception {
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getCaseLink()).thenReturn(null);
        assertEquals(Interaction.NO_ID, interaction.getCaseId());
    }

    @Test
    public void getCaseIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/cases/1");
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getCaseLink()).thenReturn(link);
        interaction.setLinks(interactionLinks);
        assertEquals(1, interaction.getCaseId());
    }

    @Test
    public void getSentByIdIsNoId() throws Exception {
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getSentBy()).thenReturn(null);
        assertEquals(Interaction.NO_ID, interaction.getSentById());
    }

    @Test
    public void getSentByIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/users/1");
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getSentBy()).thenReturn(link);
        interaction.setLinks(interactionLinks);
        assertEquals(1, interaction.getSentById());
    }

    @Test
    public void getEnteredByIdIsNoId() throws Exception {
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getEnteredBy()).thenReturn(null);
        assertEquals(Interaction.NO_ID, interaction.getEnteredById());
    }

    @Test
    public void getEnteredByIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/users/1");
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getEnteredBy()).thenReturn(link);
        interaction.setLinks(interactionLinks);
        assertEquals(1, interaction.getEnteredById());
    }

    @Test
    public void getCreatedByIdIsNoId() throws Exception {
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getCreatedBy()).thenReturn(null);
        assertEquals(Interaction.NO_ID, interaction.getCreatedById());
    }

    @Test
    public void getCreatedByIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/users/1");
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getCreatedBy()).thenReturn(link);
        interaction.setLinks(interactionLinks);
        assertEquals(1, interaction.getCreatedById());
    }

    @Test
    public void getInteractionTypeReturnsType() throws Exception {
        String[] types = new String[] { "email", "phone_call", "tweet", "chat_message", "facebook_post", "facebook_comment", "facebook_message", "community_question", "community_answer" };
        Link link = new Link();
        for (int i = 0; i < types.length; i++) {
            link.setClassName(types[i]);
            InteractionLinks interactionLinks = mock(InteractionLinks.class);
            when(interactionLinks.getSelf()).thenReturn(link);
            interaction.setLinks(interactionLinks);
            assertEquals(Interaction.InteractionType.valueOf(types[i].toUpperCase()), interaction.getInteractionType());
        }
    }

    @Test
    public void getInteractionReturnsUnknownIfNotSupported() throws Exception {
        Link link = new Link();
        link.setClassName("snapchat");
        InteractionLinks interactionLinks = mock(InteractionLinks.class);
        when(interactionLinks.getSelf()).thenReturn(link);
        interaction.setLinks(interactionLinks);
        assertEquals(Interaction.InteractionType.UNKNOWN, interaction.getInteractionType());
    }

    @Test
    public void getFromNameReturnsNamePortion() throws Exception {
        String from = "Ryan McCullough <rmccullough@salesforce.com>";
        interaction.setFrom(from);
        assertEquals("Ryan McCullough", interaction.getFromName());
    }

    @Test
    public void getFromNameReturnsNull() throws Exception {
        String from = "rmccullough@salesforce.com";
        interaction.setFrom(from);
        assertNull(interaction.getFromName());
    }

    @Test
    public void getFromNameReturnsAddressPortion() throws Exception {
        String from = "Ryan McCullough <rmccullough@salesforce.com>";
        interaction.setFrom(from);
        assertEquals("rmccullough@salesforce.com", interaction.getFromAddress());
    }

    // `getFromAddress` expects a "name <email>" format to work correctly,
    // in this test it would return "<Ryan McCullough>".
    // TODO: Fix the code to return null if no email is present?
    // @Test
    // public void getFromNameReturnsAddressNull() throws Exception {
    //     String from = "Ryan McCullough";
    //     interaction.setFrom(from);
    //     assertNull(interaction.getFromAddress());
    // }

    @Test
    public void isIncoming() throws Exception {
        interaction.setDirection("in");
        assertTrue(interaction.isIncoming());
        interaction.setDirection("out");
        assertFalse(interaction.isIncoming());
    }

}