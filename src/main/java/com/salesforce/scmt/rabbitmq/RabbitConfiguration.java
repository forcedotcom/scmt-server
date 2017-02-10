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

package com.salesforce.scmt.rabbitmq;

import java.net.URI;
import java.net.URISyntaxException;
import com.rabbitmq.client.ConnectionFactory;
import com.salesforce.scmt.utils.Utils;

public final class RabbitConfiguration
{
    /**
     * The exchange name for 'bulk' Desk.com migration processes.
     */
    public final static String EXCHANGE_TRACTOR = "tractor";
    
    /**
     * The exchange name for 'non-bulk' Desk.com migration processes.
     */
    public final static String EXCHANGE_FORMULA1 = "formula1";
    
    /**
     * The 'type' of the exchange.
     */
    public final static String EXCHANGE_TYPE = "direct";

    /**
     * The queue name for Desk.com migrations.
     */
    public final static String QUEUE_DESK_DATA_MIGRATION = "desk_data_migration";
    
    /**
     * The queue name used for migrating Desk.com Case Feed (notes, interactions, email messages, etc.)
     */
    public final static String QUEUE_DESK_FEED_MIGRATION = "desk_feed_migration";
    
//    /**
//     * The queue name used for converting the Desk.com Case Feed items that were inserted into the custom object in
//     * the destination Salesforce organization.
//     */
//    public final static String QUEUE_DESK_FEED_CONVERSION = "desk_feed_conversion";
    
    /**
     * The queue name used for converting the Desk.com companies, when the Desk.com instance has > 500k companies.
     */
    public final static String QUEUE_DESK_BIG_COMPANY_MIGRATION = "desk_big_company_migration";
    
    /**
     * The queue name used for converting the Desk.com attachments.
     */
    public final static String QUEUE_DESK_ATTACHMENT = "desk_attachment_migration";

    /**
     * The queue name used for converting the Desk.com attachments (background).
     */
    public final static String QUEUE_DESK_ATTACHMENT_BACKGROUND = "desk_attachment_migration_background";

    /**
     * Local static instance
     */
    private static ConnectionFactory _factory;

    /**
     * Private constructor for utility class.
     */
    private RabbitConfiguration() {}

    /**
     * Builds (if necessary) and returns a ConnectionFactory instance.
     * 
     * @return The ConnectionFactory instance.
     */
    public static ConnectionFactory connectionFactory()
    {
        // check if we have constructed the factory already
        if (_factory != null)
        {
            // return the existing connection
            return _factory;
        }

        // declare holders for the environment variables
        final URI amqpUri;
        final Integer requestHeartbeat;
        final Integer connectionTimeout;

        // try to read environment variables
        try
        {
            amqpUri = new URI(Utils.getEnvOrThrow("CLOUDAMQP_URL"));
            requestHeartbeat = Integer.parseInt(Utils.getEnvOrThrow("CLOUDAMQP_HEARTBEAT"));
            connectionTimeout = Integer.parseInt(Utils.getEnvOrThrow("CLOUDAMQP_TIMEOUT"));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        // create & initialize the ConnectionFactory
        _factory = new ConnectionFactory();
        _factory.setUsername(amqpUri.getUserInfo().split(":")[0]);
        _factory.setPassword(amqpUri.getUserInfo().split(":")[1]);
        _factory.setHost(amqpUri.getHost());
        _factory.setPort(amqpUri.getPort());
        _factory.setVirtualHost(amqpUri.getPath().substring(1));
        _factory.setRequestedHeartbeat(requestHeartbeat);
        _factory.setConnectionTimeout(connectionTimeout);

        return _factory;
    }
}