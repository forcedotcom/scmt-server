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

package com.salesforce.scmt.worker;

import com.salesforce.scmt.utils.Utils;
import com.salesforce.scmt.model.DeployException;
import com.salesforce.scmt.service.SalesforceService;
import com.sforce.async.AsyncApiException;
import com.sforce.async.JobInfo;
import com.sforce.ws.ConnectionException;

public class ClosedWorker implements Runnable {
    /**
     * The JobId to check for.
     */
    private String jobId;

    /**
     * The MigrationId to update.
     */
    private String migrationId;

    /**
     * The Server URL.
     */
    private String serverUrl;

    /**
     * The Session Id.
     */
    private String sessionId;

    /**
     * Bulk Connection
     */
    private SalesforceService sf;

    public ClosedWorker(String jobId, String migrationId, String serverUrl, String sessionId) {
        this.jobId       = jobId;
        this.migrationId = migrationId;
        this.serverUrl   = serverUrl;
        this.sessionId   = sessionId;
    }

    public void run() {
        sf = new SalesforceService(this.serverUrl, this.sessionId);
        try {
            JobInfo job  = sf.awaitCompletion(jobId);
            int failed   = job.getNumberRecordsFailed();
            sf.updateMigration(this.migrationId, failed);
        } catch (AsyncApiException|ConnectionException|DeployException e) {
            Utils.logException(e);
        }
    }
}
