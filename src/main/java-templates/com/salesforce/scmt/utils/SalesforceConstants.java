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

package com.salesforce.scmt.utils;

import java.util.regex.Pattern;

public final class SalesforceConstants
{
    // ReadOnly - used to test retrieval from src, won't write to SFDC
    public static final boolean READ_ONLY = false;

    // Control if all should be retrieve - when false, only pull one page for req. obj
    public static final boolean RETRIEVE_ALL = true;

    public static final int BULK_MAX_SIZE = 10000;
    public static final int API_MAX_SIZE = 200;
    public static final int SOQL_MAX_SIZE = 2000;
    public static final int LONG_TEXT_MAX = 131072;
    public static final int MED_TEXT_MAX = 4000;
    public static final int EMAIL_BODY_LENGTH = 32000;
    public static final int MAX_SIZE_REQUEST = 52428800;
    public static final String MIN_ID = "000000000000000";
    public static final String QueueUnassigned = "Unassigned";
    public static final int JOB_LIFE = 14400000; //12 hours = 4.32e7 milliseconds 43200000


    public static final String BLANK_STR = "(blank)";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String PERMSET_NAME = "SCMT_Audit";
    public static final String PERMSET_LABEL = "SCMT Audit";

    // declare the email regex pattern
    // https://help.salesforce.com/apex/HTViewSolution?id=000170904&language=en_US
//    public static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9_\\-\\.]+)@((\\[a-z]{1,3}\\.[a-z]{1,3}\\.[a-z]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})");


    public static final String CF_SUFFIX = "__c";
    public static final String PKG_PREFIX = "${package.namespace}";

    public static final String OBJ_USER = "User";
    public static final String OBJ_ACCOUNT = "Account";
    public static final String OBJ_CONTACT = "Contact";
    public static final String OBJ_CASE = "Case";
    public static final String OBJ_TOPIC = "Case";
    public static final String OBJ_EMAIL_MESSAGE = "EmailMessage";
    public static final String OBJ_CASE_COMMENT = "CaseComment";
    public static final String OBJ_ATTACHMENT = "Attachment";
    public static final String OBJ_FEED_ITEM = "FeedItem";
    public static final String OBJ_CHAT = "LiveChatTranscript";
    public static final String OBJ_CHAT_VISITOR = "LiveChatVisitor";
    public static final String OBJ_GROUP_MEMBER = "GroupMember";
    public static final String OBJ_ARTICLE = "Desk_com_Article__kav";

    // custom objects from SCMT package
    public static final String OBJ_DESK_MIGRATION = PKG_PREFIX + "Desk_Migration" + CF_SUFFIX;
    public static final String OBJ_DESK_MESSAGE = PKG_PREFIX + "Desk_Message" + CF_SUFFIX;
    public static final String OBJ_DESK_GROUP_MAP = PKG_PREFIX + "Desk_Group_Map" + CF_SUFFIX;

    /**
     * Private constructor for utility class.
     */
    private SalesforceConstants() {}

    public class Fields
    {
        // fields common to all Salesforce objects
        public static final String OwnerId = "OwnerId";
        public static final String CreatedDate = "CreatedDate";
        public static final String LastModifiedDate = "LastModifiedDate";
        public static final String Name = "Name";
        public static final String Id = "Id";
        public static final String RecordTypeId = "RecordTypeId";

        // Custom Fields
        public static final String DeskId = PKG_PREFIX + "DeskId" + CF_SUFFIX;
        public static final String DeskExternalId = PKG_PREFIX + "DeskExternalId" + CF_SUFFIX;
    }

    public class TopicFields
    {
        public static final String Name = "Name";
    }

