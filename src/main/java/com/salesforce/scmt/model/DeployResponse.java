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

package com.salesforce.scmt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeployResponse {
	private List<String> errorMessages;
	private int successCount;
	private int errorCount;
	private Map<String, Set<Long>> errorMessagesWithIds;
	private String resumePoint;
	private Boolean delta;
	
	/**
	 * Adds the values from the provided 'DeployResponse' to this instance.
	 * 
	 * @param dr
	 *            The 'DeployResponse' that you want to combine with this one.
	 */
    public void addDeployResponse(DeployResponse dr) throws Exception
    {
        this.addErrors(dr.getErrors());
        this.incrementErrorCount(dr.getErrorCount());
        this.incrementSuccessCount(dr.getSuccessCount());
    }

	/**
	 * Add error message to the response.
	 * 
	 * @param Error
	 *            The error message.
	 */
    public void addError(String Error)
    {
        addErrors(Arrays.asList(Error));
    }

	/**
	 * Add list of error messages to the response.
	 * 
	 * @param Errors
	 *            The list of error messages.
	 */
    public void addErrors(List<String> Errors)
    {
        // null check
        if (this.errorMessages == null)
        {
            // initialize
            this.errorMessages = new ArrayList<>();
        }

        // null check
        if (Errors != null)
        {
            this.errorMessages.addAll(Errors);
        }
    }

	/**
	 * Retrieve the list of error messages in the response.
	 * 
	 * @return The list of error messages.
	 */
    public List<String> getErrors()
    {
        return this.errorMessages;
    }

	/**
	 * Increment the error count.
	 */
    public void incrementErrorCount()
    {
        this.errorCount++;
    }

	/**
	 * Increment the error count by the amount specified.
	 * 
	 * @param Amount
	 *            The amount to increment the error count by.
	 */
    public void incrementErrorCount(int Amount)
    {
        this.errorCount += Amount;
    }

	/**
	 * Returns the error count.
	 * 
	 * @return
	 */
    public int getErrorCount()
    {
        return this.errorCount;
    }

	/**
	 * Increment the success count.
	 */
    public void incrementSuccessCount()
    {
        this.successCount++;
    }

	/**
	 * Increment the success count by the amount specified.
	 * 
	 * @param Amount
	 *            The amount to increment the success count by.
	 */
    public void incrementSuccessCount(int Amount)
    {
        this.successCount += Amount;
    }

	/**
	 * Returns the success count.
	 * 
	 * @return
	 */
    public int getSuccessCount()
    {
        return this.successCount;
    }
    
    /**
     * Add an error message and a set of Ids
     * @param error - The error message (key)
     * @param id - The deskId to be added to set (value)
     */
    public void addErrorWithId(String error, Long id)
    {
    	// null check
        if (this.errorMessagesWithIds == null)
        {
            // initialize
            this.errorMessagesWithIds = new HashMap<String, Set<Long>>();
        }
        
    	if(this.errorMessagesWithIds.containsKey(error))
    	{
    		Set<Long> s = this.errorMessagesWithIds.get(error);
    		s.add(id);
    		this.errorMessagesWithIds.put(error, s);
    	}
    	else{
    		Set<Long> s = new HashSet<Long>();
    		s.add(id);
    		this.errorMessagesWithIds.put(error, s);
    	}
    }
    
    /**
	 * Returns the resume point. May be sinceId or page depending on object
	 * 
	 * @return
	 */
    public String getResumePoint()
    {
        return this.resumePoint;
    }
    
    /**
   	 * Sets the resume point. May be sinceId or page depending on object
   	 * 
   	 * @return
   	 */
    public void setResumePoint(Object p)
    {    	   
    	System.out.println("Resume Point" + p);
    	this.resumePoint = (String)p.toString();
    }
    
    /**
	 * Returns the bool if delta.
	 * 
	 * @return
	 */
    public Boolean getDelta()
    {
        return this.delta;
    }
    
    /**
   	 * Sets the bool delta.
   	 * 
   	 * @return
   	 */
    public void setDelta(Boolean p)
    {    	   
    	this.delta = p;
    }
}