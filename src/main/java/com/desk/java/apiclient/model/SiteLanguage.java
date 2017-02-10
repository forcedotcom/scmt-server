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

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

public class SiteLanguage implements Serializable{

    private static final long serialVersionUID = -1731897040461479539L;

    private String id;
    private String name;
    private boolean agent;
    @SerializedName("case")
    private boolean _case;
    private boolean customer;
    private boolean is_case_default;
    
    private Links _links;

    public String getId() {
        return id;
    }

    public void setId(String i) {
        this.id = i;
    }

    public String getName() {
        return (name == null) ? "" : name;
    }

    public void setName(String n) {
        this.name = n;
    }

    /**
     * whether or not the language is valid for Desk.com Agent localization
     * @return whether or not the language is valid for Desk.com Agent localization
     */
    public boolean isAgent() {
        return agent;
    }

    /**
     * whether or not the language is valid as case language
     * @return whether or not the language is valid as case language
     */
    public boolean isCase() {
        return _case;
    }

    /**
     * whether or not the language is valid as customer language
     * @return whether or not the language is valid as customer language
     */
    public boolean isCustomer() {
        return customer;
    }

    /**
     * whether or not the language is the default case language
     * @return whether or not the language is the default case language
     */
    public boolean isCaseDefault() {
        return is_case_default;
    }

    @NotNull
    public Links getLinks() {
        return _links == null ? _links = new Links() : _links;
    }

    public void setLinks(Links l) {
        this._links = l;
    }

    @NotNull
    public Link getSelfLink() {
        return getLinks().getSelf();
    }

    @NotNull
    public String getSelfLinkUrl() {
        return getLinks().getSelfUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteLanguage that = (SiteLanguage) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
