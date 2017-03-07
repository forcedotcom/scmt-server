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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.desk.java.apiclient.model.Article;
import com.desk.java.apiclient.model.Case;
import com.desk.java.apiclient.model.CaseStatus;
import com.desk.java.apiclient.model.CaseType;
import com.desk.java.apiclient.model.Company;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.CustomerContact;
import com.desk.java.apiclient.model.Interaction;
import com.desk.java.apiclient.model.Interaction.InteractionType;
import com.desk.java.apiclient.model.Label;
import com.desk.java.apiclient.model.Note;
import com.desk.java.apiclient.model.User;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.utils.SalesforceConstants.AccountFields;
import com.salesforce.scmt.utils.SalesforceConstants.CaseCommentFields;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.SalesforceConstants.ChatFields;
import com.salesforce.scmt.utils.SalesforceConstants.ContactFields;
import com.salesforce.scmt.utils.SalesforceConstants.EmailMessageFields;
import com.salesforce.scmt.utils.SalesforceConstants.FeedItemFields;
import com.salesforce.scmt.utils.SalesforceConstants.TopicFields;
import com.salesforce.scmt.utils.SalesforceConstants.UserFields;
import com.sforce.soap.partner.sobject.SObject;

public final class DeskJsonMapUtil
{
    /**
     * Private constructor for utility class.
     */
    private DeskJsonMapUtil() {}

    private static Map<String, String> deskCustomFieldsToSalesforceJsonMap(Map<String, String> deskCFs)
    {
        // declare the Salesforce custom fields map
        Map<String, String> sfCFs = new HashMap<>();

        // migrate custom fields
        if (deskCFs != null && !deskCFs.isEmpty())
        {
            // key is the field name
            for (String key : deskCFs.keySet())
            {
                // set the custom field
                sfCFs.put("Desk_" + key + "__c", deskCFs.get(key));
            }
        }

        return sfCFs;
    }

    public static Map<String, Object> deskUserToSalesforceJsonMap(User u)
    {
        // extract name pieces
        String[] namePieces = u.getName().split(" ", 1);

        // find the position of the '@' symbol
        int atIdx = u.getEmail().indexOf('@');

        // create the map
        Map<String, Object> userMap = new HashMap<>();

        userMap.put(UserFields.DeskId, u.getId());
        userMap.put(UserFields.Alias, u.getEmail().substring(0, (atIdx > 8 ? 8 : atIdx)));
        // mask email for test runs to not notify user on creation
        userMap.put(UserFields.Email, u.getEmail() + "." + Calendar.getInstance().getTimeInMillis());
        userMap.put(UserFields.EmailEncodingKey, "UTF-8");
        userMap.put(UserFields.LanguageLocaleKey, "en_US");
        userMap.put(UserFields.LocaleSidKey, "en_US");
        userMap.put(UserFields.TimeZoneSidKey, "America/Los_Angeles");

        // Names
        userMap.put(UserFields.LastName, (namePieces.length == 1 ? namePieces[0] : namePieces[1]));
        if (namePieces.length > 1)
        {
            userMap.put(UserFields.FirstName, namePieces[0]);
        }

        // Unique usernames for testing
        userMap.put("Username", u.getEmail() + u.getId()+"." + Calendar.getInstance().getTimeInMillis());

        // Create Active/Inactive
//        userMap.put(UserFields.IsActive, u.getDeleted());
        userMap.put(UserFields.IsActive, "false");

        //set service cloud user
        //TODO: Which Desk User field should be used to indicate service cloud license?
//        userMap.put(UserFields.IsServiceCloud,"true" );
        return userMap;
    }

    public static Map<String, Object> deskCompanyToSalesforceJsonMap(Company company, Map<String, String> config)
    {
        Map<String, Object> mapObj = new HashMap<>();
        mapObj.put(AccountFields.DeskId, company.getId());
        mapObj.put(AccountFields.Name, company.getName());
        // TODO: Bug in Desk.com API that if I specify I want this field, it only returns companies
        // with an 'external_id' set
        //mapObj.put(AccountFields.DeskExternalId, company.getExternalId());
        mapObj.put(AccountFields.DeskCreatedAt, company.getCreatedAt());
        mapObj.put(AccountFields.DeskUpdatedAt, company.getUpdatedAt());

        // set the website URL if set on desk
        if (company.getDomains().length > 0)
        {
            String joinedDomains = String.join(", ", company.getDomains());
            mapObj.put(AccountFields.Website, "http://" + company.getDomains()[0]);
            mapObj.put(AccountFields.Domains, joinedDomains);
        }

        mapObj.put(AccountFields.RecordTypeId, config.get("account_record_type_id"));
        // add the desk custom fields
        mapObj.putAll(deskCustomFieldsToSalesforceJsonMap(company.getCustomFields()));

        return mapObj;
    }

    public static Map<String, Object> deskLabelToSalesforceTopicJsonMap(Label label)
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // set the name
        mapObj.put(TopicFields.Name, label.getName());

