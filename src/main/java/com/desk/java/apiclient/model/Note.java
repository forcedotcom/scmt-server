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

import java.io.Serializable;

public class Note implements Serializable
{
    private static final long serialVersionUID = -1281357273641660176L;

    public static final long NO_ID = 0;
    
    private long id;
    private String body;
    private String created_at;
    private String updated_at;
    private String erased_at;
    private NoteLinks _links;

    /**
     * Retrieve the integer identifier for this object.
     * @return Integer identifier for this object.
     */
    public long getId()
    {
        return id;
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
     * @param s Message body.
     */
    public void setBody(String s)
    {
        this.body = s;
    }

    /**
     * Retrieve the Time of creation.
     * @return Time of creation.
     */
    public String getCreatedAt()
    {
        return created_at;
    }

    /**
     * Set the Time of creation.
     * @param s Time of creation.
     */
    public void setCreatedAt(String s)
    {
        this.created_at = s;
    }

    /**
     * Retrieve the Time last updated.
     * @return Time last updated.
     */
    public String getUpdatedAt()
    {
        return updated_at;
    }

    /**
     * Set the Time last updated.
     * @param s Time last updated.
     */
    public void setUpdatedAt(String s)
    {
        this.updated_at = s;
    }

    /**
     * Retrieve the Time at which an agent erased this note.
     * @return Time at which an agent erased this note.
     */
    public String getErasedAt()
    {
        return erased_at;
    }

    /**
     * Set the Time at which an agent erased this note.
     * @param s Time at which an agent erased this note.
     */
    public void setErasedAt(String s)
    {
        this.erased_at = s;
    }

    @NotNull
    /**
     * Retrieve the Links associated with this note.
     * @return Links associated with this note.
     */
    public NoteLinks getLinks()
    {
        return _links == null ? _links = new NoteLinks() : _links;
    }
    
    /**
     * Set the Links associated with this note.
     * @param l Links associated with this note.
     */
    public void setLinks(NoteLinks l)
    {
        this._links = l;
    }
    
    /**
     * Retrieve the id of the case this note is associated to.
     * @return the id of the case this note is associated to
     */
    public long getCaseId()
    {
        return (getLinks().getCaseLink() == null ? 0 : getLinks().getCaseLink().getLinkId());
    }
}