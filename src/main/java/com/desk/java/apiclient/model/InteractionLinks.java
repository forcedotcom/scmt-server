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

public class InteractionLinks extends Links implements Serializable
{

    private static final long serialVersionUID = 1564333269335931295L;

    // common links
    @SerializedName("case")
    private Link caseLink;
    private Link customer;
    private Link sent_by;
    private Link erased_by;
    private Link hidden_by;

    // email links
    private Link splits;
    private Link attachments;
    
    // phone links
    private Link entered_by;
    
    // chat links
    private Link created_by;
    
    // twitter links
    private Link twitter_account;
    private Link favorited_by_twitter_account;
    private Link retweeted_by;
    private Link favorited_by;
    
    // community question links
    private Link topic;
    private Link best_answer;
    private Link answers_disallowed_by;
    
    // common links
    @Nullable
    /**
     * Retrieve the case this interaction belongs to
     * @return the case this interaction belongs to
     */
    public Link getCaseLink()
    {
        return caseLink;
    }
    
    @Nullable
    /**
     * Retrieve the customer that sent this interaction.
     * @return the customer that sent this interaction
     */
    public Link getCustomer()
    {
        return this.customer;
    }
    
    @Nullable
    /**
     * Retrieve the user that erased this interaction.
     * @return the user that erased this interaction
     */
    public Link getErasedBy()
    {
        return this.erased_by;
    }
    
    @Nullable
    /**
     * Retrieve the user that hid this interaction.
     * @return the user that hid this interaction
     */
    public Link getHiddenBy()
    {
        return this.hidden_by;
    }
    
    @Nullable
    /**
     * Retrieve the user that sent this interaction.
     * @return the user that sent this interaction
     */
    public Link getSentBy()
    {
        return this.sent_by;
    }
    
    // email links
    @Nullable
    /**
     * Retrieve the case splits associated with this interaction
     * @return the case splits associated with this interaction
     */
    public Link getSplits()
    {
        return this.splits;
    }
    
    @Nullable
    /**
     * Retrieve the attachments associated to this interaction.
     * @return the attachments associated to this interaction
     */
    public Link getAttachments()
    {
        return this.attachments;
    }

    // phone links
    @Nullable
    /**
     * Retrieve the user that entered this interaction.
     * @return the user that entered this interaction
     */
    public Link getEnteredBy()
    {
        return this.entered_by;
    }

    // chat links
    /**
     * Retrieve the user who sent the chat message.
     * @return The user who sent the chat message.
     */
    public Link getCreatedBy()
    {
        return created_by;
    }

    // twitter links
    /**
     * Retrieve the twitter account that created this tweet.
     * @return the twitter account that created this tweet
     */
    public Link getTwitterAccount()
    {
        return twitter_account;
    }
    
    /**
     * Retrieve the twitter account that favorited this tweet.
     * @return the twitter account that favorited this tweet
     */
    public Link getFavoritedByTwitterAccount()
    {
        return favorited_by_twitter_account;
    }
    
    /**
     * Retrieve who re-tweeted this tweet.
     * @return who re-tweeted this tweet
     */
    public Link getRetweetedBy()
    {
        return retweeted_by;
    }
    
    /**
     * Retrieve who favorited this tweet.
     * @return who favorited this tweet
     */
    public Link getFavoritedBy()
    {
        return favorited_by;
    }
    
    // community question links
    /**
     * Retrieve topics for this question.
     * @return topics for this question
     */
    public Link getTopic()
    {
        return topic;
    }

    /**
     * Retrieve link to best answer to this question.
     * @return link to best answer to this question
     */
    public Link getBestAnswer()
    {
        return best_answer;
    }

    /**
     * Retrieve user who disallowed answers.
     * @return user who disallowed answers
     */
    public Link getAnswersDisallowedBy()
    {
        return answers_disallowed_by;
    }
}
