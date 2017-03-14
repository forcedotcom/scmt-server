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

import org.jetbrains.annotations.NotNull;

import com.salesforce.scmt.utils.Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interaction implements Serializable
{
    private static final long serialVersionUID = -6009666122580784815L;

    public static final int NO_ID = 0;
    
    // common fields
    private int id;
    private Date created_at;
    private Date updated_at;
    private String sent_at;
    private String erased_at;
    private String body;
    private String direction;
    private String status;
    private String from;
    private String to;
    private InteractionLinks _links;
    
    // email fields
//    private User hidden_by;
    private String hidden_at;
    private String cc;
    private String bcc;
    private String client_type;
    private String subject;
    private boolean hidden;

    // phone_call fields
    private String entered_at;
    
    // chat fields
    private String event_type;
    
    // tweet fields
    private String type;
    private String twitter_status_id;
    
    // facebook fields
    private String facebook_id;
    private String from_facebook_name;
    private boolean liked;
    
    // community question fields
    private String public_url;
    private String answers_disallowed_at;
    private int agent_answer_count;
    private int customer_answer_count;
    private boolean are_answers_disallowed;
    
    // community answer fields
    private int rating;
    private int rating_count;
    private int rating_score;
    private boolean is_best_answer;
    
    // common fields
    /**
     * Retrieve the integer identifier for this object.
     * @return Integer identifier for this object.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Retrieve the date/time of creation.
     * @return the date/time of creation.
     */
    public Date getCreatedAt()
    {
        return created_at;
    }

    /**
     * Set the date/time of creation.
     * @param time the date/time of creation.
     */
    public void setCreatedAt(Date time)
    {
        this.created_at = time;
    }

    /**
     * Retrieve the date/time last updated.
     * @return the date/time last updated.
     */
    public Date getUpdatedAt()
    {
        return updated_at;
    }

    /**
     * Set the date/time last updated.
     * @param time the date/time last updated.
     */
    public void setUpdatedAt(Date time)
    {
        this.updated_at = time;
    }

    /**
     * Retrieve the date/time that the interaction was sent.
     * @return the date/time that the interaction was sent.
     */
    public String getSentAt()
    {
        return sent_at;
    }

    /**
     * Retrieve the date/time at which an agent erased this interaction.
     * @return the date/time at which an agent erased this interaction.
     */
    public String getErasedAt()
    {
        return erased_at;
    }

    /**
     * Set the date/time at which an agent erased this interaction.
     * @param time the date/time at which an agent erased this interaction.
     */
    public void setErasedAt(String time)
    {
        this.erased_at = time;
    }

    /**
     * Retrieve the Message body.
     * @return Message body.
     */
    public String getBody()
    {
        return body;
    }

    /**
     * Set the Message body.
     * @param string Message body.
     */
    public void setBody(String string)
    {
        this.body = string;
    }

    /**
     * Retrieve the direction of the interaction (in/out).
     * @return the direction of the interaction (in/out).
     */
    public String getDirection()
    {
        return direction;
    }
    public void setDirection(String d) { this.direction = d; }

    /**
     * Retrieve the status of the interaction.
     * Email: received, pending, sent, or failed
     * Twitter: received, draft, pending, sent, or failed
     * Facebook: received, draft, pending, sent, or failed
     * 
     * @return the status of the interaction.
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Retrieve the from address of the interaction.
     * @return the from address of the interaction.
     */
    public String getFrom()
    {
        return from;
    }
    public void setFrom(String f) { this.from = f; }

    /**
     * Retrieve the to address of the interaction.
     * @return the to address of the interaction.
     */
    public String getTo()
    {
        return to;
    }

    @NotNull
    /**
     * Retrieve the Links associated with this interaction.
     * @return Links associated with this interaction.
     */
    public InteractionLinks getLinks()
    {
        return _links == null ? _links = new InteractionLinks() : _links;
    }
    
    /**
     * Set the Links associated with this interaction.
     * @param l Links associated with this interaction.
     */
    public void setLinks(InteractionLinks links)
    {
        this._links = links;
    }
    
    /**
     * Retrieve the id of the case this interaction is associated to.
     * @return the id of the case this interaction is associated to
     */
    public int getCaseId()
    {
        return (getLinks().getCaseLink() == null ? 0 : getLinks().getCaseLink().getLinkId());
    }

    /**
     * Retrieve the id of the agent who sent this interaction.
     * @return the id of the agent who sent this interaction
     */
    public int getSentById()
    {
        return (getLinks().getSentBy() == null ? 0 : getLinks().getSentBy().getLinkId());
    }

    /**
     * Retrieve the id of the agent who entered this interaction.
     * @return the id of the agent who entered this interaction
     */
    public int getEnteredById()
    {
        return (getLinks().getEnteredBy() == null ? 0 : getLinks().getEnteredBy().getLinkId());
    }

    /**
     * Retrieve the id of the agent who sent the chat message.
     * @return the id of the agent who sent the chat message
     */
    public int getCreatedById()
    {
        return (getLinks().getCreatedBy() == null ? 0 : getLinks().getCreatedBy().getLinkId());
    }


    // email fields
//    /**
//     * Retrieve the user who made this interaction private.
//     * @return the user who made this interaction private.
//     */
//    public User getHiddenBy()
//    {
//        return hidden_by;
//    }

    /**
     * Retrieve the date/time the interaction was hidden.
     * @return the date/time the interaction was hidden.
     */
    public String getHiddenAt()
    {
        return hidden_at;
    }

    /**
     * Retrieve the email's cc address(es).
     * @return the email's cc address(es).
     */
    public String getCc()
    {
        return cc;
    }

    /**
     * Retrieve the email's bcc address(es).
     * @return the email's bcc address(es).
     */
    public String getBcc()
    {
        return bcc;
    }

    /**
     * Retrieve the email's client name that originated the email (ie. 'desk_portal' and 'desk_widget' represent emails from the customer support center).
     * @return the email client name.
     */
    public String getClientType()
    {
        return client_type;
    }

    /**
     * Retrieve the subject of the email message.
     * @return the subject of the email message.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Retrieve the flag indicating if the interaction is hidden.
     * @return the flag indicating if the interaction is hidden.
     */
    public boolean getHidden()
    {
        return hidden;
    }

    // phone_call fields
    /**
     * Retrieve the date/time the phone call was entered.
     * @return the date/time the phone call was entered.
     */
    public String getEnteredAt()
    {
        return entered_at;
    }

    // chat fields
    /**
     * Retrieve the event type of the chat message.
     * @return the event type of the chat message
     */
    public String getEventType()
    {
        return event_type;
    }
    
    // tweet fields
    /**
     * Retrieve the type of the tweet (e.g. 'mention').
     * @return the type of the tweet (e.g. 'mention').
     */
    public String getType()
    {
        return type;
    }

    /**
     * Retrieve the twitter status id.
     * @return the twitter status id.
     */
    public String getTwitterStatusId()
    {
        return twitter_status_id;
    }
    
    // facebook fields
    /**
     * Retrieve the facebook message id.
     * @return the facebook message id.
     */
    public String getFacebookId()
    {
        return facebook_id;
    }
    
    /**
     * Retrieve the sender's facebook username.
     * @return the sender's facebook username.
     */
    public String getFromFacebookName()
    {
        return from_facebook_name;
    }
    
    /**
     * True if the message was liked.
     * @return flag indicating if the facebook message was liked.
     */
    public boolean getFacebookLiked()
    {
        return liked;
    }
    
    // community question fields
    /**
     * Retrieve the public URL for this community question.
     * @return the public URL for this community question
     */
    public String getPublicUrl()
    {
        return public_url;
    }
    
    /**
     * Retrieve the date/time when answers were disabled.
     * @return the date/time when answers were disabled
     */
    public String getAnswersDisallowedAt()
    {
        return answers_disallowed_at;
    }
    
    /**
     * Retrieve the agent answer count.
     * @return the agent answer count
     */
    public int getAgentAnswerCount()
    {
        return agent_answer_count;
    }
    
    /**
     * Retrieve the customer answer count.
     * @return the customer answer count
     */
    public int getCustomerAnswerCount()
    {
        return customer_answer_count;
    }
    
    /**
     * Flag indicating if answers are allowed on the community question.
     * @return Flag indicating if answers are allowed on the community question.
     */
    public boolean isAnswerDisallowed()
    {
        return are_answers_disallowed;
    }
    
    // community answer fields
    /**
     * Retrieve the rating of the community answer.
     * @return The rating of the community answer.
     */
    public int getRating()
    {
        return rating;
    }
    
    /**
     * Retrieve the rating count of the community answer.
     * @return The rating count of the community answer.
     */
    public int getRatingCount()
    {
        return rating_count;
    }
    
    /**
     * Retrieve the rating score of the community answer.
     * @return The rating score of the community answer.
     */
    public int getRatingScore()
    {
        return rating_score;
    }
    
    /**
     * Flag indicating if this is the best answer.
     * @return Flag indicating if this is the best answer.
     */
    public boolean isBestAnswer()
    {
        return is_best_answer;
    }
    

    // Helper code
    
    private static final Pattern EMAIL_WITH_NAME = Pattern.compile("(.*)<(.*)>");
    
    /**
     * Enum used to determine what the type of the interaction is.
     * @author rmccullough
     *
     */
    public enum InteractionType
    {
        UNKNOWN,
        EMAIL,
        PHONE_CALL,
        TWEET,
        CHAT_MESSAGE,
        FACEBOOK_POST,
        FACEBOOK_COMMENT,
        FACEBOOK_MESSAGE,
        COMMUNITY_QUESTION,
        COMMUNITY_ANSWER
    }
    
    /**
     * Retrieve the type of the interaction.
     * @return the type of the interaction
     */
    public InteractionType getInteractionType()
    {
        try
        {
            return (getLinks().getSelf() == null ? null :
                InteractionType.valueOf(getLinks().getSelf().getClassName().toUpperCase()));
        }
        catch (IllegalArgumentException e)
        {
            Utils.log(String.format("[ERROR] Interaction Type not supported! Type: [%s]",
                getLinks().getSelf().getClassName()));
        }
        return InteractionType.UNKNOWN;
    }

    /**
     * Returns the 'Name' portion of an email address (if present). E.g. returns "Ryan McCullough" if the from is
     * "Ryan McCullough <rmccullough@salesforce.com>". If name is not present in the email address, returns null.
     * @return the 'Name' portion of an email address (if present).
     */
    public String getFromName()
    {
        String fromName = null;
        // null check, also check if the address contains <>, indicating it has a name in it
        if (getFrom() == null || getFrom().isEmpty() ||
            !(getFrom().contains("<") && getFrom().contains(">")))
        {
            fromName = null;
        }
        else
        {
            Matcher mEmail = EMAIL_WITH_NAME.matcher(getFrom());
            if (mEmail.find())
            {
                // get the name from the email
                fromName = mEmail.group(1).trim();
            }
            else
            {
                fromName = null;
            }
        }
        return fromName;
    }
    
    /**
     * Returns the 'Address' portion of an email address when the email contains a name. E.g. returns
     * "rmccullough@salesforce.com" if the email address is "Ryan McCullough <rmccullough@salesforce.com>".
     * If name is not present in the email address, returns null.
     * @return the 'Name' portion of an email address (if present).
     */
    public String getFromAddress()
    {
        String fromAddress = null;
        if (getFrom() == null || getFrom().isEmpty())
        {
            fromAddress = null;
        }
        else
        {
            Matcher mEmail = EMAIL_WITH_NAME.matcher(getFrom());
            if (mEmail.find())
            {
                // get the address from the email
                fromAddress = mEmail.group(2);
            }
            else
            {
                fromAddress = getFrom();
            }
        }
        return fromAddress;
    }
    
    /**
     * Indicates whether the interaction was received (true) or sent (false).
     * @return
     */
    public boolean isIncoming()
    {
        return "in".equalsIgnoreCase(getDirection());
    }
}