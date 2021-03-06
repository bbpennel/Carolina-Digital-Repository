package edu.unc.lib.dl.cdr.sword.server.deposit;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.swordapp.server.Deposit;
import org.swordapp.server.SwordError;

import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.util.DepositStatusFactory;
import edu.unc.lib.dl.util.PackagingType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/service-context.xml" })
public class DSPACEMETSDepositHandlerTest {
	@Mock
	private DepositStatusFactory depositStatusFactory;
	@Before
	public void setup() {
	    // Initialize mocks created above
	    MockitoAnnotations.initMocks(this);
	}
	
	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();
	
	@InjectMocks
	@Autowired
	private DSPACEMETSDepositHandler metsDepositHandler;
	@Autowired
	private SwordConfigurationImpl swordConfiguration;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDoDepositMETSBiomed() throws SwordError, IOException {
		Deposit d = new Deposit();
		File testPayload = tmpDir.newFile("biomedWithSupplements.zip");
		FileUtils.copyFile(new File("src/test/resources/biomedWithSupplements.zip"), testPayload);
		d.setFile(testPayload);
		d.setMd5("7ca5899e938e385c4ad61087bd834a0e");
		d.setFilename("biomedWithSupplements.zip");
		d.setMimeType("application/zip");
		d.setSlug("biomedtest");
		d.setPackaging(PackagingType.METS_DSPACE_SIP_1.getUri());
		Entry entry = Abdera.getInstance().getFactory().newEntry();
		d.setEntry(entry);
		
		PID dest = new PID("uuid:destination");
		metsDepositHandler.doDeposit(dest, d, PackagingType.METS_DSPACE_SIP_1, swordConfiguration,
				"test-depositor", "test-owner");

		verify(depositStatusFactory, atLeastOnce()).save(anyString(), anyMap());
	}

}