        // return the map
        return mapObj;
    }

    public static Map<String, Object> deskCaseToSalesforceJsonMap(DeskUtil deskUtil, Case deskCase, Map<String, String> config) throws Exception
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        mapObj.put(CaseFields.DeskId, deskCase.getId());
        mapObj.put(CaseFields.DeskExternalId, deskCase.getExternalId());
        mapObj.put(CaseFields.Subject, deskCase.getSubject().length() > 99 ? deskCase.getSubject().substring(0, 98) : deskCase.getSubject());
        mapObj.put(CaseFields.Description, deskCase.getDescription());
        mapObj.put(CaseFields.RecordTypeId, config.get("case_record_type_id"));
        // Desk.com stores priority as a numeric value between 1-10, Salesforce provides High, Medium, and Low by
        // default. Desk.com will be mapped to Salesforce as follows:
        // 1-3 = Low, 4-6 = Medium, 7-10 = High
        mapObj.put(CaseFields.Priority, (Integer.valueOf(deskCase.getPriority()) < 4 ? CaseFields.PriorityLow :
            (Integer.valueOf(deskCase.getPriority()) < 7 ? CaseFields.PriorityMedium : CaseFields.PriorityHigh)));

        // Map status values from Desk.com to Salesforce
        mapObj.put(CaseFields.Status, (deskCase.getStatus() == CaseStatus.NEW ? CaseFields.StatusNew :
            (deskCase.getStatus() == CaseStatus.CLOSED ? CaseFields.StatusClosed :
            (deskCase.getStatus() == CaseStatus.OPEN ? CaseFields.StatusWorking :
            (deskCase.getStatus() == CaseStatus.PENDING ? CaseFields.StatusPending : CaseFields.StatusResolved)))));

        // map Desk.com case type to Salesforce case origin
        //ERROR: Json Deserialization failed on token 'Origin' and has left off in the middle of parsing a row. Will go to end of row to begin parsing the next row
        mapObj.put(CaseFields.Origin, (deskCase.getType() == CaseType.EMAIL ? CaseFields.OriginEmail :
            (deskCase.getType() == CaseType.PHONE ? CaseFields.OriginPhone :
            (deskCase.getType() == CaseType.QNA ? CaseFields.OriginWeb :
            (deskCase.getType() == CaseType.CHAT ? CaseFields.OriginChat :
            (deskCase.getType() == CaseType.TWITTER ? CaseFields.OriginTwitter : CaseFields.OriginFacebook))))));

        // set the case owner
        // Desk.com can have both a user and group assigned to a case.
        // If both are specified, we will use the user. If neither are specified, we will assign the case to the
        // 'Unassigned' queue that is part of the AppExchange package.
        if (deskCase.getAssignedUserId() != Case.NO_ID)
        {
            mapObj.put(CaseFields.Owner, getUserMap(deskCase.getAssignedUserId(), true));
        }
        else if (deskCase.getAssignedGroupId() != Case.NO_ID)
        {
            // assign to the group from desk
            mapObj.put(CaseFields.OwnerId, deskUtil.getQueueId(deskCase.getAssignedGroupId()));
        }
        else
        {
            // assign to 'Unassigned' queue
        	if(deskUtil.getSalesforceService().getQueues().containsKey(SalesforceConstants.QueueUnassigned))
        	{
        		mapObj.put(CaseFields.OwnerId,
        				deskUtil.getSalesforceService().getQueues().get(SalesforceConstants.QueueUnassigned));
        	}
        }

        // check if we can convert the assigned group id to group name
        if (deskUtil.getDeskGroupIdAndName().containsKey(deskCase.getAssignedGroupId()))
        {
            mapObj.put(CaseFields.DeskAssignedGroup,
                deskUtil.getDeskGroupIdAndName().get(deskCase.getAssignedGroupId()));
        }
        // if the group id is 0 it is not assigned to a group, so only put the id into the field if it does not resolve
        // to a name (previous if statement), and it is specified (group id != 0)
        else if (deskCase.getAssignedGroupId() != Case.NO_ID)
        {
            // if there is no mapping, just put the group id into the custom field
            // Utils.log("[WARN] No mapping for Desk Group Id [" + deskCase.getAssignedGroupId() + "] to Name!");
            mapObj.put(CaseFields.DeskAssignedGroup, deskCase.getAssignedGroupId());
        }

        // set the created, last modified, and closed date on the case
        mapObj.put(CaseFields.DeskCreatedAt, SalesforceUtil.sfdcDateTimeFormat(deskCase.getCreatedAt()));
        mapObj.put(CaseFields.DeskUpdatedAt, SalesforceUtil.sfdcDateTimeFormat(deskCase.getUpdatedAt()));

        // set the desk resolved at date/time
        mapObj.put(CaseFields.DeskResolvedAt, SalesforceUtil.sfdcDateTimeFormat(deskCase.getResolvedAt()));

        // map other desk.com fields to custom fields created on the case object for them
        mapObj.put(CaseFields.Language, deskCase.getLanguage());
        mapObj.put(CaseFields.DeskChangedDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getChangedAt()));
        mapObj.put(CaseFields.DeskActiveDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getActiveAt()));
        mapObj.put(CaseFields.DeskReceivedDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getReceivedAt()));
        mapObj.put(CaseFields.DeskFirstOpenedDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getFirstOpenedAt()));
        mapObj.put(CaseFields.DeskOpenedDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getOpenedAt()));
        mapObj.put(CaseFields.DeskFirstResolvedDate, SalesforceUtil.sfdcDateTimeFormat(deskCase.getFirstResolvedAt()));

        // associate the case with a contact
        if (deskCase.getCustomerId() != Case.NO_ID)
        {
            mapObj.put(CaseFields.Contact, getContactMap(deskCase.getCustomerId()));

            // associate the case with an account
            if (deskCase.getCustomerCompanyId() != Case.NO_ID)
            {
                mapObj.put(CaseFields.Account, getAccountMap(deskCase.getCustomerCompanyId()));
            }
        }

        // add the desk custom fields
        mapObj.putAll(deskCustomFieldsToSalesforceJsonMap(deskCase.getCustomFields()));

        // put the labels into the long text area custom field, separated by newline
        mapObj.put(CaseFields.DeskLabels, String.join("\n", deskCase.getLabels()));

        // TODO: Should we implement this here? We can do it with a trigger, but it is a bit of overhead on the import...
        // generate the Desk.com email thread id
        // EncodingUtil.convertToHex(Crypto.generateDigest('Sha1', Blob.valueOf('--assistly--' + DeskId + '--')));
