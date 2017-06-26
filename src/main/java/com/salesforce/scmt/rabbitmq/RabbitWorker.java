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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.salesforce.scmt.utils.Utils;
import com.salesforce.scmt.worker.DeskWorker;
import java.io.IOException;
import static com.salesforce.scmt.rabbitmq.RabbitConfiguration.*;

public final class RabbitWorker
{
    /**
     * Private constructor for utility class.
     */
    private RabbitWorker() {}

    public static void main(String[] argv) throws Exception
    {
        // declare the queue array and exchange string
        String[] queues = null;
        String exchange = null;
        
        // check which exchange we are initializing
        if (EXCHANGE_TRACTOR.equalsIgnoreCase(argv[0]))
        {
            exchange = EXCHANGE_TRACTOR;
            queues = new String[] { QUEUE_DESK_EMAIL, QUEUE_DESK_DATA_MIGRATION, QUEUE_DESK_ATTACHMENT };
        }
        else if (EXCHANGE_FORMULA1.equalsIgnoreCase(argv[0]))
        {
            exchange = EXCHANGE_FORMULA1;
            queues = new String[] { QUEUE_DESK_FEED_MIGRATION, QUEUE_DESK_EMAIL, // QUEUE_DESK_FEED_CONVERSION,
                QUEUE_DESK_BIG_COMPANY_MIGRATION, QUEUE_DESK_ATTACHMENT_BACKGROUND };
        }
        
        // ensure I found an exchange and initlized variables
        if (exchange == null || queues == null || queues.length <= 0)
        {
            Utils.log("[MQ] The passed exchange (" + argv[0] + ") does not exist!");
            return;
        }
        
        // create connection
        final Connection connection = connectionFactory().newConnection();
        
        // create channel
        final Channel channel = connection.createChannel();
        
        // create the exchange
        channel.exchangeDeclare(exchange, EXCHANGE_TYPE);
        
        //allow only one message to be added to queue at a time
        channel.basicQos(1);
        
        // loop through the queues
        for (String queue : queues)
        {
            // create the queue
            channel.queueDeclare(queue, false, false, false, null);
            
            // bind the queue to the exchange with a routing key (same as queue name)
            channel.queueBind(queue, exchange, queue);
            Utils.log("[MQ] Waiting for messages on [" + queue + "] Queue.");
        }
        
        final Consumer consumer = new DefaultConsumer(channel)
        {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                byte[] body) throws IOException
            {
                try
                {
                    String message = new String(body, "UTF-8");
                    Utils.log("[MQ]   Routing Key:  [" + envelope.getRoutingKey() + "]\n" +
                        "\t Exchange: [" + envelope.getExchange() + "]\n" +
                        "\t Message:  [" + message + "]");
                    
                    if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_DATA_MIGRATION))
                    {
                        DeskWorker.migrateData(message);
                    }
                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_FEED_MIGRATION))
                    {
                        DeskWorker.migrateCaseFeedData(message);
                    }
//                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_FEED_CONVERSION))
//                    {
//                        DeskWorker.convertCaseFeedData(message);
//                    }
                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_BIG_COMPANY_MIGRATION))
                    {
                        DeskWorker.migrateBigCompanyData(message);
                    }
                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_ATTACHMENT))
                    {
                        DeskWorker.queryDeskMessageAttachments(message);
                    }
                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_ATTACHMENT_BACKGROUND))
                    {
                        DeskWorker.migrateAttachments(message);
                    }
                    else if (envelope.getRoutingKey().equalsIgnoreCase(QUEUE_DESK_EMAIL))
                    {
                        DeskWorker.sendEmail(message);
                    }
                    else
                    {
                        Utils.log("[MQ] Unknown routing key: " + envelope.getRoutingKey());
                    }
                }
                finally
                {
                    Utils.log("[MQ] Completed work for [" + argv[0] + "].");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        // loop through the queues
        for (String queue : queues)
        {
            // start consumers
            channel.basicConsume(queue, false, consumer);
        }
    }
}