    public class UserFields extends SalesforceConstants.Fields
    {
        // standard fields
        public static final String Alias = "Alias";
        public static final String Email = "Email";
        public static final String EmailEncodingKey = "EmailEncodingKey";
        public static final String LanguageLocaleKey = "LanguageLocaleKey";
        public static final String LastName = "LastName";
        public static final String FirstName = "FirstName";
        public static final String LocaleSidKey = "LocaleSidKey";
        public static final String TimeZoneSidKey = "TimeZoneSidKey";
        public static final String ProfileId = "ProfileId";
        public static final String RoleId = "RoleId";
        public static final String Username = "Username";
        public static final String IsActive = "IsActive";
        public static final String IsServiceCloud = "UserPermissionsSupportUser";
    }

    public class GroupFields
    {
        public static final String Id = "Id";
        public static final String DeveloperName = "DeveloperName";
        public static final String Name = "Name";
    }

    public class GroupMemberFields
    {
        public static final String GroupId = "GroupId";
        public static final String UserOrGroupId = "UserOrGroupId";
    }

    public class AccountFields extends SalesforceConstants.Fields
    {
        public static final String Name = "Name";
        public static final String Website = "Website";
        public static final String AccountNumber = "AccountNumber";

        // Custom Fields
        public static final String DeskCreatedAt = PKG_PREFIX + "DeskCreatedAt" + CF_SUFFIX;
        public static final String DeskUpdatedAt = PKG_PREFIX + "DeskUpdatedAt" + CF_SUFFIX;
        public static final String Domains = PKG_PREFIX + "Domains" + CF_SUFFIX;
    }

    public class ContactFields extends SalesforceConstants.Fields
    {
        public static final String FirstName = "FirstName";
        public static final String LastName = "LastName";
        public static final String Description = "Description";
        // public static final String Language = "Language" + CF_SUFFIX;
        public static final String Title = "Title";
        public static final String Email = "Email";
        public static final String Phone = "Phone";
        public static final String HomePhone = "HomePhone";
        public static final String MobilePhone = "MobilePhone";
        public static final String OtherPhone = "OtherPhone";
        public static final String Account = "Account";
        public static final String HomeAddress = PKG_PREFIX + "HomeAddress" + CF_SUFFIX;
        public static final String WorkAddress = PKG_PREFIX + "WorkAddress" + CF_SUFFIX;

        // Custom Fields
        public static final String DeskCreatedAt = PKG_PREFIX + "DeskCreatedAt" + CF_SUFFIX;
        public static final String DeskUpdatedAt = PKG_PREFIX + "DeskUpdatedAt" + CF_SUFFIX;
    }

    public class CaseFields extends SalesforceConstants.Fields
    {
        // Standard Fields
        public static final String Subject = "Subject";
        public static final String Description = "Description";
        public static final String ClosedDate = "ClosedDate";

        public static final String Priority = "Priority";
        public static final String PriorityHigh = "High";
        public static final String PriorityMedium = "Medium";
        public static final String PriorityLow = "Low";

        public static final String Status = "Status";
        public static final String StatusNew = "New";
        public static final String StatusClosed = "Closed";
        public static final String StatusWorking = "Working";
        public static final String StatusPending = "Pending";
        public static final String StatusResolved = "Resolved";
        public statis final String StatusOpen = "Open";

        public static final String Origin = "Origin";
        public static final String OriginEmail = "Email";
        public static final String OriginPhone = "Phone";
        public static final String OriginWeb = "Web";
        public static final String OriginChat = "Chat";
        public static final String OriginTwitter = "Twitter";
        public static final String OriginFacebook = "Facebook";

        public static final String Owner = "Owner";
        public static final String Contact = "Contact";
        public static final String Account = "Account";

