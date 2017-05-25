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

import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskCaseToSalesforceJsonMap;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskCompanyToSalesforceJsonMap;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskCustomerToSalesforceJsonMap;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskInteractionToSalesforceJsonMaps;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskLabelToSalesforceTopicJsonMap;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskNoteToSalesforceJsonMap;
import static com.salesforce.scmt.utils.DeskJsonMapUtil.deskUserToSalesforceJsonMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.desk.java.apiclient.DeskClient;
import com.desk.java.apiclient.model.ApiResponse;
import com.desk.java.apiclient.model.Article;
import com.desk.java.apiclient.model.Attachment;
import com.desk.java.apiclient.model.Case;
import com.desk.java.apiclient.model.Company;
import com.desk.java.apiclient.model.CustomField;
import com.desk.java.apiclient.model.CustomFieldDataType;
import com.desk.java.apiclient.model.CustomFieldType;
import com.desk.java.apiclient.model.Customer;
import com.desk.java.apiclient.model.Fields;
import com.desk.java.apiclient.model.Group;
import com.desk.java.apiclient.model.Interaction;
import com.desk.java.apiclient.model.Interaction.InteractionType;
import com.desk.java.apiclient.model.Label;
import com.desk.java.apiclient.model.Message;
import com.desk.java.apiclient.model.Note;
import com.desk.java.apiclient.model.SiteLanguage;
import com.desk.java.apiclient.model.SiteSetting;
import com.desk.java.apiclient.model.SortDirection;
import com.desk.java.apiclient.model.User;
import com.desk.java.apiclient.service.ArticleService;
import com.desk.java.apiclient.service.CaseService;
import com.desk.java.apiclient.service.CompanyService;
import com.desk.java.apiclient.service.CustomFieldsService;
import com.desk.java.apiclient.service.CustomerService;
import com.desk.java.apiclient.service.GroupService;
import com.desk.java.apiclient.service.InteractionService;
import com.desk.java.apiclient.service.LabelService;
import com.desk.java.apiclient.service.NoteService;
import com.desk.java.apiclient.service.SiteService;
import com.desk.java.apiclient.service.UserService;
import com.desk.java.apiclient.util.StringUtils;
import com.google.gson.reflect.TypeToken;
import com.salesforce.scmt.model.DeployResponse;
import com.salesforce.scmt.rabbitmq.RabbitConfiguration;
import com.salesforce.scmt.service.DeskService;
import com.salesforce.scmt.service.SalesforceService;
import com.salesforce.scmt.utils.SalesforceConstants.AccountFields;
import com.salesforce.scmt.utils.SalesforceConstants.AttachmentFields;
import com.salesforce.scmt.utils.SalesforceConstants.CaseCommentFields;
import com.salesforce.scmt.utils.SalesforceConstants.CaseFields;
import com.salesforce.scmt.utils.SalesforceConstants.ContactFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMessageFields;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.salesforce.scmt.utils.SalesforceConstants.EmailMessageFields;
import com.salesforce.scmt.utils.SalesforceConstants.FeedItemFields;
import com.salesforce.scmt.utils.SalesforceConstants.GroupMemberFields;
import com.salesforce.scmt.utils.SalesforceConstants.TopicFields;
import com.salesforce.scmt.utils.SalesforceConstants.UserFields;
import com.sforce.async.AsyncApiException;
import com.sforce.async.OperationEnum;
import com.sforce.soap.metadata.*;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.soap.partner.sobject.SObject;
import com.squareup.okhttp.Headers;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import retrofit.Response;

public final class DeskUtil
{
    /**
     * Map where the key is the Desk.com language code and the value is the Salesforce language code.
     */
    private static final Map<String, String> Desk2SalesforceLangCode;

    static // initialize the map
    {
        Desk2SalesforceLangCode = new TreeMap<>();
        Desk2SalesforceLangCode.put("ar", "ar");
        Desk2SalesforceLangCode.put("cs", "cs");
        Desk2SalesforceLangCode.put("da", "da");
        Desk2SalesforceLangCode.put("de", "de");
        Desk2SalesforceLangCode.put("de_ch", "de_CH");
        Desk2SalesforceLangCode.put("el", "el");
        Desk2SalesforceLangCode.put("en", "en_US");
        Desk2SalesforceLangCode.put("en_au", "en_AU");
        Desk2SalesforceLangCode.put("en_ca", "en_CA");
        Desk2SalesforceLangCode.put("en_gb", "en_GB");
        Desk2SalesforceLangCode.put("en_hk", "en_HK");
        Desk2SalesforceLangCode.put("en_ie", "en_IE");
        Desk2SalesforceLangCode.put("en_sg", "en_SG");
        Desk2SalesforceLangCode.put("en_us", "en_US");
        Desk2SalesforceLangCode.put("en_za", "en_ZA");
        Desk2SalesforceLangCode.put("es", "es");
        Desk2SalesforceLangCode.put("es_es", "es");
        Desk2SalesforceLangCode.put("es_mx", "es_MX");
        Desk2SalesforceLangCode.put("es_us", "es_US");
        Desk2SalesforceLangCode.put("fi", "fi");
        Desk2SalesforceLangCode.put("fil", "tl");
        Desk2SalesforceLangCode.put("fr", "fr");
        Desk2SalesforceLangCode.put("fr_ca", "fr_CA");
        Desk2SalesforceLangCode.put("fr_fr", "fr");
        Desk2SalesforceLangCode.put("he", "iw");
        Desk2SalesforceLangCode.put("hi", "hi");
        Desk2SalesforceLangCode.put("hu", "hu");
        Desk2SalesforceLangCode.put("id", "in");
        Desk2SalesforceLangCode.put("it", "it");
        Desk2SalesforceLangCode.put("ja", "ja");
        Desk2SalesforceLangCode.put("ko", "ko");
        Desk2SalesforceLangCode.put("ms", "ms");
        Desk2SalesforceLangCode.put("nl", "nl_NL");
        Desk2SalesforceLangCode.put("no", "no");
        Desk2SalesforceLangCode.put("pl", "pl");
        Desk2SalesforceLangCode.put("pt", "pt_PT");
        Desk2SalesforceLangCode.put("pt_br", "pt_BR");
        Desk2SalesforceLangCode.put("ro_ro", "ro");
        Desk2SalesforceLangCode.put("ru", "ru");
        Desk2SalesforceLangCode.put("sl", "sl");
        Desk2SalesforceLangCode.put("sv", "sv");
        Desk2SalesforceLangCode.put("th", "th");
        Desk2SalesforceLangCode.put("tr", "tr");
        Desk2SalesforceLangCode.put("zh_cn", "zh_CN");
        Desk2SalesforceLangCode.put("zh_tw", "zh_TW");
    }

    private static final Map<String, String> DeskLangId2Name;

    static // initialize the map
    {
        DeskLangId2Name = new TreeMap<>();
        DeskLangId2Name.put("ar", "Arabic");
        DeskLangId2Name.put("ca", "Catalan");
        DeskLangId2Name.put("cs", "Czech");
        DeskLangId2Name.put("da", "Danish");
        DeskLangId2Name.put("de", "German");
        DeskLangId2Name.put("de_ch", "German (Switzerland)");
        DeskLangId2Name.put("el", "Greek");
        DeskLangId2Name.put("en", "English");
        DeskLangId2Name.put("en_au", "English (Australia)");
        DeskLangId2Name.put("en_ca", "English (Canada)");
        DeskLangId2Name.put("en_gb", "English (United Kingdom)");
        DeskLangId2Name.put("en_hk", "English (Hong Kong)");
        DeskLangId2Name.put("en_ie", "English (Ireland)");
        DeskLangId2Name.put("en_jp", "English (Japanese)");
        DeskLangId2Name.put("en_nz", "English (New Zealand)");
        DeskLangId2Name.put("en_sg", "English (Singapore)");
        DeskLangId2Name.put("en_us", "English (US)");
        DeskLangId2Name.put("en_za", "English (South Africa)");
        DeskLangId2Name.put("es", "Spanish");
        DeskLangId2Name.put("es_es", "Spanish (Spain)");
        DeskLangId2Name.put("es_mx", "Spanish (Mexico)");
        DeskLangId2Name.put("es_us", "Spanish (United States)");
        DeskLangId2Name.put("fi", "Finnish");
        DeskLangId2Name.put("fil", "Tagalog");
        DeskLangId2Name.put("fr", "French");
        DeskLangId2Name.put("fr_ca", "French (Canada)");
        DeskLangId2Name.put("fr_fr", "French (France)");
        DeskLangId2Name.put("he", "Hebrew");
        DeskLangId2Name.put("hi", "Hindi");
        DeskLangId2Name.put("hu", "Hungarian");
        DeskLangId2Name.put("id", "Indonesian");
        DeskLangId2Name.put("it", "Italian");
        DeskLangId2Name.put("ja", "Japanese");
        DeskLangId2Name.put("ko", "Korean");
        DeskLangId2Name.put("ms", "Malay");
        DeskLangId2Name.put("nl", "Dutch");
        DeskLangId2Name.put("no", "Norwegian");
        DeskLangId2Name.put("pl", "Polish");
        DeskLangId2Name.put("pt", "Portuguese");
        DeskLangId2Name.put("pt_br", "Portuguese (Brazil)");
        DeskLangId2Name.put("ro_ro", "Romanian (Romania)");
        DeskLangId2Name.put("ru", "Russian");
        DeskLangId2Name.put("sl", "Slovenian");
        DeskLangId2Name.put("sv", "Swedish");
        DeskLangId2Name.put("sv_fi", "Swedish (Finland)");
        DeskLangId2Name.put("sw", "Swahili");
        DeskLangId2Name.put("th", "Thai");
        DeskLangId2Name.put("tr", "Turkish");
        DeskLangId2Name.put("zh_cn", "Chinese (Simplified)");
        DeskLangId2Name.put("zh_tw", "Chinese (Traditional)");
    }

    public static final String DESK_MESSAGE_ATTACHMENT_PREFIX = "Desk-Case-Attachments-";
    
