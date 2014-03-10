package edu.unc.lib.bag.f3ingest;

import edu.unc.lib.workers.AbstractBagJob;

/**
 * Ingests the contents of the bag into the Fedora repository, along
 * with a deposit record. Also performs updates to the destination container.
 * @author count0
 *
 */
public class IngestBag extends AbstractBagJob implements Runnable {

	public IngestBag() {
		super();
	}

	public IngestBag(String uuid, String bagDirectory, String depositId) {
		super(uuid, bagDirectory, depositId);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// make foxml 
		// update container
		// queue all object ingests
	}

}