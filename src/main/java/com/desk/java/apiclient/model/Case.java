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
import java.util.Date;
import java.util.HashMap;

public class Case implements Serializable {

    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 10;

    public static final long NO_ID = 0;

    private static final long serialVersionUID = 8501351454459361905L;

    private long id;
    private String external_id;
    private String subject;
    private String priority;
    private String description;
    private String name;
    private CaseType type;
    private String blurb;
    private CaseStatus status;
    private String[] labels;
    private long[] labelIds;
    private LabelAction labelAction;
    private String language;
    private HashMap<String, String> customFields;
    private CaseEmbedded _embedded;
    private Date createdAt;
    private Date updatedAt;
    private Date changed_at;
    private Date active_at;
    private Date received_at;
    private Date first_opened_at;
    private Date opened_at;
    private Date first_resolved_at;
    private Date resolved_at;
    private CaseLinks _links;
    private Date lockedUntil;
    private Message message;
    private Message reply;
    private int active_attachments_count;
    private String recordTypeId;

    /**
     * default constructor
     */
    public Case() {
        // nothing happening here
    }

    public Case(HashMap<String, String> customFields, String cfName, String cfValue) {
        this.customFields = customFields;
        this.customFields.put(cfName, cfValue);
    }

    public static Case withAssignedUser(User assignedUser) {
        Case aCase = new Case();
        aCase.setLinks(CaseLinks.withAssignedUserLink(assignedUser.getSelfLink()));
        return aCase;
    }

    public static Case withAssignedGroup(Group assignedGroup) {
        Case aCase = new Case();
        aCase.setLinks(CaseLinks.withAssignedGroupLink(assignedGroup.getSelfLink()));
        return aCase;
    }

    public static Case withAssignedGroupUnassignedUser(Group assignedGroup) {
        Case aCase = new Case();
        aCase.setLinks(CaseLinks.withAssignedGroupLinkUnassignedUser(assignedGroup.getSelfLink()));
        return aCase;
    }

    public long getId() {
        return id;
    }

    public void setId(long i) {
        this.id = i;
    }
    
    public String getExternalId() {
        return external_id;
    }

    public void setExternalId(String s) {
        this.external_id = s;
    }

    public String getCustomerName() {
        return name;
    }

