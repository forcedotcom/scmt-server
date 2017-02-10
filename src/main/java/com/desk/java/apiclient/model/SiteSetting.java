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

import org.jetbrains.annotations.NotNull;

public class SiteSetting implements Serializable
{

    private static final long serialVersionUID = -7534395644812857992L;

    private String name;
    private Object value;

    private Links _links;

    public String getName()
    {
        return (this.name == null) ? "" : this.name;
    }

    public void setName(String n)
    {
        this.name = n;
    }

    public Object getValue()
    {
        return (this.value == null) ? null : this.value;
    }

    public void setValue(Object v)
    {
        this.value = v;
    }
    
    public String getValueAsString()
    {
        return (this.value == null) ? null : this.value.toString();
    }
    
    public Integer getValueAsInteger()
    {
        return (this.value == null) ? null : (Integer) this.value;
    }
    
    public Boolean getValueAsBoolean()
    {
        return (this.value == null) ? null : (Boolean) this.value;
    }

    @NotNull
    public Links getLinks()
    {
        return _links == null ? _links = new Links() : _links;
    }

    public void setLinks(Links l)
    {
        this._links = l;
    }

    @NotNull
    public Link getSelfLink()
    {
        return getLinks().getSelf();
    }

    @NotNull
    public String getSelfLinkUrl()
    {
        return getLinks().getSelfUrl();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SiteSetting that = (SiteSetting) o;

        return this.name == that.name;
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }
}
