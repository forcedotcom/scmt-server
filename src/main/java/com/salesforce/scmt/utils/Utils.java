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

import static java.lang.System.getenv;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spark.Request;

public final class Utils
{
    /**
     * Private constructor for utility class.
     */
    private Utils()
    {
    }

    /**
     * Reads an environment variable, or throws an exception if it is not defined.
     * 
     * @param name
     *            The environment variable name to read.
     * @return The environment variable value.
     */
    public static String getEnvOrThrow(String name)
    {
        final String env = getenv(name);
        if (env == null)
        {
            throw new IllegalStateException("Environment variable [" + name + "] is not set.");
        }
        return env;
    }

    public static void log(String msg)
    {
        System.out.println(msg);
        System.out.flush();
    }

    public static void logException(Exception e)
    {
        e.printStackTrace(System.err);
        System.err.flush();
    }

    public static String excetionToString(Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Convert a list of double's to a list of int's. After deserializing the JSON, lists of integer appear as lists of
     * doubles. Casting to Integer produced entries like '5.0', so explicitly parse to Integer.
     * 
     * @param doubles
     *            List of doubles to convert.
     * @return The list of integer values from the list of doubles.
     */
    public static List<Integer> listDoubleToListInteger(List<Double> doubles)
    {
        List<Integer> integers = new ArrayList<>();

        // null & empty check
        if (doubles == null || doubles.isEmpty())
        {
            // return empty list
            return integers;
        }

        // loop through the doubles
        for (Double d : doubles)
        {
            // add the integer value to the list of integers
            integers.add(d.intValue());
        }

        return integers;
    }

    /**
     * Returns a string truncated by length in bytes, not characters (e.g. UTF-8 multi-byte characters).
     * @param input The input string that will be truncated to the specified byte length.
     * @param byteLength The length in bytes to trim the string to.
     * @return The string that has been truncated to the <= to the specified max byte length.
     * @throws UnsupportedEncodingException
     */
    public static String truncateStringInBytes(String input, int byteLength) throws UnsupportedEncodingException
    {
        // assume the longest length of a UTF8 character is 4 bytes
        // see https://en.wikipedia.org/wiki/UTF-8#Description for more information
        double MAX_UTF8_CHAR_LENGTH = 4.0;
        
        // check if the input length is longer than the max length
        // (no sense calculating byte length against characters we know are beyond the max length we want)
        if (input.length() > byteLength)
        {
            // truncate the input to the max length specified
            input = input.substring(0, byteLength);
        }
        
        // check if the length is greater than 4, if it is not, we can assume this is not multi-byte UTF-8 characters
        if (input.length() > byteLength / MAX_UTF8_CHAR_LENGTH)
        {
            // get the difference in byte length to max length
            int overage = input.getBytes("UTF-8").length - byteLength;
            
            // check if the byte length is longer than the specified length 
            if (overage > 0)
            {
                // initialize integers for temporary overage, start position, and end position
                int tmpOverage = overage, start, end = input.length();
                
                // loop while we still have overage
                while (tmpOverage > 0)
                {
                    // find the start position by dividing overage bytes by UTF8 max byte length, and subtracting that
                    // from the character length of the input
                    start = end - ((int) Math.ceil((double) tmpOverage / MAX_UTF8_CHAR_LENGTH));
                    
                    // update the temporary overage subtracting the byte length of what we are going to trim
                    tmpOverage = tmpOverage - input.substring(start, end).getBytes("UTF-8").length;
                    
                    // update the end position
                    end = start;
                }
                
                // truncate the string to the number of characters that will no exceed the maximum length in bytes
                input = input.substring(0, end);
            }
        }
        
        // return the string that has been truncated by the specified number of bytes
        return input;
    }
    
    /**
     * Retrieves the post parameters from a 'Spark' request, and enforces required parameters.
     * 
     * @param req
     *            The 'Spark' request object.
     * @param requiredParams
     *            An array of parameter names that are required.
     * @return A map of post parameters from the request.
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> getPostParamsFromRequest(Request req, String[] requiredParams)
        throws UnsupportedEncodingException
    {
        // declare the response map
        Map<String, String> postParams = getPostParamsFromRequest(req);

        // check for required post data
        for (String param : requiredParams)
        {
            // Utils.log(String.format("Param: [%s], Value: [%s]", param, postParams.get(param)));

            // check if it is empty
            if (!postParams.containsKey(param) || postParams.get(param) == null
                || postParams.get(param).trim().length() == 0)
            {
                throw new IllegalArgumentException(String.format("'%s' is required but was empty!", param));
            }
        }

        // return the post parameters
        return postParams;
    }

    /**
     * Retrieves the post parameters from a 'Spark' request.
     * 
     * @param req
     *            The 'Spark' request object.
     * @return A map of post parameters from the request.
     */
    public static Map<String, String> getPostParamsFromRequest(Request req) throws UnsupportedEncodingException
    {
        // declare the response map
        Map<String, String> postParams = new TreeMap<>();

        // get the post parameters from the request body
        Map<String, Object> postParamsRaw = asMap(req.body());

        // Utils.log("Post Params: " + postParamsRaw);

        // convert from Map<String,Object> to Map<String, String>
        for (String key : postParamsRaw.keySet())
        {
            postParams.put(key, String.valueOf(postParamsRaw.get(key)));
        }

        // return the post parameters
        return postParams;
    }

    public static Map<String, Object> asMap(String urlencoded) throws UnsupportedEncodingException
    {
        return asMap(urlencoded, "UTF-8");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(String urlencoded, String encoding) throws UnsupportedEncodingException
    {

        Map<String, Object> map = new LinkedHashMap<>();

        for (String keyValue : urlencoded.trim().split("&"))
        {

            String[] tokens = keyValue.trim().split("=");
            String key = tokens[0];
            String value = tokens.length == 1 ? null : URLDecoder.decode(tokens[1], encoding);

            String[] keys = key.split("\\.");
            Map<String, Object> pointer = map;

            for (int i = 0; i < keys.length - 1; i++)
            {

                String currentKey = keys[i];
                Map<String, Object> nested = (Map<String, Object>) pointer.get(keys[i]);

                if (nested == null)
                {
                    nested = new LinkedHashMap<>();
                }

                pointer.put(currentKey, nested);
                pointer = nested;
            }

            pointer.put(keys[keys.length - 1], value);
        }

        return map;
    }

}
