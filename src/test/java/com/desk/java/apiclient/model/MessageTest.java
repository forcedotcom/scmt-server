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
public class MessageTest {

    private Message message;

    @Before
    public void setup() throws Exception {
        message = new Message();
    }

    @Test
    public void getEmbeddedDoesNotReturnNull() throws Exception {
        assertNotNull(message.getEmbedded());
        message.setEmbedded(null);
        assertNotNull(message.getEmbedded());
    }

    @Test
    public void getLinksDoesNotReturnNull() throws Exception {
        assertNotNull(message.getLinks());
        message.setLinks(null);
        assertNotNull(message.getLinks());
    }

    @Test
    public void setOutgoingStatusIsSentForPhone() throws Exception {
        message.setStatus(null);
        assertNull(message.getStatus());
        message.setOutgoingStatus(CaseType.PHONE);
        assertEquals(MessageStatus.SENT, message.getStatus());
    }

    @Test
    public void setOutgoingStatusIsSentForQna() throws Exception {
        message.setStatus(null);
        assertNull(message.getStatus());
        message.setOutgoingStatus(CaseType.QNA);
        assertEquals(MessageStatus.SENT, message.getStatus());
    }

    @Test
    public void setOutgoingStatusIsPendingForOthers() throws Exception {
        message.setStatus(null);
        assertNull(message.getStatus());
        message.setOutgoingStatus(null);
        assertEquals(MessageStatus.PENDING, message.getStatus());
    }

    @Test
    public void findUserLinkUrlWithSentBy() throws Exception {
        String link = "/api/v2/users/1";
        message.setLinks(new MessageLinks());
        assertEquals("", message.findUserLinkUrl());
        assertNull(message.getSentByLink());
        assertNull(message.getEnteredByLink());
        assertNull(message.getUserLink());
        message.getLinks().setSentBy(new Link(link));
        assertEquals(link, message.findUserLinkUrl());
    }

    @Test
    public void findUserLinkUrlWithEnteredBy() throws Exception {
        String link = "/api/v2/users/1";
        message.setLinks(new MessageLinks());
        assertEquals("", message.findUserLinkUrl());
        assertNull(message.getSentByLink());
        assertNull(message.getEnteredByLink());
        assertNull(message.getUserLink());
        message.getLinks().setEnteredBy(new Link(link));
        assertEquals(link, message.findUserLinkUrl());
    }

    @Test
    public void findUserLinkUrlWithUserLink() throws Exception {
        String link = "/api/v2/users/1";
        message.setLinks(new MessageLinks());
        assertEquals("", message.findUserLinkUrl());
        assertNull(message.getSentByLink());
        assertNull(message.getEnteredByLink());
        assertNull(message.getUserLink());
        message.getLinks().setUser(new Link(link));
        assertEquals(link, message.findUserLinkUrl());
    }

    @Test
    public void getMessageTypeIsNote() throws Exception {
        Link link = new Link("/api/v2/cases/1/message");
        link.setClassName("note");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getSelf()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(Message.MessageType.NOTE, message.getMessageType());
    }

    @Test
    public void getMessageTypeIsReply() throws Exception {
        Link link = new Link("/api/v2/cases/1/message");
        link.setClassName("email");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getSelf()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(Message.MessageType.REPLY, message.getMessageType());
    }

    @Test
    public void getOutboundMailboxLinkIsNoId() throws Exception {
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getOutboundMailbox()).thenReturn(null);
        assertEquals(Message.NO_ID, message.getOutboundMailboxId());
    }

    @Test
    public void getOutboundMailboxLinkIsLinkId() throws Exception {
        Link link = new Link("/api/v2/mailboxes/outbound/1");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getOutboundMailbox()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(1, message.getOutboundMailboxId());
    }

    @Test
    public void getCaseIdIsNoId() throws Exception {
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getCaseLink()).thenReturn(null);
        assertEquals(Message.NO_ID, message.getCaseId());
    }

    @Test
    public void getCaseIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/cases/1");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getCaseLink()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(1, message.getCaseId());
    }

    @Test
    public void getUserIdIsNoId() throws Exception {
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getUser()).thenReturn(null);
        assertEquals(Message.NO_ID, message.getUserId());
    }

    @Test
    public void getUserIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/cases/1");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getUser()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(1, message.getUserId());
    }

    @Test
    public void getCustomerIdIsNoId() throws Exception {
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getCustomer()).thenReturn(null);
        assertEquals(Message.NO_ID, message.getCustomerId());
    }

    @Test
    public void getCustomerIdIsLinkId() throws Exception {
        Link link = new Link("/api/v2/customers/1");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getCustomer()).thenReturn(link);
        message.setLinks(messageLinks);
        assertEquals(1, message.getCustomerId());
    }

    @Test
    public void equals() throws Exception {
        Message a = new Message();
        Message b = new Message();
        // TODO: This should probably be false.
        // assertFalse(a.equals(b));
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals(true));
        a.setId(1);
        b.setId(2);
        assertFalse(a.equals(b));
        b.setId(1);
        assertTrue(a.equals(b));
    }

    @Test
    public void isReply() throws Exception {
        Link link = new Link("/api/v2/cases/1/notes/1");
        MessageLinks messageLinks = mock(MessageLinks.class);
        when(messageLinks.getSelf()).thenReturn(link);
        message.setLinks(messageLinks);
        assertFalse(message.isReply());
        link.setHref("/api/v2/cases/1/replies/1");
        assertTrue(message.isReply());
    }

    @Test
    public void toStringBuildsObject() throws Exception {
        assertTrue(message.toString().contains("Message Object"));
    }

}