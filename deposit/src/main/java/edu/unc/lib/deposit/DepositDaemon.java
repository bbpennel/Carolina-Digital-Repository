package edu.unc.lib.deposit;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.unc.lib.deposit.work.DepositSupervisor;

public class DepositDaemon implements Daemon {
	private static final Logger LOG = LoggerFactory.getLogger(DepositDaemon.class);
	private AbstractApplicationContext appContext;
	public DepositDaemon() {
	}

	@Override
	public void destroy() {
		LOG.info("Deposit Daemon destroy called");
		appContext.destroy();
		// supervisor has destroy hooks registered in appContext
	}

	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
	}

	@Override
	public void start() throws Exception {
		LOG.info("Starting the Deposit Daemon");
		if(appContext == null) {
			appContext = new ClassPathXmlApplicationContext(new String[] {"service-context.xml"});
			appContext.registerShutdownHook();
		} else {
			appContext.refresh();
		}
		// start the supervisor
		DepositSupervisor supervisor = appContext.getBean(DepositSupervisor.class);
		supervisor.start();
		LOG.info("Started the Deposit Daemon");
	}

	@Override
	public void stop() throws Exception {
		LOG.info("Stopping the Deposit Daemon");
		// stop the supervisor
		DepositSupervisor supervisor = appContext.getBean(DepositSupervisor.class);
		supervisor.stop();
		appContext.stop();
		LOG.info("Stopped the Deposit Daemon");
	}

}
