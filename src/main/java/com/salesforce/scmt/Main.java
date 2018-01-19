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

package com.salesforce.scmt;

//import java.sql.*;
//import java.util.HashMap;
//import java.util.ArrayList;
//import java.util.Map;

import static spark.Spark.*;

import java.io.IOException;


import static com.salesforce.scmt.utils.JsonUtil.toJson;

import com.salesforce.scmt.model.ErrorResponse;
import com.salesforce.scmt.utils.Utils;

//import com.heroku.sdk.jdbc.DatabaseUrl;

public final class Main
{
    /**
     * Private constructor for program start.
     */
    private Main() {}

    public static void main(String[] args)
    {
        /**
         * By default, Spark runs on port 4567. If you want to set another port use port. This has to be done before
         * using routes and filters:
         */
        port(Integer.valueOf(System.getenv("PORT")));

        /**
         * You can assign a folder in the classpath serving static files with the staticFileLocation method. Note that
         * the public directory name is not included in the URL.
         * A file /public/css/style.css is made available as http://{host}:{port}/css/style.css
         */
        staticFileLocation("/public");

        // Before-filters are evaluated before each request, and can read the request and read/modify the response.
        before((request, response) -> {
            // default the response type to JSON
            response.type("text/json");

            // set the 'Access-Control-Allow-Origin' for CORS
            response.header("Access-Control-Allow-Origin", "*");

            // set the 'X-XSS-Protection'
            response.header("X-XSS-Protection", "1");

            //set X-Frame-Options to DENY
            response.header("X-Frame-Options", "DENY");

            response.header("X-Content-Type-Options", "nosniff");

            response.header("Pragma", "no-cache");

            response.header("Expires", "0");

            // force to https
            System.out.println(request.headers("x-forwarded-proto"));
        	if(!"https".equalsIgnoreCase(request.headers("x-forwarded-proto"))){

        		// allow local development without ssl
        		if(!"127.0.0.1".equalsIgnoreCase(request.ip()))
        		{
        			throw new UnsupportedOperationException("only http alllowd");
        		}

        	}
        });

        after((request, response) -> {
            response.header("Cache-Control", "must-revalidate,no-cache,no-store,private");
});

        /**
         * Register an exception handler when an 'IllegalArgumentException' is thrown.
         */
        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ErrorResponse(e)));
            });

        /**
         * Register an exception handler when an 'IOException' is thrown.
         */
        exception(IOException.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ErrorResponse(e)));
            });
        
        /**
         * Register an exception handler when an 'Exception' is thrown.
         */
        exception(UnsupportedOperationException.class, (e, req, res) -> {
            res.status(403);
            res.body(toJson(new ErrorResponse(e)));
            });

        /**
         * Register an exception handler when an 'Exception' is thrown.
         */
        exception(Exception.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ErrorResponse(e)));
            });



        // add the desk end-points
        new com.salesforce.scmt.controller.DeskController();
        new com.salesforce.scmt.controller.MetaController();
    }
}
