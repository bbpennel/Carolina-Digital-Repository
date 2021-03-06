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
package edu.unc.lib.dl.data.ingest.solr.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRequest;
import edu.unc.lib.dl.data.ingest.solr.exception.IndexingException;
import edu.unc.lib.dl.data.ingest.solr.exception.OrphanedObjectException;
import edu.unc.lib.dl.data.ingest.solr.exception.UnsupportedContentModelException;
import edu.unc.lib.dl.data.ingest.solr.indexing.DocumentIndexingPackage;
import edu.unc.lib.dl.fedora.PID;

/**
 * Performs depth first indexing of a tree of repository objects, starting at the PID of the provided update request.
 *
 * @author bbpennel
 *
 */
public class RecursiveTreeIndexer {
	private static final Logger log = LoggerFactory.getLogger(RecursiveTreeIndexer.class);

	private final UpdateTreeAction action;
	private final SolrUpdateRequest updateRequest;
	private boolean addDocumentMode = true;

	public RecursiveTreeIndexer(SolrUpdateRequest updateRequest, UpdateTreeAction action) {
		this.updateRequest = updateRequest;
		this.action = action;
	}

	public RecursiveTreeIndexer(SolrUpdateRequest updateRequest, UpdateTreeAction action, boolean addDocumentMode) {
		this.updateRequest = updateRequest;
		this.action = action;
		this.addDocumentMode = addDocumentMode;
	}

	public void index(PID pid, DocumentIndexingPackage parent) {
		DocumentIndexingPackage dip = null;
		try {
			// Force wait before each document being indexed
			if (this.action.getUpdateDelay() > 0)
				Thread.sleep(this.action.getUpdateDelay());
			// Wait if the service is paused
			while (this.action.getSolrUpdateService().isPaused()) {
				Thread.sleep(1000L);
			}

			// Get the DIP for the next object being indexed
			dip = this.action.getDocumentIndexingPackage(pid, parent);
			if (dip == null)
				throw new IndexingException("No document indexing package was retrieved for " + pid.getPid());

			// Perform document populating pipeline
			this.action.getPipeline().process(dip);

			// Update the current target in solr
			if (addDocumentMode)
				this.action.getSolrUpdateDriver().addDocument(dip.getDocument());
			else
				this.action.getSolrUpdateDriver().updateDocument("set", dip.getDocument());

			// Update the number of objects processed in this action
			this.updateRequest.incrementChildrenProcessed();

		} catch (InterruptedException e) {
			log.warn("Indexing of {} was interrupted", updateRequest.getPid().getPid());
			return;
		} catch (UnsupportedContentModelException e) {
			log.info("Invalid content model on object {}, skipping its children", pid.getPid(), e);
			return;
		} catch (OrphanedObjectException e) {
			log.info("Object {} was orphaned, skipping its children", pid.getPid(), e);
			return;
		} catch (IndexingException e) {
			log.warn("Failed to index {}", pid.getPid(), e);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while indexing {}, skipping its children", pid.getPid(), e);
			return;
		} finally {
			// Clear parent bond to allow memory cleanup
			if (dip != null)
				dip.setParentDocument(null);
		}

		// Start indexing the children
		if (dip != null)
			this.indexChildren(dip, dip.getChildren());
	}

	public void indexChildren(DocumentIndexingPackage parent, List<PID> children) {
		if (children != null) {
			for (PID child : children) {
				this.index(child, parent);
			}
		}
	}
}