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
import java.util.HashMap;

public class Company implements Serializable {

    private static final long serialVersionUID = 7792122326436126562L;

    private int id;
    private String name;
    private String[] domains;
    private CompanyLinks _links;
    private HashMap<String, String> customFields;
    private String created_at;
    private String updated_at;
    private String external_id;
    private String recordTypeId;

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    @NotNull
    public String[] getDomains() {
        return domains == null ? new String[0] : domains;
    }

    public void setDomains(String[] d) {
        this.domains = d;
    }

    @NotNull
    public CompanyLinks getLinks() {
        return _links == null ? _links = new CompanyLinks() : _links;
    }

    public void setLinks(CompanyLinks l) {
        this._links = l;
    }

    @NotNull
    public HashMap<String, String> getCustomFields() {
        return customFields == null ? new HashMap<String, String>() : customFields;
    }

    public void setCustomFields(HashMap<String, String> cf) {
        this.customFields = cf;
    }

    public int getId() {
        return id;
    }

    public String getDomainsForDisplay() {
        StringBuilder domainsStr = new StringBuilder();
        String[] domains;
        if ((domains = getDomains()) != null) {
            for (String domain : domains) {
                domainsStr.append(domain + " ");
            }
        }

        return domainsStr.toString();
    }
    
    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String n) {
        this.created_at = n;
    }
    
    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(String n) {
        this.updated_at = n;
    }
    
    public String getExternalId() {
        return external_id;
    }

    public void setExternalId(String n) {
        this.external_id = n;
    }
    
    public String getRecordTypeId() {
        return recordTypeId;
    }

    public void setRecordTypeId(String n) {
        this.recordTypeId = n;
    }
}