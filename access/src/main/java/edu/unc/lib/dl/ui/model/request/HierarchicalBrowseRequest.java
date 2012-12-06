/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.ui.model.request;

import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchState;
import edu.unc.lib.dl.acl.util.AccessGroupSet;

public class HierarchicalBrowseRequest extends SearchRequest {
	private static final long serialVersionUID = 1L;
	private int retrievalDepth;
	
	public HierarchicalBrowseRequest(int retrievalDepth){
		this.retrievalDepth = retrievalDepth;
	}
	
	public HierarchicalBrowseRequest(SearchState searchState, int retrievalDepth, AccessGroupSet accessGroups){
		super(searchState, accessGroups);
		this.retrievalDepth = retrievalDepth;
	}
	
	public int getRetrievalDepth() {
		return retrievalDepth;
	}

	public void setRetrievalDepth(int retrievalDepth) {
		this.retrievalDepth = retrievalDepth;
	}
}
