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

package com.salesforce.scmt.desk;

import com.squareup.okhttp.Headers;

public class DeskBaseResponse<T>
{
    protected boolean isSuccess;
    protected int errorCode;
    protected T body;
    private Headers headers;
    private String message;

    public void setIsSuccess(Boolean b)
    {
        this.isSuccess = b;
    }

    public boolean getIsSuccess()
    {
        return isSuccess;
    }

    public void setHeaders(Headers h)
    {
        // TODO Auto-generated method stub
        this.headers = h;
    }

    public Headers getHeaders()
    {
        return this.headers;
    }

    public int code()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object errorBody()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setMessage(String s)
    {
        // TODO Auto-generated method stub
        this.message = s;
    }

    public String getMessage()
    {
        return this.message;
    }

}