    private static final String DESK_HEADER_LIMIT_LIMIT = "X-Rate-Limit-Limit";
    private static final String DESK_HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    public static final String DESK_HEADER_LIMIT_RESET = "X-Rate-Limit-Reset";
    
    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    
    private static final Pattern SINCE_ID_PATTERN = Pattern.compile(".*since_id=(\\d+).*");

    // example date: 2014-12-08T04:58:01Z,
    public static final String DESK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Desk will only allow you to retrieve 500 pages. When you request the the 501 page it responds with:
     * {"message":"page parameter must be less than or equal to 500"}
     * So added this constant so we can watch for when we are on 500th page we can grab the last id and reset the page
     * counter.
     */
    private static final int DESK_MAX_PAGES = 500;

    private static final int DESK_PAGE_SIZE_CF = 1000;
    private static final int DESK_PAGE_SIZE_GROUP = 1000;
    private static final int DESK_PAGE_SIZE_USER = 1000;
    private static final int DESK_PAGE_SIZE_CUSTOMER = 100;
    private static final int DESK_PAGE_SIZE_COMPANY = 500;
    private static final int DESK_PAGE_SIZE_LABEL = 1000;
    private static final int DESK_PAGE_SIZE_CASE = 100; // API doc report this as 500, but the max size is really 100
    private static final int DESK_PAGE_SIZE_NOTE = 100;
    private static final int DESK_PAGE_SIZE_INTERACTION = 100;
    private static final int DESK_PAGE_SIZE_ARTICLE = 500;
    private static final int DESK_PAGE_SIZE_FEED = 50;
    private static final int DESK_COMPANY_ID_MAX = 50;

    private DeskService _deskService;

    private List<CustomField> _deskCustomFields;
    
    /**
     * Private constructor for utility class.
     */
    public DeskUtil(DeskService deskService)
    {
        _deskService = deskService;
    }
    
    public DeskService getDeskService()
    {
        return _deskService;
    }
    
    public DeskClient getDeskClient()
    {
        return getDeskService().getClient();
    }
    
    public SalesforceService getSalesforceService()
    {
        return getDeskService().getSalesforceService();
    }
    
    public Map<String, Object> getDeskSiteLanguagesMap() throws Exception
    {
        // initialize the set to hold the language codes
        Map<String, Object> enabledLanguagesList = new TreeMap<>();

        // get the enabled languages
        List<SiteLanguage> enabledLanguages = getDeskSiteLanguagesEnabled();

        // loop through the enabled languages
        for (SiteLanguage enabledLanguage : enabledLanguages)
        {
            Map<String, String> enabledLanguageMap = new TreeMap<>();
            enabledLanguageMap.put("id", enabledLanguage.getId());
            enabledLanguageMap.put("name", DeskLangId2Name.get(enabledLanguage.getId()));
            enabledLanguageMap.put("salesforce_code", Desk2SalesforceLangCode.get(enabledLanguage.getId()));
            enabledLanguageMap.put("supported",
                (SalesforceUtil.SupportedLanguages.containsKey(Desk2SalesforceLangCode.get(enabledLanguage.getId()))
                    ? "true" : "false"));
            enabledLanguagesList.put(enabledLanguage.getId(), enabledLanguageMap);
        }

        // return the enabled languages as a set of strings
        return enabledLanguagesList;
    }

    private List<SiteLanguage> getDeskSiteLanguagesEnabled() throws Exception
    {
        List<SiteLanguage> languages = getDeskSiteLanguages();
        List<SiteLanguage> enabledLanguages = new ArrayList<>();

        // loop through the languages and remove the disabled ones
        for (SiteLanguage language : languages)
        {
            // check if this language is enabled for cases (it is enabled)
            if (language.isCase())
            {
                // add the 'enabled' language
                enabledLanguages.add(language);
            }
        }

        // return the list of enabled languages
        return enabledLanguages;
    }