//        mapObj.put(CaseFields.DeskEmailThreadId, "");

        return mapObj;
    }

    public static Map<String, Object> deskCustomerToSalesforceJsonMap(Customer customer, DeployResponse deployResponse, Map<String, String> config) throws Exception
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        mapObj.put(ContactFields.DeskId, customer.getId());
        mapObj.put(ContactFields.DeskCreatedAt, customer.getCreatedAt());
        mapObj.put(ContactFields.DeskUpdatedAt, customer.getUpdatedAt());
        mapObj.put(ContactFields.DeskExternalId, customer.getExternalId());
        if(config.containsKey("contact_record_type_id")) {
        	mapObj.put(ContactFields.RecordTypeId, config.get("contact_record_type_id"));
        }



        // set first name (and compensate for empty value)
        String firstName = (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty() ?
            SalesforceConstants.BLANK_STR : customer.getFirstName());

        // check for blank first name
        if (SalesforceConstants.BLANK_STR.equalsIgnoreCase(firstName))
        {
            deployResponse.addError("[WARN] Customer First Name is blank! Desk Id: [" + customer.getId() + "]");
        }

        // check for first name too long
        if (firstName.length() > 40)
        {
            String msg = String.format(
                "[WARN] Customer First Name too long and will be truncated! Desk Id: [%d] First Name: [%s]",
                customer.getId(), firstName);
            Utils.log(msg);
            deployResponse.addError(msg);
            firstName = firstName.substring(0, 40);
        }
        mapObj.put(ContactFields.FirstName, firstName);

        // set last name (and compensate for empty value)
        String lastName = (customer.getLastName() == null || customer.getLastName().trim().isEmpty() ?
            SalesforceConstants.BLANK_STR : customer.getLastName());

        // check for blank first name
        if (SalesforceConstants.BLANK_STR.equalsIgnoreCase(lastName))
        {
            deployResponse.addError("[WARN] Customer Last Name is blank! Desk Id: [" + customer.getId() + "]");
        }

        // check for last name too long
        if (lastName.length() > 80)
        {
            String msg = String.format(
                "[WARN] Customer Last Name too long and will be truncated! Desk Id: [%d] Last Name: [%s]",
                customer.getId(), lastName);
            Utils.log(msg);
            deployResponse.addError(msg);
            lastName = lastName.substring(0, 80);
        }
        mapObj.put(ContactFields.LastName, lastName);

        if (customer.getTitle() != null)
        {
            mapObj.put(ContactFields.Title, customer.getTitle());
        }

        if (customer.getBackground() != null)
        {
            mapObj.put(ContactFields.Description, customer.getBackground());
        }

        if (customer.getFirstEmail() != null && !customer.getFirstEmail().isEmpty())
        {
            String email = customer.getFirstEmail();


            // check email length
            if (email.length() > 80)
            {
                String msg = String.format(
                    "[WARN] Customer Email too long and will be truncated! Desk Id: [%d] Email: [%s]",
                    customer.getId(), email);
                Utils.log(msg);
                deployResponse.addError(msg);
                email = email.substring(0, 80);
            }

            // ensure email is valid
            Matcher m = SalesforceConstants.EMAIL_PATTERN.matcher(email);
            if (m.find())
            {
                // set the field on the email message object
                mapObj.put(ContactFields.Email, email);
            }
            else
            {
                String msg = String.format("[WARN] Customer Email is NOT valid! Desk Id: [%d] Email: [%s]",
                    customer.getId(), email);
                Utils.log(msg);
                deployResponse.addError(msg);
            }
        }


        // loop through the phone numbers and get the first of each type
        for (CustomerContact cc : customer.getPhoneNumbers())
        {
            if ("home".equalsIgnoreCase(cc.getType()) && mapObj.get(ContactFields.HomePhone) == null)
            {
                mapObj.put(ContactFields.HomePhone, cc.getValue());
            }
            else if ("work".equalsIgnoreCase(cc.getType()) && mapObj.get(ContactFields.Phone) == null)
            {
                mapObj.put(ContactFields.Phone, cc.getValue());
            }
            else if ("mobile".equalsIgnoreCase(cc.getType()) && mapObj.get(ContactFields.MobilePhone) == null)
            {
                mapObj.put(ContactFields.MobilePhone, cc.getValue());
            }
            else if ("other".equalsIgnoreCase(cc.getType()) && mapObj.get(ContactFields.OtherPhone) == null)
            {
                mapObj.put(ContactFields.OtherPhone, cc.getValue());
            }
        }

        // take the first work or home address and add it to the Contact record
        for (CustomerContact address : customer.getAddresses())
        {
            if ("work".equalsIgnoreCase(address.getType()) && mapObj.get(ContactFields.WorkAddress) == null)
            {
                mapObj.put(ContactFields.WorkAddress, address.getValue());
            }
            else if ("home".equalsIgnoreCase(address.getType()) && mapObj.get(ContactFields.HomeAddress) == null)
            {
                mapObj.put(ContactFields.HomeAddress, address.getValue());
            }
        }

        // associate the customer with an account
        if (customer.getCompanyLink() == null)
        {
            // TODO: do we need to handle this more gracefully?
            // String msg = "[WARN] Contact does not have an Account! [" + customer.getSelfLink().getUrl() + "]";
            // Utils.log(msg);
            // deployResponse.addError(msg);
        }
        else
        {
            mapObj.put(ContactFields.Account, getAccountMap(customer.getCompanyId()));
        }

        // add the desk custom fields
        mapObj.putAll(deskCustomFieldsToSalesforceJsonMap(customer.getCustomFields()));

        return mapObj;
    }

    public static List<Map<String, Object>> deskNoteToSalesforceJsonMap(DeskUtil deskUtil, Note note, DeployResponse deployResponse) throws Exception
    {
        // check if the note has a case id
        if (note.getCaseId() == 0)
        {
            // no case id, this is an error, log it and report it back to Desk.com
            String msg = String.format("[ERROR] Note does not have a case id associated to it! Note Id: [%d]",
                note.getId());
            deployResponse.addError(msg);
            Utils.log(msg);

            // return an empty list, this is easier than null handling the response
            return new ArrayList<Map<String, Object>>();
        }
        else
        {
            // create the list
            List<Map<String, Object>> mapObjs = new ArrayList<>();

            // create the map
            Map<String, Object> mapObj = new HashMap<>();

            // flag which indicates the note body was truncated
            boolean bTruncated = false;

            // audit fields
            if (deskUtil.getAuditFieldsEnabled())
            {
                mapObj.put(CaseCommentFields.CreatedDate, note.getCreatedAt());
                mapObj.put(CaseCommentFields.LastModifiedDate, note.getUpdatedAt());
            }

            // check the body for max length
            String body = note.getBody();
            if (body == null)
            {
                // do nothing
            }
            else
            {
                // trim the body if it is longer than max
                if (body.length() > SalesforceConstants.MED_TEXT_MAX)
                {
                    String msg = String.format("Body for note [%d] is too long and will be truncated! Length: [%d], Max Length: [%d]",
                        note.getId(), body.length(), SalesforceConstants.MED_TEXT_MAX);
                    Utils.log(msg);
                    deployResponse.addError(msg);

                    // trim the un-encoded body to the max
                    body = body.substring(0, SalesforceConstants.MED_TEXT_MAX);

                    // flip the flag indicated we truncated data
                    bTruncated = true;
                }

                // convert the JSON so I can check length
                String bodyJson = JsonUtil.toJson(body);

                // loop until the encoded length is <= the max
                while (bodyJson.length() > SalesforceConstants.MED_TEXT_MAX)
                {
                    // trim 1 character from the body
                    body = body.substring(0, body.length() - 1);

                    // JSON encode the shortened body so I can check the length
                    bodyJson = JsonUtil.toJson(body);
                    // Utils.log("Body Length: " + bodyJson.length());

                    // flip the flag indicated we truncated data
                    bTruncated = true;
                }

                // check if I truncated and need to adjust the body on the note for splitting it across multiple
                // CaseComment records
                if (bTruncated)
                {
                    // encoded length should now be good, left-trim the body
                    note.setBody(note.getBody().substring(body.length() - 1));
                }
            }
            mapObj.put(CaseCommentFields.CommentBody, body);
            mapObj.put(CaseCommentFields.IsPublished, false);

            // assign the case id
            mapObj.put(CaseCommentFields.Parent, getCaseMap(note.getCaseId()));

            mapObjs.add(mapObj);

            // check if I truncated the body
            if (bTruncated)
            {
                // recursively call this method to add another case comment with the rest of the note
                mapObjs.addAll(deskNoteToSalesforceJsonMap(deskUtil, note, deployResponse));
            }

            return mapObjs;
        }
    }

    /**
     * Converts interaction records into Map's that can be JSON encoded.
     * @param jsonMaps The Map where the key is the object type, and the value is a list of Map's that will be JSON
     * encoded later.
     * @param interactions The array of interaction records.
     * @param deployResponse The deploy response for logging messages.
     */
    public static void deskInteractionToSalesforceJsonMaps(DeskUtil deskUtil,
        Map<String, List<Map<String, Object>>> jsonMaps, Interaction[] interactions, DeployResponse deployResponse)
    {

        // local variable for holding JSON map temporarily
        Map<String, Object> jsonMap = null;

        // define a map for holding chat interactions for a case
        Map<Integer, List<Interaction>> chatInteractionsByCase = new HashMap<>();

        // loop through the interactions and convert them
        for (Interaction interaction : interactions)
        {
            // check if the interaction has a case id
            if (interaction.getCaseId() == 0)
            {
                // no case id, this is an error, log it and report it back to Desk.com
                String msg = String.format(
                    "[ERROR] Interaction does not have a case id associated to it! Interaction Id: [%d]",
                    interaction.getId());
                deployResponse.addError(msg);
                Utils.log(msg);

                // do nothing
            }
            else
            {
                // Utils.log("Interaction Type: " + interaction.getInteractionType());
                switch(interaction.getInteractionType())
                {
                    case EMAIL:
                        // convert the interaction
                        jsonMap = deskInteractionToSalesforceEmail(interaction, deployResponse);

                        // ensure object is not null
                        if (jsonMap != null)
                        {
                            jsonMaps.get(SalesforceConstants.OBJ_EMAIL_MESSAGE).add(jsonMap);
                        }
                        break;
                    case TWEET:
                    case FACEBOOK_POST:
                    case FACEBOOK_COMMENT:
                    case FACEBOOK_MESSAGE:
                        // TODO: Check if SocialPost is available in the Salesforce organization and use it

                        // I can't get this working right now, create case comments for the time being.
//                        // convert the interaction
//                        jsonMap = deskInteractionToSalesforceFeedItem(interaction,
//                            (interaction.getInteractionType() == InteractionType.TWEET), deployResponse);
//
//                        // ensure object is not null
//                        if (jsonMap != null)
//                        {
//                            jsonMaps.get(SalesforceConstants.OBJ_FEED_ITEM).add(jsonMap);
//                        }
                        // convert the interaction
                        jsonMaps.get(SalesforceConstants.OBJ_CASE_COMMENT).addAll(
                            deskInteractionToSalesforceComment(deskUtil, interaction, deployResponse));
                        break;
                    case PHONE_CALL:
                        // convert the interaction
                        jsonMaps.get(SalesforceConstants.OBJ_CASE_COMMENT).addAll(
                            deskInteractionToSalesforceComment(deskUtil, interaction, deployResponse));
                        break;
                    case CHAT_MESSAGE:
                        // check if this is a new case id
                        if (!chatInteractionsByCase.containsKey(interaction.getCaseId()))
                        {
                            chatInteractionsByCase.put(interaction.getCaseId(), new ArrayList<>());
                        }

                        // add the interaction
                        chatInteractionsByCase.get(interaction.getCaseId()).add(interaction);
                        break;
                    case COMMUNITY_ANSWER:
                    case COMMUNITY_QUESTION:
                        // convert the interaction
                        jsonMaps.get(SalesforceConstants.OBJ_CASE_COMMENT).addAll(
                            deskInteractionToSalesforceComment(deskUtil, interaction, deployResponse));
                        break;
                    case UNKNOWN:
                    default:
                        Utils.log(String.format("[WARN] '%s' is not implemented yet!", interaction.getInteractionType()));
                        break;
                }
            }
        }

        // check if there are chat records to process
        if (!chatInteractionsByCase.isEmpty())
        {
            // loop through the cases
            for (Integer caseId : chatInteractionsByCase.keySet())
            {
                // convert and add to the map
                jsonMaps.get(SalesforceConstants.OBJ_CASE_COMMENT).addAll(
                    deskInteractionToSalesforceChat(deskUtil, chatInteractionsByCase.get(caseId), deployResponse));
            }
        }

    }

    public static Map<String, Object> deskInteractionToSalesforceEmail(Interaction interaction, DeployResponse deployResponse)
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // apparently these are not editable even with the set audit fields permission enabled for the org
        // Unable to create/update fields: LastModifiedDate, CreatedDate. Please check the security settings of this field and verify that it is read/write for your profile or permission set.