        // Custom Fields
        public static final String DeskExternalId = PKG_PREFIX + "DeskExternalId" + CF_SUFFIX;
        public static final String Language = PKG_PREFIX + "Language" + CF_SUFFIX;
        public static final String DeskChangedDate = PKG_PREFIX + "DeskChangedDate" + CF_SUFFIX;
        public static final String DeskActiveDate = PKG_PREFIX + "DeskActiveDate" + CF_SUFFIX;
        public static final String DeskReceivedDate = PKG_PREFIX + "DeskReceivedDate" + CF_SUFFIX;
        public static final String DeskFirstOpenedDate = PKG_PREFIX + "DeskFirstOpenedDate" + CF_SUFFIX;
        public static final String DeskOpenedDate = PKG_PREFIX + "DeskOpenedDate" + CF_SUFFIX;
        public static final String DeskFirstResolvedDate = PKG_PREFIX + "DeskFirstResolvedDate" + CF_SUFFIX;
        public static final String DeskEmailThreadId = PKG_PREFIX + "DeskEmailThreadId" + CF_SUFFIX;
        public static final String DeskLabels = PKG_PREFIX + "DeskLabels" + CF_SUFFIX;
        public static final String DeskCreatedAt = PKG_PREFIX + "DeskCreatedAt" + CF_SUFFIX;
        public static final String DeskUpdatedAt = PKG_PREFIX + "DeskUpdatedAt" + CF_SUFFIX;
        public static final String DeskAssignedGroup = PKG_PREFIX + "DeskAssignedGroup" + CF_SUFFIX;
        public static final String DeskResolvedAt = PKG_PREFIX + "DeskResolvedAt" + CF_SUFFIX;
    }

    public class EmailMessageFields
    {
        public static final String FromAddress = "FromAddress";
        public static final String FromName = "FromName";
        public static final String ToAddress = "ToAddress";
        public static final String CcAddress = "CcAddress";
        public static final String BccAddress = "BccAddress";
        public static final String CreatedDate = "CreatedDate";
        public static final String CreatedBy = "CreatedBy";
        public static final String LastModifiedDate = "LastModifiedDate";
        public static final String MessageDate = "MessageDate";
        public static final String Subject = "Subject";
        public static final String TextBody = "TextBody";
        public static final String HtmlBody = "HtmlBody";
        public static final String Incoming = "Incoming";
        public static final String Status = "Status";
        public static final String Parent = "Parent";
        public static final String HasAttachment = "HasAttachment";
        public static final String IsExternallyVisible = "IsExternallyVisible";

        public static final int StatusNew = 0;
        public static final int StatusRead = 1;
        public static final int StatusReplied = 2;
        public static final int StatusSent = 3;
        public static final int StatusForwarded = 4;
        public static final int StatusDraft = 5;
    }

    public class CaseCommentFields
    {
        public static final String CreatedDate = "CreatedDate";
        public static final String LastModifiedDate = "LastModifiedDate";
        public static final String CreatedBy = "CreatedBy";
        public static final String CommentBody = "CommentBody";
        public static final String CreatorName = "CreatorName";
        public static final String IsPublished = "IsPublished";
        public static final String Parent = "Parent";
    }

    public class AttachmentFields
    {
        public static final String Name = "Name";
        public static final String IsPrivate = "IsPrivate";
        public static final String ContentType = "ContentType";
        public static final String Parent = "Parent";
        public static final String Owner = "Owner";
        public static final String Body = "Body";
    }

    public class FeedItemFields
    {
        public static final String CreatedDate = "CreatedDate";
        public static final String LastModifiedDate = "LastModifiedDate";

        /**
         * The content of the FeedItem. Required when Type is TextPost. Optional when Type is ContentPost or LinkPost.
         * This field is the message that appears in the feed.
         */
        public static final String Body = "Body";

        /**
         * Indicates whether the feed item Body contains rich text. Set IsRichText to true if you post a rich text feed
         * item via the SOAP API. Otherwise, the post is rendered as plain text.
         * Rich text supports the following HTML tags: <p>, <b>, <i>, <u>, <s>, <ul>, <ol>, <li>, <img>
         * Though the <br> tag isn’t supported, you can use <p>&nbsp;</p> to create lines.
         * The <img> tag is accessible only via the API and must reference files in Salesforce similar to this example:
         * <img src="sfdc://069B0000000omjh"></img>
         */
        public static final String IsRichText = "IsRichText";

