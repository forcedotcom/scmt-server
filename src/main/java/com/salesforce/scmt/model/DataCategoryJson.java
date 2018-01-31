/**
 * Copyright (c) 2018, Salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */


package com.salesforce.scmt.model;

public class DataCategoryJson {
	public String name;
	public String label;
    public DataCategoryJson[] subCategories;
    public DataCategoryJson parent;


	public DataCategoryJson() {
		// TODO Auto-generated constructor stub
	}

}