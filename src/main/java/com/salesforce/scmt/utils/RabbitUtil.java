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

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.salesforce.scmt.rabbitmq.RabbitConfiguration;

public final class RabbitUtil
{
    /**
     * Private static member variable for holding the channel.
     */
    private static Channel _channel;
    
    /**
     * Private constructor for utility class.
     */
    private RabbitUtil() {}
    
//    public static void publishToQueue(String routingKey, String exchange, Object params) throws IOException
//    {
//        Utils.log("RabbitUtil::publishToQueue() entered. Routing Key: [" + routingKey + "]");
//        // build the RabbitMQ connection
//        Connection connection = RabbitConfiguration.connectionFactory().newConnection();
//        Channel channel = connection.createChannel();
//        
//        // push the job to the queue
//        String message = JsonUtil.toJson(params);
//        channel.basicPublish(exchange, routingKey, null, message.getBytes());
//        Utils.log("[MQ] Sent '" + message + "'");
//        
//        // null out the message to avoid a memory leak
//        message = null;
//        
//        // close the RabbitMQ connection
//        channel.close();
//        connection.close();
//    }
    
    /**
     * Get a channel (create it if needed).
     * @return The channel.
     */
    private static Channel getChannel()
    {
        // thread-safe singleton
        synchronized (RabbitUtil.class)
        {
            // check if channel is null
            if (_channel == null)
            {
                try
                {
                    // create a new connection and then a new channel
                    _channel = RabbitConfiguration.connectionFactory().newConnection().createChannel();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        // return channel
        return _channel;
    }

    /**
     * Publish a message to the queue.
     * @param routingKey
     * @param exchange
     * @param message
     * @throws IOException
     */
    public static void publishToQueue(String routingKey, String exchange, byte[] message) throws IOException
    {
        Utils.log("[MQ] Publish: Routing Key: [" + routingKey + "] Exchange: [" + exchange + "]");
        
        // push the job to the queue
        getChannel().basicPublish(exchange, routingKey, null, message);
//        Utils.log("[MQ] Sent '" + message + "'");
    }
}