//        // audit fields
//        if (DeskUtil.AUDIT_FIELDS_ENABLED)
//        {
//            mapObj.put(EmailMessageFields.CreatedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getCreatedAt()));
//            mapObj.put(EmailMessageFields.LastModifiedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getUpdatedAt()));
//        }

        // Can't set the CreatedById
        // "message" : "Unable to create/update fields: CreatedById. Please check the security settings of this field and verify that it is read/write for your profile or permission set.",
        // "fields" : [ "CreatedById" ],
        // "statusCode" : "INVALID_FIELD_FOR_INSERT_UPDATE"
//        // Agent who sent the email (if outgoing)
//        if (DeskUtil.AUDIT_FIELDS_ENABLED && interaction.isIncoming() == false)
//        {
//            mapObj.put(EmailMessageFields.CreatedBy, getUserMap(interaction.getSentById(), false));
//        }

        // The addresses that were sent a blind carbon copy of the email.
        if (interaction.getBcc() != null && !interaction.getBcc().isEmpty())
        {
            mapObj.put(EmailMessageFields.BccAddress, interaction.getBcc());
        }

        // The addresses that were sent a carbon copy of the email.
        if (interaction.getCc() != null && !interaction.getCc().isEmpty())
        {
            mapObj.put(EmailMessageFields.CcAddress, interaction.getCc());
        }

        // The address that originated the email.
        if (interaction.getFromAddress() != null)
        {
            mapObj.put(EmailMessageFields.FromAddress, interaction.getFromAddress());
        }

        // The sender’s name.
        if (interaction.getFromName() != null)
        {
            mapObj.put(EmailMessageFields.FromName, interaction.getFromName());
        }

        // ERROR: Unable to create/update fields: HasAttachment, IsExternallyVisible. Please check the security
        // settings of this field and verify that it is read/write for your profile or permission set.
