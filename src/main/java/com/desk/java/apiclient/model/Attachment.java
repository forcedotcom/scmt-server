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

import java.io.Serializable;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

public class Attachment implements Serializable {
    
    private static final long serialVersionUID = 2743899905456336126L;

    public static final int NO_ID = 0;
    
    private String fileName;
    private String contentType;
    private int size;
    private String url;
    private AttachmentLinks _links;

    /**
     * default constructor
     */
    public Attachment() {
        // nothing happening here
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fn) {
        this.fileName = fn;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String ct) {
        this.contentType = ct;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int s) {
        this.size = s;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String u) {
        this.url = u;
    }

    public String getFileExtension() {
        String fn = getFileName();
        int start = fn.lastIndexOf(".");

        if (start >= 0) {
            int end = fn.length();
            String ext = fn.substring(start + 1, end);
            return ext.toUpperCase(Locale.ROOT);
        } else {
            return "";
        }
    }
    
    @NotNull
    public AttachmentLinks getLinks() {
        return _links == null ? _links = new AttachmentLinks() : _links;
    }

    public void setLinks(AttachmentLinks l) {
        this._links = l;
    }

    /**
     * Gets the id of the case or {@link #NO_ID} if there is no case
     * @return the id or {@link #NO_ID}
     */
    public int getCaseId()
    {
        return (getLinks().getCaseLink() == null ? NO_ID : getLinks().getCaseLink().getLinkId());
    }

    /**
     * Gets the id of the agent or {@link #NO_ID} if there is no agent
     * @return the id or {@link #NO_ID}
     */
    public int getUserId()
    {
        return (getLinks().getUser() == null ? NO_ID : getLinks().getUser().getLinkId());
    }

    /**
     * Gets the id of the article or {@link #NO_ID} if there is no article
     * @return the id or {@link #NO_ID}
     */
    public int getArticleId()
    {
        return (getLinks().getArticle() == null ? NO_ID : getLinks().getArticle().getLinkId());
    }

}
