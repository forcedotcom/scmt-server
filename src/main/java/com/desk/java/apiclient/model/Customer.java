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


import com.desk.java.apiclient.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Customer implements Serializable {

    private static final long serialVersionUID = 6378489591680023162L;

    private long id;
    private String firstName;
    private String lastName;
    private String avatar;
    private String title;
    private String language;
    private String background;
    private CustomerContact[] emails;
    private CustomerContact[] addresses;
    private CustomerContact[] phoneNumbers;
    private CustomerLinks _links;
    private HashMap<String, String> customFields;
    private CustomerEmbedded _embedded;
    private String external_id;
    private String created_at;
    private String updated_at;
    private String recordTypeId;

    public static Customer create(String firstName, String lastName, String email,
                                         String emailType, String phone, String phoneType) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        if (!StringUtils.isEmpty(email)) {
            customer.setEmails(new CustomerContact(emailType, email));
        }

        if (!StringUtils.isEmpty(phone)) {
            customer.setPhoneNumbers(new CustomerContact(phoneType, phone));
        }

        return customer;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fn) {
        this.firstName = fn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String ln) {
        this.lastName = ln;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String l) {
        this.language = l;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String b) {
        this.background = b;
    }

    @NotNull
    public CustomerContact[] getEmails() {
        return emails == null ? new CustomerContact[0] : emails;
    }

    public void setEmails(CustomerContact... e) {
        this.emails = e;
    }

    @NotNull
    public CustomerContact[] getAddresses() {
        return addresses == null ? new CustomerContact[0] : addresses;
    }

    public void setAddresses(CustomerContact[] a) {
        this.addresses = a;
    }

    @NotNull
    public CustomerContact[] getPhoneNumbers() {
        return phoneNumbers == null ? new CustomerContact[0] : phoneNumbers;
    }

    public void setPhoneNumbers(CustomerContact... p) {
        this.phoneNumbers = p;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String a) {
        this.avatar = a;
    }

    public CustomerLinks getLinks() {
        return _links == null ? _links = new CustomerLinks() : _links;
    }

    public void setLinks(CustomerLinks l) {
        this._links = l;
    }

    @NotNull
    public Map<String, String> getCustomFields() {
        return customFields == null ? Collections.<String, String> emptyMap() : customFields;
    }

    public void setCustomFields(HashMap<String, String> cf) {
        this.customFields = cf;
    }

    @NotNull
    public CustomerEmbedded getEmbedded() {
        return _embedded == null ? _embedded = new CustomerEmbedded() : _embedded;
    }

    public void setEmbedded(CustomerEmbedded e) {
        this._embedded = e;
    }

    /**
     * Gets the first email
     * @return the first email or null if no email
     */
    @Nullable
    public String getFirstEmail() {
        if (getEmails() == null
                || getEmails().length == 0) {
            return null;
        }
        return getEmails()[0].getValue();
    }

    /**
     * Gets the first phone number
     * @return the first phone number or null if no phone number exists
     */
    @Nullable
    public String getFirstPhone() {
        if (getPhoneNumbers() == null
                || getPhoneNumbers().length == 0) {
            return null;
        }
        return getPhoneNumbers()[0].getValue();
    }

    /**
     * Gets the handle of the first twitter user
     * @return the handle of the first twitter user or null if no twitter user
     */
    @Nullable
    public String getFirstTwitterHandle() {
        if (getTwitterUser() == null) {
            return null;
        }
        return getTwitterUser().getHandle();
    }

    public boolean isValidCustomerForCaseType(CaseType caseType) {

        if (CaseType.EMAIL.equals(caseType)) {
            if (StringUtils.isEmpty(getFirstEmail())) {
                return false;
            }
        } else if (CaseType.TWITTER.equals(caseType) && StringUtils.isEmpty(getFirstTwitterHandle())) {
            return false;
        }

        return true;
    }

    /**
     * Gets the embedded facebook user
     * @return the embedded facebook user or null if there is no embedded facebook user
     */
    @Nullable
    public FacebookUser getFacebookUser() {
        return getEmbedded().getFacebookUser();
    }

    /**
     * Gets the embedded twitter user
     * @return the embedded twitter user or null if there is no embedded twitter user
     */
    @Nullable
    public TwitterUser getTwitterUser() {
        return getEmbedded().getTwitterUser();
    }

    /**
     * Gets the self link
     * @return the self link
     */
    @NotNull
    public Link getSelfLink() {
        return getLinks().getSelf();
    }

    /**
     * Get the company link or null if there is no company
     * @return the company link or null
     */
    @Nullable
    public Link getCompanyLink() {
        return getLinks().getCompany();
    }
    
    public int getCompanyId()
    {
        return (getCompanyLink() == null ? 0 : getCompanyLink().getLinkId());
    }
    
    public String getExternalId() {
        return external_id;
    }

    public void setExternalId(String a) {
        this.external_id= a;
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
    
    public String getRecordTypeId() {
        return recordTypeId;
    }

    public void setRecordTypeId(String n) {
        this.recordTypeId = n;
    }
}
