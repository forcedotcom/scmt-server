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

package com.desk.java.apiclient.model;


import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class MessageLinks extends Links implements Serializable {

    private static final long serialVersionUID = 8344480030924031768L;

    private Link hiddenBy;
    private Link answersDisallowedBy;
    private Link user;
    private Link outboundMailbox;
    private Link sentBy;
    private Link enteredBy;
    @SerializedName("case")
    private Link caseLink;
    private Link customer;
    private Link attachments;

    @Nullable
    public Link getHiddenBy() {
        return this.hiddenBy;
    }

    public void setHiddenBy(@Nullable Link hb) {
        this.hiddenBy = hb;
    }

    @Nullable
    public Link getAnswersDisallowedBy() {
        return this.answersDisallowedBy;
    }

    public void setAnswersDisallowedBy(@Nullable Link ad) {
        this.answersDisallowedBy = ad;
    }

    @Nullable
    public Link getOutboundMailbox() {
        return this.outboundMailbox;
    }

    public void setOutboundMailbox(@Nullable Link om) {
        this.outboundMailbox = om;
    }

    @Nullable
    public Link getUser() {
        return this.user;
    }

    public void setUser(@Nullable Link u) {
        this.user = u;
    }

    @Nullable
    public Link getSentBy() {
        return this.sentBy;
    }

    public void setSentBy(@Nullable Link sb) {
        this.sentBy = sb;
    }

    @Nullable
    public Link getEnteredBy() {
        return this.enteredBy;
    }

    public void setEnteredBy(@Nullable Link eb) {
        this.enteredBy = eb;
    }

    @Nullable
    public Link getCaseLink() {
        return caseLink;
    }

    @Nullable
    public Link getCustomer() {
        return this.customer;
    }

    @Nullable
    public Link getAttachmentsLink() {
        return this.attachments;
    }

}
