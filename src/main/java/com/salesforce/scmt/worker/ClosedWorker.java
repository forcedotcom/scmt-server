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

import java.util.List;

import com.salesforce.scmt.utils.Utils;
import com.salesforce.scmt.utils.SalesforceConstants;
import com.salesforce.scmt.utils.SalesforceConstants.DeskMigrationFields;
import com.salesforce.scmt.model.DeployException;
import com.salesforce.scmt.service.SalesforceService;
import com.sforce.async.AsyncApiException;
import com.sforce.async.JobInfo;
import com.sforce.ws.ConnectionException;
import com.sforce.soap.partner.sobject.SObject;

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
        sf = new SalesforceService(serverUrl, sessionId);
        try {
            JobInfo job   = sf.awaitCompletion(jobId);
            SObject mig   = getDeskMigration();

            int failed    = job.getNumberRecordsFailed() + Double.valueOf((String) mig.getField(DeskMigrationFields.RecordsFailed)).intValue();
            int processed = job.getNumberRecordsProcessed() + Double.valueOf((String) mig.getField(DeskMigrationFields.RecordsTotal)).intValue();

            sf.updateMigration(migrationId, failed, processed);
            //sf.updateCustomLabel("BypassProcessBuilder", "0");
        } catch (AsyncApiException|ConnectionException|DeployException e) {
            Utils.logException(e);
        }
    }

    /**
     * Query the Desk Migration Record from Salesforce. If none is found we
     * return a blank Desk Migration with zero failed and total.
     *
     * @throws ConnectionException
     * @return SObject
     */
    public SObject getDeskMigration() throws ConnectionException {
        String query = String.format(
            "Select %s, %s From %s Where %s = '%s'",
            DeskMigrationFields.RecordsFailed,
            DeskMigrationFields.RecordsTotal,
            SalesforceConstants.OBJ_DESK_MIGRATION,
            DeskMigrationFields.ID, migrationId
        );

        List<SObject> result = sf.query(query);
        if (result != null || !result.isEmpty()) {
            return result.get(0);
        }

        SObject migration = new SObject(SalesforceConstants.OBJ_DESK_MIGRATION);
        migration.setId(migrationId);
        migration.setField(DeskMigrationFields.RecordsFailed, 0);
        migration.setField(DeskMigrationFields.RecordsTotal, 0);

        return migration;
    }
}
