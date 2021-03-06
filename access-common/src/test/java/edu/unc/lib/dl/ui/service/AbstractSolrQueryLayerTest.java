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
package edu.unc.lib.dl.ui.service;

import static edu.unc.lib.dl.test.TestHelpers.setField;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.search.solr.service.SearchStateFactory;
import edu.unc.lib.dl.search.solr.util.FacetFieldUtil;
import edu.unc.lib.dl.search.solr.util.SearchSettings;
import edu.unc.lib.dl.search.solr.util.SolrSettings;

/**
 * @author bbpennel
 * @date Jan 23, 2015
 */
public class AbstractSolrQueryLayerTest {
	protected EmbeddedSolrServer server;

	protected CoreContainer container;

	protected SolrQueryLayerService queryLayer;

	protected SearchStateFactory stateFactory;
	protected SearchSettings searchSettings;
	protected SolrSettings solrSettings;
	protected FacetFieldUtil facetUtil;

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder();

	public final String COLLECTIONS_PID = "uuid:Collections";

	@Before
	public void setUp() throws Exception {
		initMocks(this);

		File home = new File("src/test/resources/config");
		File configFile = new File(home, "solr.xml");

		File dataDir = tmpFolder.newFolder("solrdata");
		System.setProperty("solr.data.dir", dataDir.getAbsolutePath());
		container = new CoreContainer("src/test/resources/config", configFile);

		server = new EmbeddedSolrServer(container, "access-master");

		searchSettings = new SearchSettings();
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File("src/test/resources/search.properties")));
		searchSettings.setProperties(properties);

		solrSettings = new SolrSettings();
		Properties solrProperties = new Properties();
		solrProperties.load(new FileInputStream(new File("src/test/resources/solr.properties")));
		solrSettings.setProperties(solrProperties);

		stateFactory = new SearchStateFactory();
		stateFactory.setSearchSettings(searchSettings);

		facetUtil = new FacetFieldUtil();
		facetUtil.setSearchSettings(searchSettings);
		facetUtil.setSolrSettings(solrSettings);

		queryLayer = new SolrQueryLayerService();
		queryLayer.setCollectionsPid(new PID("cdr:Collections"));
		queryLayer.setSearchSettings(searchSettings);
		queryLayer.setSolrSettings(solrSettings);
		queryLayer.setSearchStateFactory(stateFactory);
		queryLayer.setFacetFieldUtil(facetUtil);
		setField(queryLayer, "server", server);
	}

	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}
}