    public void setCustomerName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String s) {
        this.subject = s;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String p) {
        this.priority = p;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String b) {
        this.blurb = b;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus s) {
        this.status = s;
    }

    public CaseType getType() {
        return type;
    }

    public void setType(CaseType t) {
        this.type = t;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public Date getChangedAt()
    {
        return changed_at;
    }
    
    public Date getActiveAt()
    {
        return active_at;
    }
    
    public Date getReceivedAt()
    {
        return received_at;
    }
    
    public Date getFirstOpenedAt()
    {
        return first_opened_at;
    }
    
    public Date getOpenedAt()
    {
        return opened_at;
    }
    
    public Date getFirstResolvedAt()
    {
        return first_resolved_at;
    }
    
    public Date getResolvedAt()
    {
        return resolved_at;
    }
    
    public Date getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Date lu) {
        this.lockedUntil = lu;
    }

    @NotNull
    public String[] getLabels() {
        return labels == null ? new String[0] : labels;
    }

    public void setLabels(String[] lbls) {
        this.labels = lbls;
    }

    @NotNull
    public long[] getLabelIds() {
        return labelIds == null ? new long[0] : labelIds;
    }

    public void setLabelIds(long[] ids) {
        this.labelIds = ids;
    }

    public LabelAction getLabelAction() {
        return labelAction;
    }

    public void setLabelAction(LabelAction la) {
        this.labelAction = la;
    }
    
    public String getLanguage()
    {
        return language;
    }
    
    public void setLanguage(String s)
    {
        this.language = s;
    }

    @Nullable
    public String getAgentName() {
        User user = getAssignedUser();
        return user == null ? null : user.getName();
    }

    @Nullable
    public String getGroupName() {
        Group group = getAssignedGroup();
        return group == null ? null : group.getName();
    }

    public HashMap<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(HashMap<String, String> cf) {
        this.customFields = cf;
    }

    @NotNull
    public CaseEmbedded getEmbedded() {
        return _embedded == null ? _embedded = new CaseEmbedded() : _embedded;
    }

    public void setEmbedded(CaseEmbedded e) {
        this._embedded = e;
    }

    @NotNull
    public CaseLinks getLinks() {
        return _links == null ? _links = new CaseLinks() : _links;
    }

    public void setLinks(CaseLinks l) {
        this._links = l;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message m) {
        this.message = m;
    }

    public Message getReply() {
        return this.reply;
    }

    public void setReply(Message r) {
        this.reply = r;
    }

    public void setCustomField(String name, String value) {
        getCustomFields().put(name, value);
    }

    @NotNull
    public String getSelfLink() {
        return getLinks().getSelf().getUrl();
    }

    /**
     * Gets the message link url
     * @return the message link url or null
     */
    @Nullable
    public String getMessageLinkUrl() {
        if (getMessageLink() == null) {
            return null;
        }
        return getMessageLink().getUrl();
    }

    /**
     * Gets the message link
     * @return the message link or null
     */
    @Nullable
    public Link getMessageLink() {
        return getLinks().getMessage();
    }

    /**
     * Gets the attachments link url
     * @return the attachments link url or null
     */
    @Nullable
    public String getAttachmentsLinkUrl() {
        if (getAttachmentsLink() == null) {
            return null;
        }
        return getAttachmentsLink().getUrl();
    }

    /**
     * Gets the attachments link
     * @return the attachments link or null
     */
    @Nullable
    public Link getAttachmentsLink() {
        return getLinks().getAttachments();
    }

    /**
     * Gets the embedded message
     * @return the embedded message or null
     */
    @Nullable
    public Message getEmbeddedMessage() {
        return getEmbedded().getMessage();
    }

    /**
     * Gets the embedded draft
     * @return the embedded draft or null
     */
    @Nullable
    public Message getEmbeddedDraft() {
        return getEmbedded().getDraft();
    }

    public boolean isQna() {
        return CaseType.QNA.equals(type);
    }

    public boolean isEmail() {
        return CaseType.EMAIL.equals(type);
    }

    public boolean areAnswersDisallowed() {
        if (getEmbeddedMessage() == null) {
            return false;
        }
        return (Boolean.parseBoolean(getEmbeddedMessage().getAreAnswersDisallowed()));
    }

    /**
     * Returns the link to the assigned user
     * @return the link or an empty string if unassigned
     */
    @Nullable
    public String getAssignedUserLinkUrl() {
        if (getAssignedUserLink() == null) {
            return null;
        }
        return getAssignedUserLink().getUrl();
    }

    /**
     * Returns the link to the assigned user
     * @return the link or null
     */
    @Nullable
    public Link getAssignedUserLink() {
        return getLinks().getAssignedUser();
    }

    /**
     * Set the link of the assigned user
     * @param assignedUser the assigned user link
     */
    public void setAssignedUserLink(@Nullable Link assignedUser) {
        getLinks().setAssignedUser(assignedUser);
    }

    /**
     * Returns the user id of the assigned user
     * @return the user id if assigned, {@link #NO_ID} if unassigned
     */
    public long getAssignedUserId() {
        if (getAssignedUserLink() == null) {
            return NO_ID;
        }
        return getAssignedUserLink().getLinkId();
    }

    /**
     * Returns the url to the assigned group
     * @return the url or an empty string if unassigned
     */
    @Nullable
    public String getAssignedGroupLinkUrl() {
        if (getAssignedGroupLink() == null) {
            return null;
        }
        return getAssignedGroupLink().getUrl();
    }

    /**
     * Returns the link to the assigned group
     * @return the link or an empty link object
     */
    @Nullable
    public Link getAssignedGroupLink() {
        return getLinks().getAssignedGroup();
    }

    /**
     * Set the link of hte assigned user
     * @param assignedGroup the assigned user link
     */
    public void setAssignedGroupLink(@Nullable Link assignedGroup) {
        getLinks().setAssignedGroup(assignedGroup);
    }

    /**
     * Returns the user id of the assigned user
     * @return the user id if assigned, {@link #NO_ID} if unassigned
     */
    public long getAssignedGroupId() {
        if (getAssignedGroupLink() == null) {
            return NO_ID;
        }
        return getAssignedGroupLink().getLinkId();
    }

    /**
     * Gets the customer link or null
     * @return the customer link or null
     */
    @Nullable
    public Link getCustomerLink() {
        return getLinks().getCustomer();
    }

    /**
     * Gets the embedded customer id or {@link #NO_ID} if there is no embedded customer
     * @return the customer id or {@link #NO_ID}
     */
    public long getCustomerId() {
        if (getCustomerLink() == null) {
            return NO_ID;
        }
        return getCustomerLink().getLinkId();
    }

    /**
     * Gets the embedded customer or null if there is no embedded customer
     * @return the embedded customer or null
     */
    @Nullable
    public Customer getCustomer() {
        return getEmbedded().getCustomer();
    }

    /**
     * Gets the company id of the embedded customer or {@link #NO_ID} if there
     * is no embedded customer or company for the embedded customer
     * @return the company id or {@link #NO_ID}
     */
    public long getCustomerCompanyId() {
        long id = NO_ID;
        Customer customer = getCustomer();
        if (customer != null) {
            Link companyLink = customer.getCompanyLink();
            if (companyLink != null) {
                id = companyLink.getLinkId();
            }
        }
        return id;
    }

    /**
     * Gets the embedded assigned user or null if unassigned
     * @return the assigned user or null
     */
    @Nullable
    public User getAssignedUser() {
        return getEmbedded().getAssignedUser();
    }

    /**
     * Set the embedded assigned user
     * @param assignedUser the embedded assigned user
     */
    public void setAssignedUser(@Nullable User assignedUser) {
        getEmbedded().setAssignedUser(assignedUser);
    }

    /**
     * Gets the embedded assigned group or null if unassigned
     * @return the assigned group or null
     */
    @Nullable
    public Group getAssignedGroup() {
        return getEmbedded().getAssignedGroup();
    }

    /**
     * Set the embedded assigned group
     * @param assignedGroup the embedded assigned group
     */
    public void setAssignedGroup(@Nullable Group assignedGroup) {
        getEmbedded().setAssignedGroup(assignedGroup);
    }

    /**
     * Gets the url to the locked by user
     * @return the url or null if unassigned
     */
    @Nullable
    public String getLockedByLinkUrl() {
        if (getLockedByLink() == null) {
            return null;
        }
        return getLockedByLink().getUrl();
    }

    /**
     * Gets the link to the locked by user
     * @return the link to the locked by user or null
     */
    @Nullable
    public Link getLockedByLink() {
        return getLinks().getLockedBy();
    }

    /**
     * Set the link to the assigned user
     * @param lockedBy the link to the locked by user
     */
    public void setLockedByLink(@Nullable Link lockedBy) {
        getLinks().setLockedBy(lockedBy);
    }
    
    public int getActiveAttachmentsCount()
    {
        return active_attachments_count;
    }
    
    public void setActiveAttachmentsCount(int i)
    {
        active_attachments_count = i;
    }
    
    public String getRecordTypeId() {
        return recordTypeId;
    }

    public void setRecordTypeId(String n) {
        this.recordTypeId = n;
    }
}