//        // Indicates whether the email was sent with an attachment (true) or not (false).
//        mapObj.put(EmailMessageFields.HasAttachment, (interaction.getLinks().getAttachments().getCount() > 0));

        // The body of the email in HTML format. (max length=32000)
        if (interaction.getBody() != null && interaction.getBody().length() > SalesforceConstants.EMAIL_BODY_LENGTH)
        {
            String msg = String.format(
                "Interaction body exceeds max length and will be truncted! ");
            Utils.log(msg);
            deployResponse.addErrorWithId(msg, interaction.getId());
            interaction.setBody(interaction.getBody().substring(0, SalesforceConstants.EMAIL_BODY_LENGTH));
        }
        mapObj.put(EmailMessageFields.HtmlBody, interaction.getBody());

        // Indicates whether the email was received (true) or sent (false).
        mapObj.put(EmailMessageFields.Incoming, interaction.isIncoming());

//        // Controls the external visibility of email messages in communities, and is accessible only if the community
//        // case feed is enabled. When this field is set to true—its default value—email messages are visible to
//        // external users in the case feed. Update is not supported if the record Status is Draft.
//        mapObj.put(EmailMessageFields.IsExternallyVisible, false);

        // The date the email was created.
        mapObj.put(EmailMessageFields.MessageDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getCreatedAt()));

        // Case to which the email is associated.
        mapObj.put(EmailMessageFields.Parent, getCaseMap(interaction.getCaseId()));

        // The status of the email. For example, New, Draft, Unread, Replied, or Sent.
        mapObj.put(EmailMessageFields.Status, ("sent".equalsIgnoreCase(interaction.getStatus()) ?
            EmailMessageFields.StatusSent : EmailMessageFields.StatusNew));

        // The subject line of the email.
        mapObj.put(EmailMessageFields.Subject, interaction.getSubject());

        // The address of the email’s recipient.
        mapObj.put(EmailMessageFields.ToAddress, interaction.getTo());

        return mapObj;
    }

    public static Map<String, Object> deskInteractionToSalesforceFeedItem(DeskUtil deskUtil, Interaction interaction, boolean isTweet,
        DeployResponse deployResponse)
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // audit fields
        if (deskUtil.getAuditFieldsEnabled())
        {
            mapObj.put(FeedItemFields.CreatedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getCreatedAt()));
            mapObj.put(FeedItemFields.LastModifiedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getUpdatedAt()));
        }

        // The content of the FeedItem. Required when Type is TextPost.
        String socialInfo = null;
        if (isTweet)
        {
            socialInfo =
                "Type: " + interaction.getType() + "\n" +
                "To: " + interaction.getTo() + "\n" +
                "From: " + interaction.getFrom() + "\n" +
                "Status: " + interaction.getStatus() + "\n" +
                "Twitter Status Id: " + interaction.getTwitterStatusId() + "\n";
        }
        else
        {
            socialInfo =
                "Facebook Id: " + interaction.getFacebookId() + "\n" +
                "Facebook From Name: " + interaction.getFromFacebookName() + "\n" +
                "Status: " + interaction.getStatus() + "\n" +
                "Liked: " + interaction.getFacebookLiked() + "\n";
        }
        mapObj.put(FeedItemFields.Body, socialInfo + interaction.getBody());

        // Indicates whether the feed item Body contains rich text. Set IsRichText to true if you post a rich text feed
        // item via the SOAP API. Otherwise, the post is rendered as plain text.
        mapObj.put(FeedItemFields.IsRichText, false);

        // The type of FeedItem. Except for ContentPost, LinkPost, and TextPost, don’t create FeedItem types directly
        // from the API.
        // SocialPost—generated when a social post is created from a case.