        /**
         * The type of FeedItem. Except for ContentPost, LinkPost, and TextPost, don’t create FeedItem types directly
         * from the API.
         * FacebookPost—generated when a Facebook post is created from a case. Deprecated.
         * SocialPost—generated when a social post is created from a case.
         */
        public static final String Type = "Type";

        /**
         * ID of the object type to which the FeedItem object is related. For example, set this field to a UserId to
         * post to someone’s profile feed, or an AccountId to post to a specific account.
         */
        public static final String Parent = "Parent";
    }

    public class ChatFields
    {
        public static final String Body = "Body";
        public static final String StartTime = "StartTime";
        public static final String RequestTime = "RequestTime";
        public static final String EndTime = "EndTime";
        public static final String ChatKey = "ChatKey";
        public static final String OperatorMessageCount = "OperatorMessageCount";
        public static final String VisitorMessageCount = "VisitorMessageCount";

        // status picklist values
        public static final String Status = "Status";
        public static final String StatusCompleted = "Completed";

        public static final String Contact = "Contact";
        public static final String Case = "Case";
        public static final String Owner = "Owner";
        public static final String LiveChatVisitor = "LiveChatVisitor";
        public static final String Parent = "Parent";

        public static final String ChatTranscriptAgent = "Agent";
        public static final String ChatTranscriptCustomer = "Customer";
    }

    public class ChatVisitorFields
    {
        public static final String Name = "Name";
    }

    // fields for custom objects from SCMT package
    public class DeskMigrationFields
    {
        public static final String ID = "Id";
        public static final String RecordsMigrated = PKG_PREFIX + "RecordsMigrated" + CF_SUFFIX;
        public static final String RecordsFailed = PKG_PREFIX + "RecordsFailed" + CF_SUFFIX;
        public static final String RecordsTotal = PKG_PREFIX + "RecordsTotal" + CF_SUFFIX;
        public static final String Status = PKG_PREFIX + "Status" + CF_SUFFIX;
        public static final String EndDate = PKG_PREFIX + "EndDate" + CF_SUFFIX;
        public static final String Stage = PKG_PREFIX + "Stage" + CF_SUFFIX;
        public static final String Log = PKG_PREFIX + "Log" + CF_SUFFIX;
        public static final String ResumePoint = PKG_PREFIX + "RestartPoint" + CF_SUFFIX;
        public static final String ResultLink = PKG_PREFIX + "ResultLink" + CF_SUFFIX;
        public static final String Object = PKG_PREFIX + "Object" + CF_SUFFIX;
        public static final String JobId = PKG_PREFIX + "JobId" + CF_SUFFIX;

        // status picklist values
        public static final String StatusNew = "New";
        public static final String StatusQueued = "Queued";
        public static final String StatusRunning = "Running";
        public static final String StatusComplete = "Complete";
        public static final String StatusFailed = "Failed";

        // object piclickst values
        public static final String ObjectUser = "User";
        public static final String ObjectGroupMember = "Group Member";
        public static final String ObjectAccount = "Account";
        public static final String ObjectContact = "Contact";
        public static final String ObjectCase = "Case";
        public static final String ObjectNote = "Note";
        public static final String ObjectInteraction = "Interaction";
        public static final String ObjectAttachment = "Attachment";
        public static final String ObjectArticle = "Article";
    }

    public class DeskMessageFields
    {
        public static final String Id = "Id";
        public static final String Name = "Name";
        public static final String Data = PKG_PREFIX + "Data" + CF_SUFFIX;
        public static final String Error = PKG_PREFIX + "Error" + CF_SUFFIX;
        public static final String Status = PKG_PREFIX + "Status" + CF_SUFFIX;

        // status picklist values
        public static final String StatusNew = "New";
        public static final String StatusConverted = "Converted";
        public static final String StatusFailed = "Failed";
    }
}
