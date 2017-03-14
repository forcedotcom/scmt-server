package com.desk.java.apiclient.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name of Salesforce.com nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class CustomerTest {

    private Customer customer;

    @Before
    public void setup() throws Exception {
        this.customer = new Customer();
    }

    @Test
    public void create() throws Exception {
        Customer c = Customer.create("John", "Smith", "john@smith.com", "home", "415-555-1584", "home");
        assertEquals("John", c.getFirstName());
        assertEquals("Smith", c.getLastName());
        assertEquals("john@smith.com", c.getFirstEmail());
        assertEquals("415-555-1584", c.getFirstPhone());
        assertEquals("home", c.getEmails()[0].getType());
        assertEquals("home", c.getPhoneNumbers()[0].getType());
    }

    @Test
    public void getEmailsCanNotBeNull() throws Exception {
        customer.setEmails((CustomerContact[]) null);
        assertNotNull(customer.getEmails());
    }

    @Test
    public void getAddressesCanNotBeNull() throws Exception {
        customer.setAddresses(null);
        assertNotNull(customer.getAddresses());
    }

    @Test
    public void getPhoneNumbers() throws Exception {
        customer.setPhoneNumbers((CustomerContact[]) null);
        assertNotNull(customer.getPhoneNumbers());
    }

    @Test
    public void getCustomFields() throws Exception {
        customer.setCustomFields(null);
        assertNotNull(customer.getCustomFields());
    }

    @Test
    public void getEmbedded() throws Exception {
        customer.setEmbedded(null);
        assertNotNull(customer.getEmbedded());
    }

    @Test
    public void getFirstEmail() throws Exception {
        customer.setEmails((CustomerContact[]) null);
        assertNull(customer.getFirstEmail());
        customer.setEmails((CustomerContact[]) new CustomerContact[] {});
        assertNull(customer.getFirstEmail());
        customer.setEmails((CustomerContact[]) new CustomerContact[] { new CustomerContact("home", "john@smith.com") });
        assertNotNull(customer.getFirstEmail());
    }

    @Test
    public void getFirstPhone() throws Exception {
        customer.setPhoneNumbers((CustomerContact[]) null);
        assertNull(customer.getFirstPhone());
        customer.setPhoneNumbers((CustomerContact[]) new CustomerContact[] {});
        assertNull(customer.getFirstPhone());
        customer.setPhoneNumbers((CustomerContact[]) new CustomerContact[] { new CustomerContact("home", "415-555-1584") });
        assertNotNull(customer.getFirstPhone());
    }

    @Test
    public void getFirstTwitterHandle() throws Exception {
        customer.setEmbedded(null);
        assertNull(customer.getFirstTwitterHandle());
        CustomerEmbedded ce = new CustomerEmbedded();
        ce.setTwitterUser(new TwitterUser());
        customer.setEmbedded(ce);
        assertNotNull(customer.getFirstTwitterHandle());
    }

    @Test
    public void isValidCustomerForCaseType() throws Exception {
        customer.setEmails((CustomerContact[]) null);
        assertFalse(customer.isValidCustomerForCaseType(CaseType.EMAIL));
        customer.setEmails((CustomerContact[]) new CustomerContact[] { new CustomerContact("home", "john@smith.com") });
        assertTrue(customer.isValidCustomerForCaseType(CaseType.EMAIL));

        customer.setEmbedded(null);
        assertFalse(customer.isValidCustomerForCaseType(CaseType.TWITTER));
        CustomerEmbedded ce = new CustomerEmbedded();
        ce.setTwitterUser(new TwitterUser());
        customer.setEmbedded(ce);
        assertTrue(customer.isValidCustomerForCaseType(CaseType.TWITTER));
    }

}