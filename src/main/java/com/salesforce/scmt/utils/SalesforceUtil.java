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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.salesforce.scmt.service.SalesforceService;
import com.salesforce.scmt.utils.SalesforceConstants.GroupFields;
import com.sforce.soap.partner.sobject.SObject;

public final class SalesforceUtil
{

    /**
     * Map where the key is the language code in Salesforce, and the value is the language name/label.
     */
    public static final Map<String, String> SupportedLanguages;
    static // initialize the map
    {
        SupportedLanguages = new HashMap<>();
        SupportedLanguages.put("ar", "Arabic");
        SupportedLanguages.put("cs", "Czech");
        SupportedLanguages.put("da", "Danish");
        SupportedLanguages.put("de", "German");
//        SupportedLanguages.put("de_CH", "German (Switzerland)"); // Platform-Only
        SupportedLanguages.put("el", "Greek");
        SupportedLanguages.put("en_US", "English");
//        SupportedLanguages.put("en_AU", "English (Australia)"); // Platform-Only
//        SupportedLanguages.put("en_CA", "English (Canada)"); // Platform-Only
        SupportedLanguages.put("en_GB", "English (United Kingdom)");
//        SupportedLanguages.put("en_HK", "English (Hong Kong)"); // Platform-Only
//        SupportedLanguages.put("en_IE", "English (Ireland)"); // Platform-Only
//        SupportedLanguages.put("en_SG", "English (Singapore)"); // Platform-Only
        SupportedLanguages.put("en_US", "English (US)");
//        SupportedLanguages.put("en_ZA", "English (South Africa)"); // Platform-Only
        SupportedLanguages.put("es", "Spanish");
        SupportedLanguages.put("es", "Spanish (Spain)");
        SupportedLanguages.put("es_MX", "Spanish (Mexico)");
//        SupportedLanguages.put("es_US", "Spanish (United States)"); // Platform-Only
        SupportedLanguages.put("fi", "Finnish");
//        SupportedLanguages.put("tl", "Tagalog"); // Platform-Only
        SupportedLanguages.put("fr", "French");
//        SupportedLanguages.put("fr_CA", "French (Canada)"); // Platform-Only
        SupportedLanguages.put("fr", "French (France)");
        SupportedLanguages.put("iw", "Hebrew");
//        SupportedLanguages.put("hi", "Hindi"); // Platform-Only
        SupportedLanguages.put("hu", "Hungarian");
        SupportedLanguages.put("in", "Indonesian");
        SupportedLanguages.put("it", "Italian");
        SupportedLanguages.put("ja", "Japanese");
        SupportedLanguages.put("ko", "Korean");
//        SupportedLanguages.put("ms", "Malay"); // Platform-Only
        SupportedLanguages.put("nl_NL", "Dutch");
        SupportedLanguages.put("no", "Norwegian");
        SupportedLanguages.put("pl", "Polish");
        SupportedLanguages.put("pt_PT", "Portuguese");
        SupportedLanguages.put("pt_BR", "Portuguese (Brazil)");
        SupportedLanguages.put("ro", "Romanian (Romania)");
        SupportedLanguages.put("ru", "Russian");
        SupportedLanguages.put("sl", "Slovenian");
        SupportedLanguages.put("sv", "Swedish");
        SupportedLanguages.put("th", "Thai");
        SupportedLanguages.put("tr", "Turkish");
        SupportedLanguages.put("zh_CN", "Chinese (Simplified)");
        SupportedLanguages.put("zh_TW", "Chinese (Traditional)");
    }
    
    
    
    /**
     * Private constructor for utility class.
     */
    private SalesforceUtil() {}

    public static String nameToDevName(String name, int maxLength)
    {
//        Utils.log("nameToDevName() entered: " + name);
        // The name used as a unique identifier for API access.
        // The 'Developer Name' can contain only underscores and alphanumeric characters.
        // It must be unique, begin with a letter, not include spaces, not end with an underscore, and not contain two
        // consecutive underscores.

        // enforce alphanumeric and no consecutive underscores
        String devName = name
            // ensure value only contains alphanumeric characters, all others are replaced with underscore
            .replaceAll("[^a-zA-Z\\d]", "_")
            // do not allow consecutive underscores
            .replaceAll("(_)\\1+", "_");

        // begin with a letter
        if (devName.matches("^[^a-zA-Z].*$"))
        {
            devName = "A" + devName;
        }

        // ensure field is not longer than max length
        if (devName.length() > maxLength)
        {
            devName = devName.substring(0, maxLength);
        }

//        Utils.log("Name after replaceAll(): " + devName);

        // not end with an underscore
        while (devName.endsWith("_"))
        {
            devName = devName.substring(0, devName.length() - 1);
        }

//        Utils.log("Name strip trailing underscore: " + devName);

        // return the developer name
        Utils.log(String.format("nameToDevName() [%s] => [%s]", name, devName));
        return devName;
    }
    
    public static String nameToDevName(String name)
    {
        // convert the name to a dev name, use a large value for max length
        // are there API Name fields longer than 255 characters?
        return nameToDevName(name, 255);
    }
    
    public static Map<String, String> getQueueName2Id(SalesforceService service) throws Exception
    {
    	// initialize the map
        Map<String, String> _QueueName2Id = new HashMap<>();
        
        // thread-safe singleton
        synchronized (SalesforceUtil.class)
        {
            
                                
                // query for the queue names & ids
                List<SObject> queues = service.query("SELECT Id, DeveloperName FROM Group WHERE Type = 'Queue'");

                // loop through the queues and put them into the map
                for (SObject queue : queues)
                {
                    // Key = Name, Value = Id
                    _QueueName2Id.put((String) queue.getField(GroupFields.DeveloperName),
                        (String) queue.getField(GroupFields.Id));
                }                                
            
        }
        // return the map
        return _QueueName2Id;

    }
    
    /**
     * Convert a DateTime String into a Salesforce API compatible format
     * Use the yyyy-MM-ddTHH:mm:ss.SSS+/-HHmm or yyyy-MM-ddTHH:mm:ss.SSSZ 
     * formats to specify dateTime fields.
     * https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/datafiles_date_format.htm
     * @param date The incoming date value.
     * @return The date formatted as a string acceptable to Salesforce.
     */
    public static String sfdcDateTimeFormat(Date incomingDateTime)
    {
        try
        {
            // Incoming: 'Wed Sep 09 11:20:05 CDT 2015'
            // Outgoing: '2015-09-09T11:20:05.000Z'
            SimpleDateFormat sdf = new SimpleDateFormat(SalesforceConstants.DATE_FORMAT, Locale.ROOT);
            return sdf.format(incomingDateTime);
        }
        catch (Exception e)
        {
            // TODO: Save errors and put into migration status record
            return null;
        }
    }
    
    /**
     * Converts a date to the Salesforce date/time string value.
     * @param format The string format of the incoming date.
     * @param date The string value of the incoming date.
     * @return The date formatted as a string acceptable to Salesforce.
     */
    public static Calendar sfdcDateTimeFormat(String format, String date)
    {
        try
        {
            DateFormat df = new SimpleDateFormat(format, Locale.ROOT);
            Date d = df.parse(date);
            Calendar c = new GregorianCalendar();
            c.setTime(d);
            return c;
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // if the date fails to parse, return null
        return null;
    }
}