//        mapObj.put(FeedItemFields.Type, "SocialPost");
        mapObj.put(FeedItemFields.Type, "TextPost");

        // Case to which the email is associated.
        mapObj.put(EmailMessageFields.Parent, getCaseMap(interaction.getCaseId()));

        return mapObj;
    }

    private static List<Map<String, Object>> deskInteractionToSalesforceChat(DeskUtil deskUtil, List<Interaction> interactions, DeployResponse deployResponse)
    {
        // create the list
        List<Map<String, Object>> mapObjs = new ArrayList<>();

        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // list of chat bodies
        List<String> chatLines = new ArrayList<>();

        // fields that are summarized across the interaction records
        Date startTime = null;
        Date endTime = null;
        Integer caseId = null;
        Integer userId = null;

        // loop through the chat messages so I can build a single chat transcript body
        for (Interaction interaction : interactions)
        {
            // TODO: Can we assume these are ordered? docs say "sorted by updated_at".
            // [2014-07-30T03:31:25Z] Agent: You are now connected with Agent Lucy Liu
            // [2014-07-30T03:31:41Z] Agent: We have many resorts that would accommodate you. Thanks for contacting us!
            // [2014-07-30T03:32:40Z] Customer: I am a very important toddler, so it has to be an ultra-exclusive place. Can you suggest any places?
            // [2014-07-30T03:32:59Z] Agent: How many toddlers are coming? What activities will you be interested in?
            chatLines.add(String.format("[%s] %s: %s",
                interaction.getCreatedAt(),
                (interaction.isIncoming() ? ChatFields.ChatTranscriptCustomer : ChatFields.ChatTranscriptAgent),
                interaction.getBody()));

            // get the case id if I have not already
            if (caseId == null && interaction.getCaseId() != Interaction.NO_ID)
            {
                caseId = interaction.getCaseId();
            }

            // get the user id if I have not already
            if (userId == null && interaction.getCreatedById() != Interaction.NO_ID)
            {
                userId = interaction.getCreatedById();
            }

            // find the lowest created time and assign it to the start time of the chat
            if (startTime == null || interaction.getCreatedAt().getTime() < startTime.getTime())
            {
                startTime = interaction.getCreatedAt();
            }

            // find the highest updated time and assign it to the end time of the chat
            if (endTime == null || interaction.getUpdatedAt().getTime() > endTime.getTime())
            {
                endTime = interaction.getUpdatedAt();
            }
        }

        // create a new mapObj
        mapObj = getChatComment(deskUtil, startTime, endTime, caseId, userId);

        // check if there are chat lines
        if (!chatLines.isEmpty())
        {
            // create a string builder to hold the chat body
            StringBuilder chatBody = new StringBuilder();

            // loop through the chat lines
            for (String chatLine : chatLines)
            {
                // check if the JSON encoded length is too long
                if (JsonUtil.toJson(chatBody.toString() + "\n" + chatLine).length() > SalesforceConstants.MED_TEXT_MAX)
                {
                    // log a message
                    String msg = String.format(
                        "Chat body for Case is too long and will be truncated!");
                    Utils.log(msg);
                    deployResponse.addErrorWithId(msg, caseId);

                    // assign the body to the comment
                    mapObj.put(CaseCommentFields.CommentBody, chatBody.toString());

                    // add the map object to the list
                    mapObjs.add(mapObj);

                    // create a new mapObj
                    mapObj = getChatComment(deskUtil, startTime, endTime, caseId, userId);

                    // clear the chat body
                    chatBody = new StringBuilder();
                }

                // append the line to the body
                chatBody.append(chatLine + "\n");
            }

            // assign the body to the comment
            mapObj.put(CaseCommentFields.CommentBody, chatBody.toString());

            // add the map object to the list
            mapObjs.add(mapObj);
        }

        return mapObjs;
    }

    private static Map<String, Object> getChatComment(DeskUtil deskUtil, Date startTime, Date endTime, Integer caseId, Integer userId)
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // audit fields
        if (deskUtil.getAuditFieldsEnabled())
        {
            mapObj.put(CaseCommentFields.CreatedDate, SalesforceUtil.sfdcDateTimeFormat(startTime));
            mapObj.put(CaseCommentFields.LastModifiedDate, SalesforceUtil.sfdcDateTimeFormat(endTime));
        }

        // check if the answer is from an agent
        if (caseId != null)
        {
            // Agent who answered the question
            mapObj.put(CaseCommentFields.Parent, getCaseMap(caseId));
        }

        // check if the answer is from an agent
        if (deskUtil.getAuditFieldsEnabled() && userId != null)
        {
            // Agent who answered the question
            mapObj.put(CaseCommentFields.CreatedBy, getUserMap(userId, true));
        }

        return mapObj;
    }

    private static List<Map<String, Object>> deskInteractionToSalesforceComment(DeskUtil deskUtil, Interaction interaction, DeployResponse deployResponse)
    {
        // create the list
        List<Map<String, Object>> mapObjs = new ArrayList<>();

        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        // flag which indicates the note body was truncated
        boolean bTruncated = false;

        // audit fields
        if (deskUtil.getAuditFieldsEnabled())
        {
            mapObj.put(CaseCommentFields.CreatedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getCreatedAt()));
            mapObj.put(CaseCommentFields.LastModifiedDate, SalesforceUtil.sfdcDateTimeFormat(interaction.getUpdatedAt()));
        }

        // The content of the FeedItem. Required when Type is TextPost.
        String typeInfo = "";
        if (interaction.getInteractionType() == InteractionType.TWEET)
        {
            typeInfo =
                "Twitter\n" +
                "Type: " + interaction.getType() + "\n" +
                "To: " + (interaction.getTo() == null ? "" : interaction.getTo()) + "\n" +
                "From: " + (interaction.getFrom() == null ? "" : interaction.getFrom()) + "\n" +
                "Status: " + interaction.getStatus() + "\n" +
                "Status Id: " + (interaction.getTwitterStatusId() == null ? "" : interaction.getTwitterStatusId()) + "\n";
        }
        else if (interaction.getInteractionType() == InteractionType.FACEBOOK_POST ||
            interaction.getInteractionType() == InteractionType.FACEBOOK_COMMENT ||
            interaction.getInteractionType() == InteractionType.FACEBOOK_MESSAGE)
        {
            typeInfo =
                "Facebook\n" +
                "Id: " + interaction.getFacebookId() + "\n" +
                "From Name: " + interaction.getFromFacebookName() + "\n" +
                "Status: " + interaction.getStatus() + "\n" +
                "Liked: " + interaction.getFacebookLiked() + "\n";
        }
        else if (interaction.getInteractionType() == InteractionType.COMMUNITY_QUESTION)
        {
            typeInfo =
                "Community Question\n" +
                "URL: " + interaction.getPublicUrl() + "\n" +
                String.format("Agent Answer Count [%d], Customer Answer Count [%d]\n",
                    interaction.getAgentAnswerCount(), interaction.getCustomerAnswerCount()) +
                "Subject: " + (interaction.getSubject() == null ? "" : interaction.getSubject()) + "\n";
        }
        else if (interaction.getInteractionType() == InteractionType.COMMUNITY_ANSWER)
        {
            typeInfo =
                "Community Answer\n" +
                (interaction.isBestAnswer() ? "*** Best Answer ***\n" : "") +
                String.format("Rating [%d], Rating Count [%d], Rating Score [%d]\n",
                    interaction.getRating(), interaction.getRatingCount(), interaction.getRatingScore()) +
                "Subject: " + (interaction.getSubject() == null ? "" : interaction.getSubject()) + "\n";

            // check if the answer is from an agent and if audit fields are enabled
            if (deskUtil.getAuditFieldsEnabled() && interaction.isIncoming())
            {

                // Agent who answered the question
                mapObj.put(CaseCommentFields.CreatedBy, getUserMap(interaction.getSentById(), true));
            }
        }
        else if (interaction.getInteractionType() == InteractionType.PHONE_CALL)
        {
            // only set if audit fields are enabled
            if (deskUtil.getAuditFieldsEnabled())
            {
                // Agent who took the phone call
                mapObj.put(CaseCommentFields.CreatedBy, getUserMap(interaction.getEnteredById(), true));
            }
        }

        // check the body for max length
        String body = typeInfo + interaction.getBody();
        if (body == null || body.isEmpty())
        {
            // do nothing
        }
        else
        {
            // trim the body if it is longer than max
            if (body.length() > SalesforceConstants.MED_TEXT_MAX)
            {
                String msg = String.format("Body for interaction is too long and will be truncated!");

                Utils.log(msg);
                deployResponse.addErrorWithId(msg, interaction.getId());

                // trim the un-encoded body to the max
                body = body.substring(0, SalesforceConstants.MED_TEXT_MAX);

                // flip the flag indicated we truncated data
                bTruncated = true;
            }

            // convert the JSON so I can check length
            String bodyJson = JsonUtil.toJson(body);

            // loop until the encoded length is <= the max
            while (bodyJson.length() > SalesforceConstants.MED_TEXT_MAX)
            {
                // trim 1 character from the body
                body = body.substring(0, body.length() - 1);

                // JSON encode the shortened body so I can check the length
                bodyJson = JsonUtil.toJson(body);
                // Utils.log("Body Length: " + bodyJson.length());

                // flip the flag indicated we truncated data
                bTruncated = true;
            }

            // check if I truncated and need to adjust the body on the interaction for splitting it across multiple
            // CaseComment records
            if (bTruncated)
            {
                // encoded length should now be good, left-trim the body
                interaction.setBody(body.substring(body.length() - 1));
            }
        }
        mapObj.put(CaseCommentFields.CommentBody, body);
        mapObj.put(CaseCommentFields.IsPublished, false);

        // assign the case id
        mapObj.put(CaseCommentFields.Parent, getCaseMap(interaction.getCaseId()));

        mapObjs.add(mapObj);

        // check if I truncated the body
        if (bTruncated)
        {
            // recursively call this method to add another case comment with the rest of the interaction
            mapObjs.addAll(deskInteractionToSalesforceComment(deskUtil, interaction, deployResponse));
        }

        return mapObjs;
    }

    private static Map<String, Object> getLookupMap(String field, int id)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(field, id);
        return map;
    }

    private static Map<String, Object> getAccountMap(int deskCompanyId)
    {
        return getLookupMap(AccountFields.DeskId, deskCompanyId);
    }

    private static Map<String, Object> getContactMap(int deskCustomerId)
    {
        return getLookupMap(ContactFields.DeskId, deskCustomerId);
    }

    private static Map<String, Object> getCaseMap(int deskCaseId)
    {
        return getLookupMap(CaseFields.DeskId, deskCaseId);
    }

    private static Map<String, Object> getUserMap(int deskUserId, boolean polymorphic)
    {
        Map<String, Object> userMap = getLookupMap(UserFields.DeskId, deskUserId);

        // check if this is the polymorphic user field
        if (polymorphic)
        {
            /**
Since CreatedBy is a polymorphic field it is required to specify type of the object in json or xml file
Below is json string which was correctly inserted using Rest API POST (notice "attributes" object).

{
"Parent":{"DeskId__c":64},
"CommentBody":"Following up on my forgot password email.",
"CreatedBy":{ "attributes" : {"type" : "User"},"DeskId__c":453259 },
"IsPublished":false,
"CreatedDate":"2014-04-30T16:28:44.000Z",
"LastModifiedDate":"2014-05-01T10:02:10.000Z"
}
             */
            Map<String, Object> userAttributesMap = new HashMap<String, Object>();
            userAttributesMap.put("type", SalesforceConstants.OBJ_USER);
            userMap.put("attributes", userAttributesMap);
        }
        return userMap;
    }

    public static Map<String, Object> deskArticleToSalesforceJsonMap(Article article, DeployResponse deployResponse, Integer counter) throws Exception
    {
        // create the map
        Map<String, Object> mapObj = new HashMap<>();

        mapObj.put("Body__c", article.getBody());
        mapObj.put("Title", article.getSubject());

        // sfArticle.setField("Chat_Answer__c", a.getBodyChat());
        // Utils.log(System.currentTimeMillis());
        Utils.log(article.getSubject().replaceAll(" ", "-").replaceAll("/[^A-Za-z0-9 ]/", "") + counter);
        if (article.getSubject() == null || article.getSubject().isEmpty())
        {
            mapObj.put("UrlName",
                String.valueOf(System.currentTimeMillis()).replaceAll("/[^A-Za-z0-9 ]/", "") + counter);
        }
        else
        {
            String subject = "a" + article.getSubject().replaceAll(" ", "-").replaceAll("[^A-Za-z0-9]", "")
                + System.currentTimeMillis() + counter;
            if (subject.length() > 255)
            {
                subject = subject.substring(0, 255);
            }
            Utils.log("Subject: " + subject);
            mapObj.put("UrlName", subject);
        }
        // sfArticle.setField("IsVisibleInPkb", a.);//TODO get in_support_center
        // sfArticle.setField("IsVisibleInCsp", a.);//TODO get in_support_center
        // sfArticle.setField("IsVisibleInPrm", a.);//TODO get in_support_center
        mapObj.put("Internal_Notes__c", article.getInternalNotes());
        // sfArticle.setField("Language", a.getLocale());
        // sfArticle.setField("ArticleType", "Desk_com");
        // sfArticle.setField("IsVisibleInApp", true);
        if (article.getBodyPhone().length() > 999)
        {
            mapObj.put("Summary", article.getBodyPhone().substring(0, 999));
        }
        else
        {
            mapObj.put("Summary", article.getBodyPhone());
        }
        return mapObj;
    }

}