    private List<SiteLanguage> getDeskSiteLanguages() throws Exception
    {
        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<SiteLanguage>> resp = null;
        ApiResponse<SiteLanguage> apiResp = null;

        // get a service
        SiteService service = getDeskClient().sites();

        // retrieve the records synchronously
        resp = service.getSiteLanguages().execute();

        // check for success
        if (resp.isSuccess())
        {
            // get the response body
            apiResp = resp.body();

            // retrieve the list of records
            return apiResp.getEntriesAsList();
        }
        else
        {
            Utils.log(resp.headers().toString());
            throw new Exception(
                String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
        }
    }

    public Map<String, Object> getDeskSiteSettingsAsMap() throws Exception
    {
        // declare the return map
        Map<String, Object> settingsMap = new TreeMap<>();
        // retrieve the list of records
        List<SiteSetting> settings = getDeskSiteSettings();

        // loop through the settings and turn into a map
        for (SiteSetting setting : settings)
        {
            // Utils.log("\t" + setting.getName() + ": " + setting.getValue() + " (type: " +
            // setting.getValue().getClass() + ")");
            settingsMap.put(setting.getName(), setting.getValue());
        }

        // return the settings map
        return settingsMap;
    }

    public List<SiteSetting> getDeskSiteSettings() throws Exception
    {

        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<SiteSetting>> resp = null;
        ApiResponse<SiteSetting> apiResp = null;

        // get a service
        SiteService service = getDeskClient().sites();

        // retrieve the records synchronously
        resp = service.getSiteSettings().execute();

        // check for success
        if (resp.isSuccess())
        {
            // get the response body
            apiResp = resp.body();

            // retrieve the list of records
            return apiResp.getEntriesAsList();
        }
        else
        {
            Utils.log(resp.headers().toString());
            throw new Exception(
                String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
        }
    }

    public CustomField getDeskCustomField(String name) throws Exception
    {
        for (CustomField cf : getDeskCustomFields()) {
            if (cf.getName().equals(name)) {
                return cf;
            }
        }
        return null;
    }

    public List<CustomField> getDeskCustomFields() throws Exception
    {
        if (_deskCustomFields == null) {
            // declare page counter
            int page = 0;

            // declare the response objects at this scope so I can check them in the do/while loop
            Response<ApiResponse<CustomField>> resp = null;
            ApiResponse<CustomField> apiResp = null;

            // declare the list that will be returned
            _deskCustomFields = new ArrayList<>();

            // get a service
            CustomFieldsService service = getDeskClient().customFields();

            // loop through retrieving records
            do {
                // increment the page counter
                page++;

                // retrieve the records synchronously
                resp = service.getCustomFields(DESK_PAGE_SIZE_CF, page).execute();

                // check for success
                if (resp.isSuccess()) {
                    // get the response body
                    apiResp = resp.body();

                    // add the list of records to the return list
                    _deskCustomFields.addAll(apiResp.getEntriesAsList());
                } else {
                    Utils.log(resp.headers().toString());
                    throw new Exception(
                            String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
                }
            }
            // continue to loop while the request is successful and there are subsequent pages of results
            while (resp.isSuccess() && apiResp.hasNextPage());
        }

        // return the list of records
        return _deskCustomFields;
    }

    public Map<Integer, String> getDeskGroupIdAndName() throws Exception
    {            	
        if (getDeskService().getDeskGroupId2Name() == null || getDeskService().getDeskGroupId2Name().isEmpty())
        {
            // declare the map to hold the group id to name
            getDeskService().setDeskGroupId2Name(new HashMap<>());
     
            // retrieve the groups
            List<Group> groups = getDeskGroups();
            
            // loop through the groups and put them into the map
            for (Group group : groups)
            {            	
                getDeskService().getDeskGroupId2Name().put(group.getId(), group.getName());
            }
        }

        // return the map
        return getDeskService().getDeskGroupId2Name();
    }

    public List<User> getDeskUsers() throws Exception
    {
        if (getDeskService().getDeskUsers() == null || getDeskService().getDeskUsers().isEmpty()) {
            int page = 1;
            boolean bRetry = false;
            Response<ApiResponse<User>> resp = null;
            ApiResponse<User> apiResp = null;
            getDeskService().setDeskUsers(new ArrayList<>());
            UserService service = getDeskClient().users();

            do {
                bRetry = false;
                resp = service.getUsers(DESK_PAGE_SIZE_USER, page).execute();
                if (resp.isSuccess()) {
                    apiResp = resp.body();
                    getDeskService().getDeskUsers().addAll(apiResp.getEntriesAsList());
                    page++;
                } else {
                    if (resp.code() == 429) {
                        bRetry = true;
                    } else if (resp.code() == java.net.HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        bRetry = true;
                    } else {
                        Utils.log(resp.headers().toString());
                        throw new Exception(
                                String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
                    }
                }
            } while (bRetry || (resp.isSuccess() && apiResp.hasNextPage()));
        }

        return getDeskService().getDeskUsers();
    }

    public List<Group> getDeskGroups() throws Exception
    {
        // check if we have retrieved the groups already
        if (getDeskService().getDeskGroups() == null || getDeskService().getDeskGroups().isEmpty())
        {
            // declare page counter
            int page = 1;

            // declare the retry flag
            boolean bRetry = false;

            // declare the response objects at this scope so I can check them in the do/while loop
            Response<ApiResponse<Group>> resp = null;
            ApiResponse<Group> apiResp = null;

            // declare the list that will be returned
            getDeskService().setDeskGroups(new ArrayList<>());

            // get a service
            GroupService service = getDeskClient().groups();

            // loop through retrieving records
            do
            {
                // reset the retry flag
                bRetry = false;

                // retrieve the records synchronously
                resp = service.getGroups(DESK_PAGE_SIZE_GROUP, page).execute();

                // check for success
                if (resp.isSuccess())
                {
                    // get the response body
                    apiResp = resp.body();

                    // add the list of records to the return list
                    getDeskService().getDeskGroups().addAll(apiResp.getEntriesAsList());

                    // increment the page counter
                    page++;
                }
                else
                {
                    if (resp.code() == 429)
                    {
                        // 'too many requests'
                        // re-queue or retry
                        bRetry = true;
                    }
                    else if (resp.code() == java.net.HttpURLConnection.HTTP_INTERNAL_ERROR)
                    {
                        // when we run imports through the API with threaded requests we'll occasionally get a 500
                        // response and have to retry the request (which succeeds on the retry).
                        // TODO: Retry
                        bRetry = true;
                    }
                    else
                    {
                        Utils.log(resp.headers().toString());
                        throw new Exception(
                            String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
                    }
                }
            }
            // continue to loop while the request is successful and there are subsequent pages of results
            while (bRetry || (resp.isSuccess() && apiResp.hasNextPage()));
        }

        // return the list of records        
        return getDeskService().getDeskGroups();
    }

    public Set<String> getDeskGroupNames() throws Exception
    {
        // get the groups
        List<Group> groups = getDeskGroups();

        // convert groups to list of group names
        Set<String> groupNames = new TreeSet<>();

        // loop through the groups
        for (Group group : groups)
        {
            // add the name to the set
            groupNames.add(group.getName());
        }

        return groupNames;
    }

    public Set<Integer> getDeskGroupIds() throws Exception
    {
        // get the groups
        List<Group> groups = getDeskGroups();

        // convert groups to list of group ids
        Set<Integer> groupIds = new TreeSet<>();

        // loop through the groups
        for (Group group : groups)
        {
            // add the id to the set
            groupIds.add(group.getId());
        }

        return groupIds;
    }

    /**
     * Get the SFDC Id for a queue when passing the Desk Id for a group
     */
    public String getQueueId(int deskGroupId) throws Exception
    {
        String queueId = null;

        // get the desk group name, trim trailing spaces and ensure it is the max 40 char limit of Salesforce
        String deskGroupName = String.format("Desk_%d", deskGroupId);
        
        // get the queue name => id map
        Map<String, String> queueName2Id = getSalesforceService().getQueues();
        
        // check if the map contains the desk group name
        if (queueName2Id.containsKey(deskGroupName))
        {        	
            // get SFDC QueueId from DeskGroupName
            queueId = queueName2Id.get(deskGroupName);
        }
        else
        {
            Utils.log("[ERROR] No mapping for Desk.com Group Name: [" + deskGroupName + "]!");

            // try to get the 'Unassigned' queue id
            if (queueName2Id.containsKey(SalesforceConstants.QueueUnassigned))
            {
                queueId = queueName2Id.get(SalesforceConstants.QueueUnassigned);
            }
        }

        return queueId;
    }

    public DeployResponse createCustomFields(String json) throws Exception
    {
        getSalesforceService().addCustomFields(convertCustomFields(json));
        return getSalesforceService().deploy();
    }

    public DeployResponse createFieldPermissions(String json) throws Exception
    {
        List<Metadata> sfCFs = convertCustomFields(json);

        PermissionSet permissionSet = new PermissionSet();
        permissionSet.setFullName(SalesforceConstants.PERMSET);

        List<PermissionSetFieldPermissions> fieldPermissions = new ArrayList<>();


        for (com.sforce.soap.metadata.Metadata cf : sfCFs)
        {
            fieldPermissions.add(permissionSetFieldPermissionsFromCustomField(cf.getFullName()));
        }

        permissionSet.setFieldPermissions(fieldPermissions.toArray(new PermissionSetFieldPermissions[fieldPermissions.size()]));

        getSalesforceService().addCustomFields(Arrays.asList(permissionSet));
        return getSalesforceService().deploy();
    }

    private static List<com.sforce.soap.metadata.Metadata> convertCustomFields(String json)
    {
        // define the list of custom fields to create using the Salesforce metadata API
        List<com.sforce.soap.metadata.Metadata> sfCFs = new ArrayList<>();

        for (CustomField cf : parseCustomFields(json))
        {
            com.sforce.soap.metadata.CustomField sfCF = deskCustomFieldToSalesforceCustomField(cf);
            sfCFs.add(sfCF);
        }

        return sfCFs;
    }

    private static List<CustomField> parseCustomFields(String json)
    {
        // deserialize the JSON into an object
        Type listType = new TypeToken<List<CustomField>>() {}.getType();
        return (List<CustomField>) JsonUtil.fromJson(json, listType);
    }

    private static com.sforce.soap.metadata.PermissionSetFieldPermissions permissionSetFieldPermissionsFromCustomField(String name)
    {
        PermissionSetFieldPermissions perm = new PermissionSetFieldPermissions();
        perm.setEditable(true);
        perm.setReadable(true);
        perm.setField(name);
        return perm;
    }

    private static com.sforce.soap.metadata.CustomField deskCustomFieldToSalesforceCustomField(CustomField cf)
    {
        // safely cast the custom field
        com.sforce.soap.metadata.CustomField sfCF = new com.sforce.soap.metadata.CustomField();
        
        Utils.log("Type: " + cf.getType());
        
        Utils.log("CF Name: " + cf.getName());

        // get the Salesforce object name by the Desk.com field 'type' (not data type, but object type)
        String sfObjectName = (cf.getType() == CustomFieldType.COMPANY ? "Account"
            : (cf.getType() == CustomFieldType.CUSTOMER ? "Contact" : "Case"));

        // set the full name, which includes object
        sfCF.setFullName(sfObjectName + ".Desk_" + cf.getName() + "__c");

        sfCF.setDescription("Field migrated from Desk.com by Service Cloud Migration Tool.");

        if (cf.getLabel() == null)
        {
            Utils.log("Label is null! Name: " + cf.getName());
        }

        if (cf.getLabel().length() > 40)
        {
            // Label too long, truncate, add full label to description
            sfCF.setLabel(cf.getLabel().substring(0, 39));
            sfCF.setDescription(
                "Field migrated from Desk.com by Service Cloud Migration Tool. Original Label: " + cf.getLabel());
        }
        else
        {
            sfCF.setLabel(cf.getLabel());
        }

        // set the data type
        if (cf.getData().getType() == CustomFieldDataType.BOOLEAN)
        {
            sfCF.setType(FieldType.Checkbox);
            // Must specify 'defaultValue' for a CustomField of type Checkbox FIELD_INTEGRITY_EXCEPTION
            sfCF.setDefaultValue("false");
        }
        else if (cf.getData().getType() == CustomFieldDataType.DATE)
        {
            sfCF.setType(FieldType.DateTime);
        }
        else if (cf.getData().getType() == CustomFieldDataType.INTEGER)
        {
            sfCF.setType(FieldType.Number);
            // Can not specify 'length' for a CustomField of type Number FIELD_INTEGRITY_EXCEPTION
            // Must specify 'scale' for a CustomField of type Number FIELD_INTEGRITY_EXCEPTION
            // using a precision of 10, and a scall of 0 will make this number custom field behave like an integer
            sfCF.setPrecision(10);
            sfCF.setScale(0);
        }
        else if (cf.getData().getType() == CustomFieldDataType.LIST)
        {
            sfCF.setType(FieldType.Picklist);

            // create the picklist and picklist values
            Picklist pl = new Picklist();

            // delcare the list of picklist values
            List<PicklistValue> plvs = new ArrayList<>();

            // loop through the desk choices
            for (String choice : cleanDataChoices(cf.getData().getChoices()))
            {
                PicklistValue plv = new PicklistValue();
                // TODO: 'safe' the choice
                // The fullName can contain only underscores and alphanumeric characters. It must be
                // unique, begin with a letter, not include spaces, not end with an underscore, and not
                // contain two consecutive underscores.
                plv.setFullName(choice);

                // add the value to the list
                plvs.add(plv);
            }

            // assign the picklist values to the picklist
            pl.setPicklistValues(plvs.toArray(new PicklistValue[] { null }));

            // set the picklist settings on the custom field
            sfCF.setPicklist(pl);
        }
        else if (cf.getData().getType() == CustomFieldDataType.STRING)
        {
            sfCF.setType(FieldType.Text);
            sfCF.setLength(255);
        }

        // return the Salesforce custom field
        return sfCF;
    }

    private static HashSet<String> cleanDataChoices(String[] choices) {
        HashSet<String> clean = new HashSet<>();
        HashSet<String> clone = new HashSet<>();

        for (String choice : choices) {
            choice = choice.trim();
            if (!clone.contains(choice.toLowerCase())) {
                clone.add(choice.toLowerCase());
                clean.add(choice);
            }
        }

        return clean;
    }

    public DeployResponse createQueues(String json) throws Exception
    {
        Utils.log("Queue JSON: " + json);
        // define the list of queues to create using the Salesforce metadata API
        List<Queue> sfQs = new ArrayList<>();

        // deserialize the JSON into an object
        Type listType = new TypeToken<List<List<String>>>() {}.getType();
        @SuppressWarnings("unchecked")
        List<List<String>> jsonObj = (List<List<String>>) JsonUtil.fromJson(json, listType);

        // declare the QueueSobject, which determines which object the queue is associated to (Case)
        QueueSobject qso = new QueueSobject();
        qso.setSobjectType("Case");        
        
        getDeskGroupIdAndName();
        Map<Integer, String> deskGroupId2Name = getDeskService().getDeskGroupId2Name();        
        // loop through the list of strings which represents a queue
        for (List<String> queueStringList : jsonObj)
        {
            // create the queue object
            Queue sfQ = new Queue();

            // assign the queue to cases
            sfQ.setQueueSobject(new QueueSobject[] { qso });

            // TODO: For some reason the APEX 'nameToDevName()' method was not enforcing the rule that API Name can't
            // start with a number. So enforce it here with the Java version of the function. We should remove the
            // conversion to API Name in Apex code and just check for existing queues using the queue/group label, and
            // then we do not need to pass this odd 'List<List<String>>' structure, and instead can just pass a list
            // of queue/group labels.

            // set 'Full Name' (API Name), max length is 80 characters
            // queue name must start with a letter, have no spaces, no double underscore
            // must be alphanumeric, no spaces, not end with underscore
            sfQ.setFullName("Desk_" + queueStringList.get(0));

            // ensure the group name exists
            if (!deskGroupId2Name.containsKey(Integer.valueOf(queueStringList.get(0))))
            {
                Utils.log("[ERROR] Desk Group does not exist for Id: [" + queueStringList.get(0) + "]");
                continue;
            }

            // get the desk group name
            String groupName = deskGroupId2Name.get(Integer.valueOf(queueStringList.get(0)));

            // set 'Name' (Label), max length is 40 characters
            sfQ.setName((groupName.length() > 40 ? groupName.substring(0, 40) : groupName));

            // add to list
            sfQs.add(sfQ);
        }

        getSalesforceService().addQueues(sfQs);
        return getSalesforceService().deploy();
    }

    public DeployResponse getDeskGroupMembers(Set<Integer> groupIds, Map<String, String> config) throws Exception
    {
        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<User>> resp = null;
        ApiResponse<User> apiResp = null;

        // declare the list that will be returned
        List<HashMap<String, Object>> recList = new ArrayList<>();

        //deploy Response
        DeployResponse dr = new DeployResponse();
        // get a service
        GroupService service = getDeskClient().groups();

        //get All SFDC users
        String query = String.format("Select %s, %s, %s From %s Where %s != null Or %s = '%s'",
                UserFields.Id, UserFields.DeskId, UserFields.Email, SalesforceConstants.OBJ_USER, UserFields.DeskId,
                UserFields.Email, config.get("user_email"));
        List<SObject> sfUsers = getSalesforceService().query(query);

        String currentUserId = null;
        String unassignedQueue = getSalesforceService().getQueues().get(SalesforceConstants.QueueUnassigned);

        //map deskId to sfdcId
        Map<Integer, String> deskIdToSfdcId = new HashMap<Integer, String>();
        for (SObject u : sfUsers)
        {
            if (u.getField(UserFields.Email).equals(config.get("user_email")))
                currentUserId = (String) u.getField(UserFields.Id);

            if (u.getField(UserFields.DeskId) != null) {
                // better way to not lose precision? Values come back from sfdc as sci notation eg. 2.3091629E7
                int deskId = new BigDecimal(String.valueOf(u.getField(UserFields.DeskId))).intValue();
                deskIdToSfdcId.put(deskId, (String) u.getField(UserFields.Id));
            }
        }

        // unassigned record
        HashMap<String, Object> unassigned = new HashMap<String, Object>();
        unassigned.put(GroupMemberFields.GroupId, unassignedQueue);
        unassigned.put(GroupMemberFields.UserOrGroupId, currentUserId);
        recList.add(unassigned);

        // create bulk job
        String jobId = getSalesforceService().createBulkJob(SalesforceConstants.OBJ_GROUP_MEMBER,
            SalesforceConstants.Fields.Id, OperationEnum.insert);

        updateMigrationStatus(DeskMigrationFields.StatusRunning, "", dr, jobId);

        for (Integer groupId : groupIds)
        {
            // declare the retry flag
            boolean bRetry = false;
            int retryCount = 0;

//            Thread.sleep(1000);

            do
            {
                try
                {
                    // reset the retry flag
                    bRetry = false;

                    // get all users for a particular desk group
                    resp = service.getUsersForGroup(groupId, DESK_PAGE_SIZE_USER).execute();

                    if (resp.isSuccess())
                    {
                        apiResp = resp.body();

                        List<User> deskUsers = apiResp.getEntriesAsList();

                        // convert deskGroupMember to SFDC Queue Member
                        // NOTE JSON Map not used to avoid passing sfUsers by value
                        for (User u : deskUsers)
                        {
                            // make sure we have a value queue id and user id
                            if (getQueueId(groupId) == null || !deskIdToSfdcId.containsKey(u.getId()))
                            {
                                Utils.log(String.format("[ERROR] Group [%d] or User [%d] is missing!", groupId, u.getId()));
                            }
                            else
                            {
                                HashMap<String, Object> groupMember = new HashMap<String, Object>();
                                groupMember.put(GroupMemberFields.GroupId, getQueueId(groupId));
                                groupMember.put(GroupMemberFields.UserOrGroupId, deskIdToSfdcId.get(u.getId()));
                                recList.add(groupMember);
                            }
                        }

                        if (recList.size() >= SalesforceConstants.BULK_MAX_SIZE && !SalesforceConstants.READ_ONLY)
                        {
                            // create the group members
                            dr.addDeployResponse(createGroupMembers(jobId, recList));
                            //update dr success count
                            dr.incrementSuccessCount(recList.size());

                            // clear the records that were bulk inserted
                            recList.subList(0, SalesforceConstants.BULK_MAX_SIZE).clear();
                        }
                    }
                    else
                    {
                        if (resp.code() == 429)
                        {
                            // 'too many requests'
                            // re-queue or retry
                            bRetry = true;
                        }
                        else if (resp.code() == java.net.HttpURLConnection.HTTP_INTERNAL_ERROR)
                        {
                            // when we run imports through the API with threaded requests we'll occasionally get a 500
                            // response and have to retry the request (which succeeds on the retry).
                            // TODO: Retry
                            bRetry = true;
                        }
                        else
                        {
                            throw new Exception(String.format("Error (%d): %s\n%s", resp.code(), resp.message(),
                                resp.errorBody().string()));
                        }
                    }
                }
                catch (Exception e)
                {
                    // retry if we hit a socket timeout exception
                    retryCount++;
                    Utils.log("[EXCEPTION] Retry Attempt: " + retryCount);
                    if (retryCount > 5)
                    {
                        // we retried 5 times, let exception go
                        throw e;
                    }
                    else
                    {
                        bRetry = true;
                    }
                }
            }
            while (bRetry || resp.isSuccess() && apiResp.hasNextPage() && SalesforceConstants.RETRIEVE_ALL);
        }

        if (!recList.isEmpty() && !SalesforceConstants.READ_ONLY)
        {
            dr.addDeployResponse(createGroupMembers(jobId, recList));
            //update dr success count
            dr.incrementSuccessCount(recList.size());
        }

        // close the bulk job
        getSalesforceService().closeBulkJob(jobId);

        updateMigrationStatus(DeskMigrationFields.StatusComplete, "Group Member", dr);

        return dr;
    }

    private DeployResponse createGroupMembers(String jobId, List<HashMap<String, Object>> groupMembers)
    {
        DeployResponse dr = new DeployResponse();
        try
        {
            List<Map<String, Object>> sfRecs = new ArrayList<>();

            int counter = 0;
            for (HashMap<String, Object> gm : groupMembers)
            {
                counter++;
                sfRecs.add(gm);

                // submit bulk job every 10k records
                if ((counter % SalesforceConstants.BULK_MAX_SIZE) == 0)
                {
                    Utils.log("Calling insert Data GroupMembers");
                    getSalesforceService().addBatchToJob(jobId, sfRecs);

                    // clear the list
                    sfRecs.clear();
                }
            }

            // if there are records that still need to be bulk upserted
            Utils.log("Calling insert Data GroupMembers");
            if (!sfRecs.isEmpty())
            {
                getSalesforceService().addBatchToJob(jobId, sfRecs);
            }
        }
        catch (Exception e)
        {
            Utils.logException(e);
        }
        return dr;
    }

  
    public DeployResponse createTopicsFromLabels(String jobId, List<Label> labels)
    {
        DeployResponse dr = new DeployResponse();

        try
        {
            List<Map<String, Object>> sfRecs = new ArrayList<>();
            int counter = 0;
            for (Label label : labels)
            {
                // convert the desk case to the Map for conversion to JSON
                sfRecs.add(deskLabelToSalesforceTopicJsonMap(label));

                // increment the counter
                counter++;

                // submit a bulk job every 10k records
                if ((counter % SalesforceConstants.BULK_MAX_SIZE) == 0)
                {
                    getSalesforceService().addBatchToJob(jobId, sfRecs);

                    // clear the lists
                    sfRecs.clear();
                }
            }

            // check if there are records that still need to be bulk upserted
            if (!sfRecs.isEmpty())
            {
                getSalesforceService().addBatchToJob(jobId, sfRecs);
            }
        }
        catch (Exception e)
        {
            Utils.logException(e);
        }

        return dr;
    }

    public DeployResponse migrateDeskInteractions(Map<String, String> config, Integer startId) throws Exception
    {
        Utils.log("Entered DeskUtil::migrateDeskInteractions()");

        // declare last record id
        int nextRecordId = (startId == null ? 1 : startId);
        
        // initialize a flag which indicates if this is a delta migration
        boolean delta = (startId != null);
        
        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<Interaction>> resp = null;
        ApiResponse<Interaction> apiResp = null;

        // deploy response
        DeployResponse dr = new DeployResponse();
        
        // log to the deploy response the start Id
        dr.addError(String.format("Start Id: [%d]. Use this if the job fails and needs to be restarted (e.g. invalid Session).", startId));
        updateMigrationStatus(DeskMigrationFields.StatusRunning, "Interactions", dr);
        dr = new DeployResponse();
        
        // define the list of object types
        // TODO: Add support for chat, community messages, etc.
        final Set<String> soTypes = new HashSet<String>(Arrays.asList(SalesforceConstants.OBJ_EMAIL_MESSAGE,
            /*SalesforceConstants.OBJ_FEED_ITEM,*/ SalesforceConstants.OBJ_CASE_COMMENT));
        
//        // map of records
//        Map<String, List<SObject>> recLists = new HashMap<>();

        // map of records
        Map<String, List<Map<String, Object>>> recLists = new HashMap<>();
        
        // map of job id's
        Map<String, String> jobIds = new HashMap<>();
        
        // initialize soType specific items
        for (String soType : soTypes)
        {
            // initialize the lists in the map
            recLists.put(soType, new ArrayList<>());

            String jobId = getSalesforceService().createBulkJob(soType, null, OperationEnum.insert);

            // create initial bulk jobs
            jobIds.put(soType, jobId);

            if (soType.equals(SalesforceConstants.OBJ_EMAIL_MESSAGE))
                updateMigrationStatus(DeskMigrationFields.StatusRunning, "", null, jobId);
        }
        
        // get a service
        InteractionService service = getDeskClient().interactions();

        // declare the job management variables
        boolean bRetry = false;
        boolean bRequeue = false;
        int requestCount = 1;
        int retryExCount = 0;
        int retry500Count = 0;
        int retryBulkCount = 0;
        
        Set<Integer> fiveHundreds = new HashSet<Integer>();

        // loop through retrieving records
        do
        {
            try
            {
                // reset the retry flag
                bRetry = false;

//                // DEBUG: Pull only 10 pages of cases
//                if (page > 10)
//                {
//                    break;
//                }

                // retrieve the records synchronously
                resp = service.getInteractions(DESK_PAGE_SIZE_INTERACTION, nextRecordId).execute();

                // check for success
                if (resp.isSuccess())
                {
                    // reset the retry counters
                    retryExCount = 0;
                    retry500Count = 0;

                    // log the Desk.com rate limiting headers
                    DeskUtil.logDeskRateHeaders(resp.headers());

                    // get the response body
                    apiResp = resp.body();
                    
                    // Utils.log("Next URL: " + apiResp.getLinks().getNext().getUrl());
                    // get the since_id from the links
                    Matcher mSinceId = SINCE_ID_PATTERN.matcher(apiResp.getLinks().getNext().getUrl());
                    if (mSinceId.find())
                    {
                        // Utils.log("Match: " + mSinceId.group(1));
                        nextRecordId = Integer.valueOf(mSinceId.group(1));
                    }
                    
                    // convert the desk interaction to Salesforce SObjects
                    // multiple CaseComment objects will be created if we need to split the note body over multiple
                    // CaseComment records
//                     List<SObject> recList = deskInteractionToSalesforceSObjects(apiResp.getEntries(), dr);
                    if(apiResp.getEntries() != null)
                    {
                    	deskInteractionToSalesforceJsonMaps(this, recLists, apiResp.getEntries(), dr);
                    }
                    
//                    // loop through the objects and add to the holder
//                    for (SObject so : recList)
//                    {
//                        // check if the list has been initialized
//                        if (!recLists.containsKey(so.getType()))
//                        {
//                            recLists.put(so.getType(), new ArrayList<>());
//                        }
//                        
//                        // add the record to the appropriate list
//                        recLists.get(so.getType()).add(so);
//                    }
                    
                    String msg = "Response Size: [" + apiResp.getTotalEntries() + "], ";
                    for (String soType : soTypes)
                    {
                        msg += soType + " List Size: [" + recLists.get(soType).size() + "], ";
                    }
                    Utils.log(String.format(msg + "Next Record Id: [%d], Request: [%d]", nextRecordId, requestCount));
                    
                    // every 1k requests, submit all the work and queue a new job with an updated start id 
                    if (requestCount > 1000)
                    {
                        // loop through the object types
                        for (String soType : soTypes)
                        {
                            while(recLists.get(soType).size() > 0 && !SalesforceConstants.READ_ONLY)
                            {
                                // Email Messages will submit a batch every 3k records in an attempt to not hit the 10mb limit
                                int batchMaxSize = (SalesforceConstants.OBJ_EMAIL_MESSAGE.equalsIgnoreCase(soType) ? 3000 :
                                    SalesforceConstants.BULK_MAX_SIZE);
                                
                                // get the upper boundary of the record list
                                int iMax = (recLists.get(soType).size() > batchMaxSize ? batchMaxSize : recLists.get(soType).size());

                                // create the records
                                getSalesforceService().addBatchToJob(jobIds.get(soType), recLists.get(soType).subList(0, iMax));
                                
                                //reset bulkCount
                                retryBulkCount = 0;

                                // clear the records that were inserted
                                recLists.get(soType).subList(0, iMax).clear();
                            }
                        }

                        // update migration status
                        if (!fiveHundreds.isEmpty())
                        {
                            dr.addError("Received HTTP 500 response > 5 times! Id's: " + fiveHundreds.toString());
                            fiveHundreds.clear();
                        }
                        updateMigrationStatus(DeskMigrationFields.StatusRunning, "Interactions", dr);
                        dr = new DeployResponse();
                        
                        // update the start id
                        config.put("start_id", String.format("%d", nextRecordId));
                        
                        // re-queue the job with the updated id
                        RabbitUtil.publishToQueue(RabbitConfiguration.QUEUE_DESK_DATA_MIGRATION,
                            RabbitConfiguration.EXCHANGE_TRACTOR, JsonUtil.toJson(config).getBytes());
                        
                        //flip flag so job will end gracefully, queued job will start
                        bRequeue = true;
                    }

                    // increment the page counter
                    requestCount++;
                }
                else
                {
                    switch(resp.code())
                    {
                        /**
                         * When you've reached the last page of interactions, the next_page link will give you a
                         * since_id that's too high to find any more interactions. We're going to respond with a 422
                         * status the body set to 
                         * {"message":"no records found with id greater than since_id"}
                         * which will let you know when to stop.
                         */
                        // HTTP 422 'Unprocessable Entity'
                        case 422:
                            // do nothing, should exit gracefully
                            break;

                        // check for 'too many requests' response
                        case 429:
                            // get the reset seconds and sleep for that many seconds
                            Thread.sleep(Integer.parseInt(resp.headers().get(DESK_HEADER_LIMIT_RESET)) * 1000);

                            // re-queue or retry
                            bRetry = true;
                            break;

                        // HTTP 500 Server Error
                        case 500:
                            // when we run imports through the API with threaded requests we'll occasionally get a 500
                            // response and have to retry the request (which succeeds on the retry).
                            retry500Count++;
                            Utils.log("[WARN] HTTP 500 Response Count: " + retry500Count);
                            
                            // if we get the 500 response 5 times, increment the id counter
                            if (retry500Count > 5)
                            {
                                // log the page so I can report it to the desk team
                                String msg = String.format("Received HTTP 500 response > 5 times! 'since_id': [%d]",
                                    nextRecordId);
//                                dr.addError(msg);
                                fiveHundreds.add(nextRecordId);
                                Utils.log(msg);
                                
                                // we retried 5 times, increment the record id and try again
                                nextRecordId = nextRecordId + 100;
                                
                                // don't reset the 500 response counter, it gets reset once I get a successful response
                                // this way, as long as the server is returning 500 responses it will be increment by 1
                                // for each response after the first 5 HTTP 500 responses.
                            }
                            
                            // flip the retry flag
                            bRetry = true;
                            
                            // reset the exception count so I don't hit the max and the job dies
                            retryExCount = 0;
                            
                            break;
                        default:
                            Utils.log(resp.headers().toString());
                            throw new Exception(String.format("Error (%d): %s\n%s", resp.code(), resp.message(),
                                resp.errorBody().string()));
                    }
                }
            }
            catch (java.net.SocketTimeoutException e)
            {
                // retry if we hit a socket timeout exception
                retryExCount++;
                Utils.log("[EXCEPTION] Retry Attempt: " + retryExCount);
                //Utils.logException(e);
                if (retryExCount > 10)
                {
                	if (delta)
                	{
                		dr.setResumePoint(nextRecordId);                		                		
                	}
                	else
                	{
                		dr.setResumePoint(nextRecordId);
                	}                	
                	updateMigrationStatus(DeskMigrationFields.StatusFailed, "Interactions", dr);
                	//Utils.sendEmail();
                    // we retried 5 times, let exception go
                    throw e;
                }
                else
                {
                    bRetry = true;
                }
            }
            catch (AsyncApiException e)
            {
                // retry if we hit a socket timeout exception
                retryBulkCount ++;
                Utils.log("Interactions AsyncAPI Issue Start:");
                Utils.log("[EXCEPTION]  Retry Attempt: " + retryBulkCount);
                Utils.log(e.getExceptionMessage());
                e.printStackTrace();
                Utils.log("Interactions AsyncAPI Issue End:");
                //Utils.logException(e);
                if (retryBulkCount > 10)
                {
                	if (delta)
                	{
                		dr.setResumePoint(nextRecordId);                		                		
                	}
                	else
                	{
                		dr.setResumePoint(nextRecordId);
                	}                	
                	updateMigrationStatus(DeskMigrationFields.StatusFailed, "Interactions", dr);
                	//Utils.sendEmail();
                    // we retried 5 times, let exception go
                    throw e;
                }
                else
                {
                    bRetry = true;
                }
            }
        }
        // continue to loop while the request is successful and there are subsequent pages of results
        while (bRetry || (resp.isSuccess() && !bRequeue && apiResp.hasNextPage() && SalesforceConstants.RETRIEVE_ALL));
        
        // loop through the object types
        for (String soType : soTypes)
        {
            while (recLists.get(soType).size() > 0 && !SalesforceConstants.READ_ONLY)
            {
                // Email Messages will submit a batch every 3k records in an attempt to not hit the 10mb limit
                int batchMaxSize = (SalesforceConstants.OBJ_EMAIL_MESSAGE.equalsIgnoreCase(soType) ? 3000
                    : SalesforceConstants.BULK_MAX_SIZE);

                // get the upper boundary of the record list
                int iMax = (recLists.get(soType).size() > batchMaxSize ? batchMaxSize : recLists.get(soType).size());

                // create the records
                getSalesforceService().addBatchToJob(jobIds.get(soType), recLists.get(soType).subList(0, iMax));

                // clear the records that were inserted
                recLists.get(soType).subList(0, iMax).clear();
            }

            // close the current job
            getSalesforceService().closeBulkJob(jobIds.get(soType));
        }

        // update migration status
        if (!fiveHundreds.isEmpty())
        {
            dr.addError("Received HTTP 500 response > 5 times! Id's: " + fiveHundreds.toString());
            fiveHundreds.clear();
        }
        
        dr.addError(String.format("Final Interaction Page migrated [%d]", startId));
        updateMigrationStatus(DeskMigrationFields.StatusComplete, "Interactions", dr);
        dr = new DeployResponse();
        
        // return the deploy response
        return dr;
    }


    public byte[] getDeskAttachment(int caseId, String attachmentUrl)
        throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException,
        InvalidParameterSpecException, NumberFormatException, InterruptedException
    {
        // make sure to strip the trailing '/' from the URL
        String attachmentUrlSigned = getDeskClient().signUrl(
            (attachmentUrl.endsWith("/") ? attachmentUrl.substring(0, attachmentUrl.length() - 1) : attachmentUrl));
        BufferedInputStream in = null;
        ByteArrayOutputStream out = null;
        byte[] ret = null;
        boolean bRetry = false;
        int retryCount = 0;
        try
        {
            do
            {
                // reset retry flag
                bRetry = false;
                
                long startTime = Calendar.getInstance().getTimeInMillis();

                /**
                 * Perform a 'GET' request for the attachment URL on Desk.com, this will return an Amazon S3 URL as the
                 * redirect, however we do not want to follow the redirect. We do not want to follow because the
                 * Desk.com URL requires authentication that would be passed to Amazon and Amazon will reject it.
                 */
                
                // create the URL
                URL url = new URL(attachmentUrlSigned);
                Utils.log(String.format("--> GET %s", attachmentUrlSigned));
                
                // create the connection
                HttpURLConnection connDesk = (HttpURLConnection) url.openConnection();
                
                // do not follow the redirect
                connDesk.setInstanceFollowRedirects(false);
                
                // set special override header
                connDesk.setRequestProperty("x-desk-app", "wowed");
                
                // log the response
                Utils.log(String.format("<-- %s %s %s (%dms, %d-byte body)", url.getProtocol().toUpperCase(),
                    connDesk.getResponseCode(), connDesk.getResponseMessage(),
                    (Calendar.getInstance().getTimeInMillis() - startTime), connDesk.getContentLength()));
                
                // if the response is 'HTTPS 401 Unauthorized' do not log the headers
                if (connDesk.getResponseCode() != 401)
                {
                    DeskUtil.logDeskRateHeaders(connDesk.getHeaderFields());
                }

                // debug statements
                // Utils.log(connDesk.getHeaderFields());
                // Utils.log("HTTP Response Code: " + connDesk.getResponseCode());
                // Utils.log("HTTP Response Message: " + connDesk.getResponseMessage());
                
                // handle HTTP response code
                switch (connDesk.getResponseCode())
                {
                    // check for 'too many requests' response
                    case 429:
                        // get the reset seconds and sleep for that many seconds
                        Utils.log(String.format("[DESK] API Limit Reached. Sleeping for %s seconds.",
                            connDesk.getHeaderField(DESK_HEADER_LIMIT_RESET)));
                        Thread.sleep(Integer.parseInt(connDesk.getHeaderField(DESK_HEADER_LIMIT_RESET)) * 1000);

                        // retry
                        retryCount++;
                        if (retryCount > 5)
                        {
                            Utils.log("Max retry count exceeded. Returning null for attachment content.");
                            return null;
                        }
                        else
                        {
                            bRetry = true;
                        }

                        break;
                    // The 'GET' request will return an HTTP 302 response (Moved Temporarily) when it is successful.
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        // do nothing, this is the expected result
                        break;
                    default:
                        throw new InvalidParameterSpecException(String.format(
                            "Received HTTP Response Code %d (URL: %s)",
                            connDesk.getResponseCode(), attachmentUrlSigned));
                }
                
                // only execute if we are not going to retry
                if (!bRetry)
                {
                    // get the amazon URL from the 'Location' response header
                    String amazonS3Url = connDesk.getHeaderField("Location");
                    
                    // set the start time for the attachment request
                    startTime = Calendar.getInstance().getTimeInMillis();
                    
                    // create the URL
                    URL amazonUrl = new URL(amazonS3Url);
                    Utils.log(String.format("--> GET %s", amazonS3Url));
                    
                    // create the connection
                    HttpURLConnection connAmazon = (HttpURLConnection) amazonUrl.openConnection();

                    // open the input stream with the Amazon S3 URL
                    in = new BufferedInputStream(connAmazon.getInputStream());
    
                    // open the output stream to the file
                    out = new ByteArrayOutputStream();
    
                    // define the byte array
                    final byte buf[] = new byte[1024];
                    int size;
    
                    // read the data in 1024 byte chunks
                    while ((size = in.read(buf, 0, 1024)) != -1)
                    {
                        // write the output data
                        out.write(buf, 0, size);
                    }
    
                    // write the output byte array to the return variable
                    ret = out.toByteArray();

                    // log the response
                    Utils.log(String.format("<-- %s %s %s (%dms, %d-byte body)", url.getProtocol().toUpperCase(),
                        connAmazon.getResponseCode(), connAmazon.getResponseMessage(),
                        (Calendar.getInstance().getTimeInMillis() - startTime), connAmazon.getContentLength()));
                }
            }
            while (bRetry);
        }
        catch (InvalidParameterSpecException e)
        {
            Utils.logException(e);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
        return ret;
    }

    public void migrateDeskAttachments(List<Integer> caseIds) throws Exception
    {
        updateMigrationStatus(DeskMigrationFields.StatusRunning, "Attachments", null);

        // get a service
        CaseService service = getDeskClient().cases();

        // declare page counter
        int page = 1;

        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<Attachment>> resp = null;
        ApiResponse<Attachment> apiResp = null;

        // status object
        DeployResponse dr = new DeployResponse();
        
        // error handling
        boolean bRetry = false;
        int retryCount = 0;

        // list of error message strings
        List<String> errorMessages = new ArrayList<>();

        // loop through the case id's
        for (Integer caseId : caseIds)
        {
            // reset the page counter
            page = 1;

            // loop through retrieving records
            do
            {
                try
                {
                    // reset the error handling flag
                    bRetry = false;
                    
                    // reset the error messages
                    errorMessages.clear();

                    // call the desk api
                    resp = service.getAttachments(caseId, 100, page).execute();

                    if (resp.isSuccess())
                    {
                        // log the Desk.com rate limiting headers
                        DeskUtil.logDeskRateHeaders(resp.headers());
                        
                        // reset the error handling counter
                        retryCount = 0;

                        // increment the page counter
                        page++;

                        // get the ApiResponse (so I can check for 'hasNextPage')
                        apiResp = resp.body();
                        
                        // check if there are results
                        if (apiResp.getTotalEntries() > 0)
                        {
                            // pass the list of attachments to the method that will migrate them
                            errorMessages = createAttachments(apiResp.getEntriesAsList(), dr);
                        }
                        else
                        {
                            errorMessages.add(String.format("Case [%d] has no attachments.", caseId));
                        }
                        
                        // check if there were errors
                        if (errorMessages != null && !errorMessages.isEmpty())
                        {
                            // add the error message to deploy response
                            dr.addErrors(errorMessages);
                        }

                        // update the Desk Message record with the failure
                        getSalesforceService().upsertData(DeskMessageFields.Name,
                            Arrays.asList(getDeskMessageCaseAttachment(caseId, errorMessages)));
                    }
                    else
                    {
                        switch (resp.code())
                        {
                            // not found
                            case 404:
                                // let it continue
                                break;
                            // check for 'too many requests' response
                            case 429:
                                // get the reset seconds and sleep for that many seconds
                                Thread.sleep(Integer.parseInt(resp.headers().get(DESK_HEADER_LIMIT_RESET)) * 1000);

                                // re-queue or retry
                                bRetry = true;
                                break;
                            case 500:
                                // when we run imports through the API with threaded requests we'll occasionally get a
                                // 500
                                // response and have to retry the request (which succeeds on the retry).
                                // TODO: Retry
                                bRetry = true;
                                break;
                            default:
                                Utils.log(resp.headers().toString());
                                throw new Exception(String.format("Error (%d): %s\n%s", resp.code(), resp.message(),
                                    resp.errorBody().string()));
                        }
                    }
                }
                catch (Exception e)
                {
                    // retry if we hit a socket timeout exception
                    retryCount++;
                    Utils.log("[EXCEPTION] Retry Attempt: " + retryCount);
                    if (retryCount > 5)
                    {
                        // we retried 5 times, let exception go
                        throw e;
                    }
                    else
                    {
                        bRetry = true;
                    }
                }
            }
            while (bRetry || (resp.isSuccess() && apiResp.hasNextPage()));
        } // FOR Case Id

        // update the migration status
        updateMigrationStatus(DeskMigrationFields.StatusComplete, "Attachments", dr);
    }

    private static SObject getDeskMessageCaseAttachment(int caseId, List<String> errMessages)
    {
        // create the SObject so we can update it with success/failure
        SObject so = new SObject(SalesforceConstants.OBJ_DESK_MESSAGE);
        so.setField(DeskMessageFields.Name, String.format("%s%d",
            DeskUtil.DESK_MESSAGE_ATTACHMENT_PREFIX, caseId));
        so.setField(DeskMessageFields.Status, DeskMessageFields.StatusNew);

        // check if the insert was successful
        if (errMessages == null || errMessages.isEmpty())
        {
            so.setField(DeskMessageFields.Status, DeskMessageFields.StatusConverted);
        }
        else
        {
            // set status to failure
            so.setField(DeskMessageFields.Status, DeskMessageFields.StatusFailed);
            
            // set the error message
            so.setField(DeskMessageFields.Error, String.join("\n", errMessages));
        }
        
        return so;
    }
    
    public DeployResponse migrateDeskLabels() throws Exception
    {
        // declare page counter
        int page = 0;

        // declare deploy result
        DeployResponse dr = new DeployResponse();

        // declare the response objects at this scope so I can check them in the do/while loop
        Response<ApiResponse<Label>> resp = null;
        ApiResponse<Label> apiResp = null;

        // declare the list that will be returned
        List<Label> recList = new ArrayList<>();

        // get a service
        LabelService service = getDeskClient().labels();

        // create bulk job
        String jobId = getSalesforceService().createBulkJob(SalesforceConstants.OBJ_TOPIC, TopicFields.Name,
            OperationEnum.upsert);

        // loop through retrieving records
        do
        {
            // increment the page counter
            page++;

            // retrieve the records synchronously
            // TODO: replace "language" with actual language?
            resp = service.getLabels(DESK_PAGE_SIZE_LABEL, page).execute();

            // check for success
            if (resp.isSuccess())
            {
                // get the response body
                apiResp = resp.body();

                // add the list of records to the return list
                recList.addAll(apiResp.getEntriesAsList());

                Utils.log("Retrieved [" + recList.size() + "] records. Max is [" + DESK_PAGE_SIZE_CASE + "]");

                // every 10k records, pass to createCases() to bulk upsert them
                if (recList.size() >= SalesforceConstants.BULK_MAX_SIZE && !SalesforceConstants.READ_ONLY)
                {
                    // create the cases
                    dr.addDeployResponse(createTopicsFromLabels(jobId, recList));

                    // clear the records that were bulk inserted
                    recList = recList.subList(SalesforceConstants.BULK_MAX_SIZE, recList.size());
                }
            }
            else
            {
                Utils.log(resp.headers().toString());
                throw new Exception(
                    String.format("Error (%d): %s\n%s", resp.code(), resp.message(), resp.errorBody().string()));
            }
        }
        // continue to loop while the request is successful and there are subsequent pages of results
        while (resp.isSuccess() && apiResp.hasNextPage());

        // process any records over the 10k chunk
        if (!recList.isEmpty() && !SalesforceConstants.READ_ONLY)
        {
            dr.addDeployResponse(createTopicsFromLabels(jobId, recList));
        }

        // close the bulk job
        getSalesforceService().closeBulkJob(jobId);

        // return the list of records
        return dr;
    }
    
   

    public static List<SObject> deskInteractionToSalesforceSObjects(Interaction[] interactions, DeployResponse deployResponse)
    {
        // create the list
        List<SObject> sos = new ArrayList<>();
        
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
                        SObject soEmail = deskInteractionToSalesforceEmail(interaction, deployResponse);
                        
                        // ensure object is not null
                        if (soEmail != null)
                        {
                            sos.add(soEmail);
                        }
                        break;
                    case TWEET:
                    case FACEBOOK_POST:
                    case FACEBOOK_COMMENT:
                    case FACEBOOK_MESSAGE:
                        // TODO: Check if SocialPost is available in the org and use it
                        // convert the interaction
                        SObject soTweet = deskInteractionToSalesforceFeedItem(interaction,
                            (interaction.getInteractionType() == InteractionType.TWEET), deployResponse);
                        
                        // ensure object is not null
                        if (soTweet != null)
                        {
                            sos.add(soTweet);
                        }
                        break;
                    case PHONE_CALL:
                        // convert the interaction
                        sos.addAll(deskInteractionToSalesforceComment(interaction, deployResponse));
                        break;
                    case CHAT_MESSAGE:
                    case COMMUNITY_ANSWER:
                    case COMMUNITY_QUESTION:
                    case UNKNOWN:
                    default:
                        Utils.log(String.format("[WARN] '%s' is not implemented yet!", interaction.getInteractionType()));
                        break;
                }
            }
        }
        return sos;
    }
    
    private static SObject deskInteractionToSalesforceEmail(Interaction interaction, DeployResponse deployResponse)
    {
        // create the SObject
        SObject so = new SObject(SalesforceConstants.OBJ_EMAIL_MESSAGE);
        
        // The addresses that were sent a blind carbon copy of the email.
        if (interaction.getBcc() != null && !interaction.getBcc().isEmpty())
        {
            so.addField(EmailMessageFields.BccAddress, interaction.getBcc());
        }
        
        // The addresses that were sent a carbon copy of the email.
        if (interaction.getCc() != null && !interaction.getCc().isEmpty())
        {
            so.addField(EmailMessageFields.CcAddress, interaction.getCc());
        }
        
        // The address that originated the email.
        if (interaction.getFromAddress() != null)
        {
            so.addField(EmailMessageFields.FromAddress, interaction.getFromAddress());
        }
        
        // The sender’s name.
        if (interaction.getFromName() != null)
        {
            so.addField(EmailMessageFields.FromName, interaction.getFromName());
        }
        
        // Indicates whether the email was sent with an attachment (true) or not (false).
        so.addField(EmailMessageFields.HasAttachment, (interaction.getLinks().getAttachments().getCount() > 0));
        
        // The body of the email in HTML format.
        so.addField(EmailMessageFields.HtmlBody, interaction.getBody());
        
        // Indicates whether the email was received (true) or sent (false).
        so.addField(EmailMessageFields.Incoming, interaction.isIncoming());
        
        // Controls the external visibility of email messages in communities, and is accessible only if the community
        // case feed is enabled. When this field is set to true—its default value—email messages are visible to
        // external users in the case feed. Update is not supported if the record Status is Draft.
        so.addField(EmailMessageFields.IsExternallyVisible, false);
        
        // The date the email was created.
        so.addField(EmailMessageFields.MessageDate, interaction.getCreatedAt());
        
        // Case to which the email is associated.
        SObject soCase = new SObject(SalesforceConstants.OBJ_CASE);
        soCase.addField(CaseFields.DeskId, interaction.getCaseId());
        so.addField(EmailMessageFields.Parent, soCase);
        
        // The status of the email. For example, New, Draft, Unread, Replied, or Sent.
        so.addField(EmailMessageFields.Status, ("sent".equalsIgnoreCase(interaction.getStatus()) ?
            EmailMessageFields.StatusSent : EmailMessageFields.StatusNew));
        
        // The subject line of the email.
        so.addField(EmailMessageFields.Subject, interaction.getSubject());
        
        // The address of the email’s recipient.
        so.addField(EmailMessageFields.ToAddress, interaction.getTo());
        
        return so;
    }
    
    private static SObject deskInteractionToSalesforceFeedItem(Interaction interaction, boolean isTweet,
        DeployResponse deployResponse)
    {
        // create the SObject
        SObject so = new SObject(SalesforceConstants.OBJ_FEED_ITEM);
        
        // audit fields
        so.addField(CaseCommentFields.CreatedDate, interaction.getCreatedAt());
        so.addField(CaseCommentFields.LastModifiedDate, interaction.getUpdatedAt());
        
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
        so.addField(FeedItemFields.Body, socialInfo + interaction.getBody());
        
        // Indicates whether the feed item Body contains rich text. Set IsRichText to true if you post a rich text feed
        // item via the SOAP API. Otherwise, the post is rendered as plain text.
        so.addField(FeedItemFields.IsRichText, false);
        
        // The type of FeedItem. Except for ContentPost, LinkPost, and TextPost, don’t create FeedItem types directly
        // from the API.
        // SocialPost—generated when a social post is created from a case.
        so.addField(FeedItemFields.Type, "SocialPost");
        
        // Case to which the email is associated.
        SObject soCase = new SObject(SalesforceConstants.OBJ_CASE);
        soCase.addField(CaseFields.DeskId, interaction.getCaseId());
        so.addField(EmailMessageFields.Parent, soCase);
        
        return so;
    }
    
    private static List<SObject> deskInteractionToSalesforceComment(Interaction interaction, DeployResponse deployResponse)
    {
        // create the list
        List<SObject> sos = new ArrayList<>();
        
        // create the SObject
        SObject so = new SObject(SalesforceConstants.OBJ_CASE_COMMENT);
        
        // flag which indicates the note body was truncated
        boolean bTruncated = false;
        
        // audit fields
        so.addField(CaseCommentFields.CreatedDate, interaction.getCreatedAt());
        so.addField(CaseCommentFields.LastModifiedDate, interaction.getUpdatedAt());
        
        // check the body for max length
        String body = interaction.getBody();
        if (body == null)
        {
            // do nothing
        }
        else
        {
            // trim the body if it is longer than max
            if (body.length() > SalesforceConstants.MED_TEXT_MAX)
            {
                String msg = String.format("Body for interaction [%d] is too long and will be truncated! Length: [%d], Max Length: [%d]",
                    interaction.getId(), body.length(), SalesforceConstants.MED_TEXT_MAX);
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
                
                // json encode the shortened body so I can check the length
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
                interaction.setBody(interaction.getBody().substring(body.length() - 1));
            }
        }
        so.addField(CaseCommentFields.CommentBody, body);
        so.addField(CaseCommentFields.IsPublished, false);
        
        // Case to which the email is associated.
        SObject soCase = new SObject(SalesforceConstants.OBJ_CASE);
        soCase.addField(CaseFields.DeskId, interaction.getCaseId());
        so.addField(EmailMessageFields.Parent, soCase);
        
        sos.add(so);
        
        // check if I truncated the body
        if (bTruncated)
        {
            // recursively call this method to add another case comment with the rest of the note
            sos.addAll(deskInteractionToSalesforceComment(interaction, deployResponse));
        }
        
        return sos;
    }

    public List<String> createAttachments(List<Attachment> attachments, DeployResponse dr) throws UnexpectedErrorFault
    {
        List<SaveResult> saveResults = new ArrayList<>();
        List<String> errMessages = new ArrayList<String>();
        int requestFileSize = 0;
        try
        {
            // empty check
            if (attachments == null || attachments.isEmpty())
            {
                Utils.log("[WARN] Empty list of attachments passed to DeskCaseUtil::createAttachments()!");
                return null;
            }

            // list of salesforce attachments
            List<SObject> sfAttachments = new ArrayList<>();

            // loop through the desk attachments and calculate the encoded size
            for (Attachment a : attachments)
            {
                // add the size of the current attachment to the total to determine if we are over limit
                // http://stackoverflow.com/questions/4715415/base64-what-is-the-worst-possible-increase-in-space-usage
                // http://stackoverflow.com/questions/13378815/base64-length-calculation
                // Base64 encodes each set of three bytes into four bytes. In addition the output is padded to always be
                // a multiple of four. This means that the size of the base-64 representation of a string of size n is:
                // ceil(n / 3) * 4
                requestFileSize += Math.ceil(a.getSize() / 3) * 4;
                
                // check file size limit (account for base64 encoding overhead)
                if (requestFileSize > (SalesforceConstants.MAX_SIZE_REQUEST))
                {
                    String errMessage = String.format(
                        "Case (%d) attachments ecoded size (%d) exceed maximum request size (%d).",
                        a.getCaseId(), requestFileSize, SalesforceConstants.MAX_SIZE_REQUEST);
                    errMessages.add(errMessage);
                    Utils.log("[ERROR] " + errMessage);
                    break;
                }
            }
            
            Utils.log("Estimated Encoded Request File Size: " + requestFileSize);

            // found an error converting attachments
            if (!errMessages.isEmpty())
            {
                return errMessages;
            }

            // loop through the desk attachments
            for (Attachment a : attachments)
            {
                // add to the list to be inserted
                sfAttachments.add(deskAttachmentToSalesforceAttachment(a, dr, errMessages));
            }
            
            // found an error converting attachments
            if (!errMessages.isEmpty())
            {
                return errMessages;
            }

            // Utils.log(String.format("Inserting [%d] Attachments", sfAttachments.size()));

            // while we have records to migrate
            while (!sfAttachments.isEmpty())
            {
                // determine the size of the request
                int iMax = (sfAttachments.size() > SalesforceConstants.API_MAX_SIZE ? SalesforceConstants.API_MAX_SIZE
                    : sfAttachments.size());

                // clear the save results
                saveResults.clear();
                
                try
                {
                    // insert the records
                    dr.addDeployResponse(getSalesforceService().insertData(sfAttachments.subList(0, iMax), true, saveResults));
                }
                catch (UnexpectedErrorFault e)
                {
                    // check if the exception is for invalid session
                    if (e.getExceptionCode() == ExceptionCode.INVALID_SESSION_ID)
                    {
                        // let the exception throw
                        throw e;
                    }
                    // set error message to '[case id] (exception code) exception message'
                    errMessages.add(String.format("[%d] (%s) %s", attachments.get(0).getCaseId(),
                        e.getExceptionCode().name(), e.getExceptionMessage()));
                }

                // loop through the records we just inserted and mark the corresponding Desk Message record as migrated
                for (int i = 0; i < iMax; i++)
                {
                    // make sure there is a case id (this should never been blank!
                    if (attachments.get(i).getCaseId() == Attachment.NO_ID)
                    {
                        Utils.log("[ERROR] Case Id is 0 for an attachment!");
                    }
                    else
                    {
                        if (!saveResults.get(i).getSuccess())
                        {
                            // set error message
                            for (com.sforce.soap.partner.Error err : saveResults.get(i).getErrors())
                            {
                                errMessages.add(String.format("[%d] %s (%s)%s\n", attachments.get(0).getCaseId(),
                                    err.getMessage(), err.getStatusCode().name(),
                                    (err.getFields() == null || err.getFields().length == 0 ? "" :
                                        " [Fields: " + StringUtils.join(", ", err.getFields()) + "]")));
                            }
                        }
                    }
                }

                // clear the migrated records from the list
                sfAttachments.subList(0, iMax).clear();
            }
        }
        catch (UnexpectedErrorFault e)
        {
            // let the exception throw
            throw e;
        }
        catch (Exception e)
        {
            Utils.logException(e);
            dr.addError(Utils.excetionToString(e));
        }
        return errMessages;
    }
    
    private SObject deskAttachmentToSalesforceAttachment(Attachment a, DeployResponse dr, List<String> errMessages)
        throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException
    {
        // create the attachment object and set some fields
        SObject sfAttachment = new SObject(SalesforceConstants.OBJ_ATTACHMENT);
        sfAttachment.setField(AttachmentFields.Name, a.getFileName());
        sfAttachment.setField(AttachmentFields.IsPrivate, false);
        sfAttachment.setField(AttachmentFields.ContentType, a.getContentType());
        // Utils.log("ContentType " + a.getContentType());

        // check if the attachment is associated to a case and set the Parent of the attachment to the Case (using
        // DeskID External ID field)
        // TODO: if the attachment is not associated to a case, isn't that an error?
        if (a.getCaseId() != Attachment.NO_ID)
        {
            SObject sfCase = new SObject(SalesforceConstants.OBJ_CASE);
            sfCase.setField(CaseFields.DeskId, a.getCaseId());
            sfAttachment.setField(AttachmentFields.Parent, sfCase);
        }

        // check if the attachment is associated to a user and set the owner of the attachment
        if (a.getUserId() != Attachment.NO_ID)
        {
            SObject sfUser = new SObject(SalesforceConstants.OBJ_USER);
            sfUser.setField(UserFields.DeskId, a.getUserId());
            sfAttachment.setField(AttachmentFields.Owner, sfUser);
        }

        try
        {
            // get the file attachment bytes
            byte[] attachmentBytes = getDeskAttachment(a.getCaseId(), a.getUrl());
            
            // set the 'Body' field to the Base64 encoded contents of the file
            sfAttachment.setField(AttachmentFields.Body, attachmentBytes);
        }
        catch (Exception e)
        {
            String errMessage = String.format("%s: %s", e.getClass().getName(), e.getMessage());
            errMessages.add(errMessage);
            Utils.logException(e);
            dr.addError(String.format("[Case Id: %d] %s", a.getCaseId(), errMessage));
        }

        return sfAttachment;
    }
    
    public void updateMigrationStatus(String status, String stage, DeployResponse dr)
    {
        updateMigrationStatus(status, stage, dr, null);
    }

    public void updateMigrationStatus(String status, String stage, DeployResponse dr, String jobId)
    {
        try
        {
            SObject deskMigration = new SObject(SalesforceConstants.OBJ_DESK_MIGRATION);
            deskMigration.setId(getDeskService().getMigrationId());
    
            // check if we need to increment the counters & append to log
            if (dr != null)
            {
                // retrieve the existing counts so I can increment them
                String query = String.format("SELECT %s, %s, %s FROM %s WHERE %s = '%s'", DeskMigrationFields.RecordsFailed,
                    DeskMigrationFields.RecordsMigrated, DeskMigrationFields.Log, SalesforceConstants.OBJ_DESK_MIGRATION,
                    DeskMigrationFields.ID, getDeskService().getMigrationId());
                List<SObject> results = getSalesforceService().query(query);
    
                // check if an existing migration record was found
                if (results == null || results.isEmpty())
                {
                    // we are in big trouble if we get here
                    throw new InvalidParameterException(
                        "Could not find existing Desk Migration record with Id: [" + getDeskService().getMigrationId() + "]!");
                }
    
                SObject prev = results.get(0);
    
                Double recordsMigrated = (prev.getField(DeskMigrationFields.RecordsMigrated) == null ? 0
                    : Double.valueOf((String) prev.getField(DeskMigrationFields.RecordsMigrated)));
                Double recordsFailed = (prev.getField(DeskMigrationFields.RecordsFailed) == null ? 0
                    : Double.valueOf((String) prev.getField(DeskMigrationFields.RecordsFailed)));
    
                // increment counters
                deskMigration.setField(DeskMigrationFields.RecordsMigrated,
                    recordsMigrated.intValue() + dr.getSuccessCount());
                deskMigration.setField(DeskMigrationFields.RecordsFailed, recordsFailed.intValue() + dr.getErrorCount());
                deskMigration.setField(DeskMigrationFields.RecordsTotal,
                    recordsMigrated.intValue() + dr.getSuccessCount() + recordsFailed.intValue() + dr.getErrorCount());
    
                if (dr.getErrors() != null && !dr.getErrors().isEmpty())
                {
                    StringBuilder sbErrors = new StringBuilder();
    
                    sbErrors.append("[" + _dateFormat.format(Calendar.getInstance().getTime()) + "]: ");
                    for (String err : dr.getErrors())
                    {
                        sbErrors.append(err + "\n");
                        // Utils.log(err);
                    }
    
                    // check if there are previous log messages to pre-pend
                    if (prev.getField(DeskMigrationFields.Log) != null)
                    {
                        sbErrors.append(prev.getField(DeskMigrationFields.Log));
                    }
    
                    // trim to max length
                    if (sbErrors.length() > SalesforceConstants.LONG_TEXT_MAX)
                    {
                        sbErrors.setLength(SalesforceConstants.LONG_TEXT_MAX);
                    }
    
                    // append errors to log
                    deskMigration.setField(DeskMigrationFields.Log, sbErrors.toString());
                }
            }
    
            deskMigration.setField(DeskMigrationFields.Status, status);
    
            // set stage if provides
            if (stage != null)
            {
                deskMigration.setField(DeskMigrationFields.Stage, stage);
            }

            if (jobId != null) {
                deskMigration.setField(DeskMigrationFields.JobId, jobId);
            }
    
            // TODO: Set this with a workflow rule
            if (status == DeskMigrationFields.StatusComplete)
            {
                deskMigration.setField(DeskMigrationFields.EndDate, java.util.Calendar.getInstance());
            }
            
            if (status == DeskMigrationFields.StatusFailed)
            {
//            	deskMigration.setField(DeskMigrationFields.EndDate, java.util.Calendar.getInstance());
//            	deskMigration.setField(DeskMigrationFields.ResumePoint, dr.getResumePoint());
            	
            }
            
    
            // upsert the migration status
            getSalesforceService().upsertData(DeskMigrationFields.ID, Arrays.asList(deskMigration));
        }
        catch (UnexpectedErrorFault e)
        {
            Utils.log(String.format("[%s] %s", e.getExceptionCode().name(), e.getExceptionMessage()));
        }
        catch (AsyncApiException e)
        {
            Utils.log(String.format("[%s] %s", e.getExceptionCode().name(), e.getExceptionMessage()));
        }
        catch (Exception e)
        {
            Utils.log(String.format("[EXCEPTION] %s", e.getMessage()));
        }
    }

    public static void logDeskRateHeaders(Headers headers)
    {
        logDeskRateHeaders(
            headers.get(DESK_HEADER_LIMIT_LIMIT),
            headers.get(DESK_HEADER_LIMIT_REMAINING),
            headers.get(DESK_HEADER_LIMIT_RESET));
    }
    
    public static void logDeskRateHeaders(Map<String, List<String>> headers)
    {
        logDeskRateHeaders(
            (headers.containsKey(DESK_HEADER_LIMIT_LIMIT) && headers.get(DESK_HEADER_LIMIT_LIMIT) != null ?
                String.join(", ", headers.get(DESK_HEADER_LIMIT_LIMIT)) : ""),
            (headers.containsKey(DESK_HEADER_LIMIT_REMAINING) && headers.get(DESK_HEADER_LIMIT_REMAINING) != null ?
                String.join(", ", headers.get(DESK_HEADER_LIMIT_REMAINING)) : ""),
            (headers.containsKey(DESK_HEADER_LIMIT_RESET) && headers.get(DESK_HEADER_LIMIT_RESET) != null ?
                String.join(", ", headers.get(DESK_HEADER_LIMIT_RESET)) : ""));
    }
    
    private static void logDeskRateHeaders(String limit, String remaining, String reset)
    {
        Utils.log(String.format("[DESK] Limit: [%s] Remaining: [%s] Reset: [%s]",
            limit, remaining, reset));
    }

    public boolean getAuditFieldsEnabled()
    {
        // TODO Auto-generated method stub
        return getSalesforceService().getAuditFieldsEnabled();
    }
}
