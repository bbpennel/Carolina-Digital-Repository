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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRequest;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateService;
import edu.unc.lib.dl.data.ingest.solr.exception.IndexingException;
import edu.unc.lib.dl.data.ingest.solr.indexing.DocumentIndexingPackage;
import edu.unc.lib.dl.data.ingest.solr.indexing.DocumentIndexingPipeline;
import edu.unc.lib.dl.data.ingest.solr.indexing.SolrUpdateDriver;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.search.solr.model.IndexDocumentBean;

public class RecursiveTreeIndexerTest extends Assert {

	private RecursiveTreeIndexer indexer;

	@Mock
	private SolrUpdateService updateService;
	@Mock
	private SolrUpdateDriver driver;
	@Mock
	private UpdateTreeAction action;
	@Mock
	private DocumentIndexingPipeline pipeline;

	@Mock
	private DocumentIndexingPackage parentDip;
	@Mock
	private SolrUpdateRequest request;

	@Before
	public void setup() {
		initMocks(this);

		when(action.getSolrUpdateDriver()).thenReturn(driver);
		when(action.getPipeline()).thenReturn(pipeline);
		when(action.getSolrUpdateService()).thenReturn(updateService);

		indexer = new RecursiveTreeIndexer(request, action);
	}

	@Test
	public void indexNoDip() throws Exception {

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class))).thenReturn(null);

		indexer.index(new PID("pid"), parentDip);

		verify(pipeline, never()).process(any(DocumentIndexingPackage.class));
		verify(driver, never()).addDocument(any(IndexDocumentBean.class));
		verify(request, never()).incrementChildrenProcessed();

	}

	@Test
	public void indexGetDipException() throws Exception {

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class)))
			.thenThrow(new IndexingException(""));

		indexer.index(new PID("pid"), parentDip);

		verify(pipeline, never()).process(any(DocumentIndexingPackage.class));
		verify(driver, never()).addDocument(any(IndexDocumentBean.class));
		verify(request, never()).incrementChildrenProcessed();

	}

	@Test
	public void indexNoChildren() throws Exception {

		DocumentIndexingPackage dip = mock(DocumentIndexingPackage.class);
		when(dip.getChildren()).thenReturn(null);

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class))).thenReturn(dip);

		indexer.index(new PID("pid"), parentDip);

		verify(pipeline).process(eq(dip));
		verify(driver).addDocument(any(IndexDocumentBean.class));
		verify(request).incrementChildrenProcessed();

	}

	@Test
	public void indexUpdate() throws Exception {

		indexer = new RecursiveTreeIndexer(request, action, false);

		DocumentIndexingPackage dip = mock(DocumentIndexingPackage.class);

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class))).thenReturn(dip);

		indexer.index(new PID("pid"), parentDip);

		verify(pipeline).process(eq(dip));
		verify(driver, never()).addDocument(any(IndexDocumentBean.class));
		verify(driver).updateDocument(eq("set"), any(IndexDocumentBean.class));
		verify(request).incrementChildrenProcessed();

	}

	@Test
	public void indexVerifyOrder() throws Exception {

		final Map<String, DocumentIndexingPackage> tree = new HashMap<String, DocumentIndexingPackage>();

		DocumentIndexingPackage dipRoot = mock(DocumentIndexingPackage.class);
		tree.put("c0", dipRoot);
		List<PID> children1 = Arrays.asList(new PID("c1"), new PID("c2"));
		when(dipRoot.getChildren()).thenReturn(children1);

		DocumentIndexingPackage dipC1 = mock(DocumentIndexingPackage.class);
		tree.put("c1", dipC1);
		List<PID> children2 = Arrays.asList(new PID("c3"));
		when(dipC1.getChildren()).thenReturn(children2);

		tree.put("c2", mock(DocumentIndexingPackage.class));
		tree.put("c3", mock(DocumentIndexingPackage.class));

		ArgumentCaptor<PID> pidArgs = ArgumentCaptor.forClass(PID.class);

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class))).thenAnswer(
				new Answer<DocumentIndexingPackage>() {
					@Override
					public DocumentIndexingPackage answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						PID pid = (PID) args[0];
						return tree.get(pid.getPid());
					}
				});

		indexer.index(new PID("c0"), parentDip);

		verify(action, times(4)).getDocumentIndexingPackage(pidArgs.capture(), any(DocumentIndexingPackage.class));
		verify(pipeline, times(4)).process(any(DocumentIndexingPackage.class));
		verify(driver, times(4)).addDocument(any(IndexDocumentBean.class));
		verify(request, times(4)).incrementChildrenProcessed();

		List<PID> pidOrder = pidArgs.getAllValues();
		assertEquals("Incorrect number of pids used to retrieved indexing packages", 4, pidOrder.size());

		assertEquals("Processing order incorrect", "c0", pidOrder.get(0).getPid());
		assertEquals("Processing order incorrect", "c1", pidOrder.get(1).getPid());
		assertEquals("Processing order incorrect", "c3", pidOrder.get(2).getPid());
		assertEquals("Processing order incorrect", "c2", pidOrder.get(3).getPid());

	}

	@Test
	public void testIndexingExceptionThrown() throws Exception {
		DocumentIndexingPackage dipRoot = mock(DocumentIndexingPackage.class);
		List<PID> children1 = Arrays.asList(new PID("c1"), new PID("c2"), new PID("c3"));
		when(dipRoot.getChildren()).thenReturn(children1);

		DocumentIndexingPackage dipC1 = mock(DocumentIndexingPackage.class);
		DocumentIndexingPackage dipC2 = mock(DocumentIndexingPackage.class);
		List<PID> children2 = Arrays.asList(new PID("c4"));
		when(dipC2.getChildren()).thenReturn(children2);
		DocumentIndexingPackage dipC3 = mock(DocumentIndexingPackage.class);
		DocumentIndexingPackage dipC4 = mock(DocumentIndexingPackage.class);

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class)))
				.thenReturn(dipRoot, dipC1, dipC2, dipC4, dipC3);

		doThrow(new IndexingException("")).when(pipeline).process(eq(dipC2));

		indexer.index(new PID("c0"), parentDip);

		// All objects, including the children of c2 should have been retrieved
		verify(action, times(5)).getDocumentIndexingPackage(any(PID.class),
				any(DocumentIndexingPackage.class));
		verify(pipeline, times(5)).process(any(DocumentIndexingPackage.class));
		// All objects except c2 should have been added to driver
		verify(driver, times(4)).addDocument(any(IndexDocumentBean.class));
		// Count should only be increment for objects that successfully indexed
		verify(request, times(4)).incrementChildrenProcessed();

		verify(dipC3).setParentDocument(isNull(DocumentIndexingPackage.class));
		// Ensure that parent reference cleared on object that throws exception
		verify(dipC2).setParentDocument(isNull(DocumentIndexingPackage.class));
	}

	/**
	 * This test seems a bit contrived, but it is to ensure that cleanup happens and the children of the object that
	 * threw the exception are not indexed.
	 */
	@Test
	public void indexUnexpectedException() throws Exception {

		when(action.getSolrUpdateDriver()).thenReturn(driver, (SolrUpdateDriver) null, driver);

		DocumentIndexingPackage dipRoot = mock(DocumentIndexingPackage.class);
		List<PID> children1 = Arrays.asList(new PID("c1"), new PID("c2"));
		when(dipRoot.getChildren()).thenReturn(children1);

		DocumentIndexingPackage dipC1 = mock(DocumentIndexingPackage.class);
		IndexDocumentBean idbC1 = mock(IndexDocumentBean.class);
		when(dipC1.getDocument()).thenReturn(idbC1);
		List<PID> children2 = Arrays.asList(new PID("c3"));
		when(dipC1.getChildren()).thenReturn(children2);

		DocumentIndexingPackage dipC2 = mock(DocumentIndexingPackage.class);

		when(action.getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class)))
				.thenReturn(dipRoot, dipC1, dipC2);

		try {
			indexer.index(new PID("c0"), parentDip);
		} finally {
			// Everything except the children of c1 and its children should have been indexed
			verify(action, times(3)).getDocumentIndexingPackage(any(PID.class), any(DocumentIndexingPackage.class));

			verify(driver, times(2)).addDocument(any(IndexDocumentBean.class));
			verify(driver, never()).addDocument(eq(idbC1));

			verify(dipC1).setParentDocument(isNull(DocumentIndexingPackage.class));
		}

	}
}
