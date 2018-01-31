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
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 752919141528370641L;

    private long id;
    private String name;
    private String publicName;
    private String email;
    private String level;
    private String avatar;
    private UserLinks _links;
    private boolean deleted;

    public long getId() {
        return id;
    }

    public void setId(long i) {
        this.id = i;
    }

    public String getName() {
        return (name == null) ? "" : name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String pn) {
        this.publicName = pn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        this.email = e;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String l) {
        this.level = l;
    }

    public String getAvatar() {
        return (avatar == null) ? "" : avatar;
    }

    public void setAvatar(String a) {
        this.avatar = a;
    }

    @NotNull
    public UserLinks getLinks() {
        return _links == null ? _links = new UserLinks() : _links;
    }

    public void setLinks(UserLinks l) {
        this._links = l;
    }

    @Nullable
    public String getFiltersUrl() {
        if (getLinks().getFilters() == null) {
            return null;
        }
        return getLinks().getFilters().getUrl();
    }

    @NotNull
    public Link getSelfLink() {
        return getLinks().getSelf();
    }

    public String getSelfLinkUrl() {
        return getSelfLink().getUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        return id == that.id;

    }
    
    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean b) {
        this.deleted = b;
    }

}
