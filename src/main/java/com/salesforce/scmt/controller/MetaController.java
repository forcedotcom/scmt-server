/**
 * Copyright (c) 2017, Salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */


package com.salesforce.scmt.controller;

import static spark.Spark.post;

import com.salesforce.scmt.service.DeskService;
import com.salesforce.scmt.utils.JsonTransformer;

public class MetaController
{
    private static final String PREFIX_URI = "/meta";

    public MetaController()
    {
        post(PREFIX_URI + "/remotesite", (req, res) -> { return SalesforceService.createRemoteSite(req, res); }, new JsonTransformer() );
        
    }
}