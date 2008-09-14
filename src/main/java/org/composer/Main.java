package org.composer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;

import org.composer.core.ReaderManager;
import org.composer.beans.RDFBean;

import java.util.List;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutorService;
import java.io.InterruptedIOException;

public class Main {
    private Log log = LogFactory.getLog(Main.class);

	private ThreadPoolTaskExecutor executor;

	private ReaderManager reader;
	private long sleepTime = 1000000;
    private long timeout = 120000;

    // flag that specifies whether the program should keep running or be allowed to exit
	private volatile boolean keepRunning = true;

    public void setReaderManager(ReaderManager reader) {
        this.reader = reader;
    }

    /**
	 * Thread manager.
	 */
	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	/**
	 * If there are no transactions to be processed, this sets the time in
	 * milliseconds that the program will sleep before checking if there
	 * are more transactions.
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
		log.info("sleep time set to " + sleepTime + "ms");
	}

    public void setTimeout(long timeout) {
		this.timeout = timeout;
		log.info("timeout set to " + timeout + "ms");
	}

    /**
	 * Call this method when the program is ready to exit. It will tell the
	 * internal thread manager to shutdown and signal any processing loops
	 * to terminate.
	 */
	public void shutdown() {
		log.info("program shutdown requested");
		log.info("waiting for threads to finish");
		System.out.println("Exiting program");
		keepRunning = false;
		executor.shutdown();
	}


    private class ReadingTask implements Runnable {
        private RDFBean bean;

		public ReadingTask(RDFBean bean) {
			this.bean = bean;
		}

		public void run() {
            log.info("> processing reading task ");

            TimerTask task = new TimerTask() {
                   Thread thread = Thread.currentThread();
                   public void run() {
                       thread.interrupt();
                   }
                };

            Timer timer = new Timer();
            timer.schedule(task,timeout);

			try {
				reader.read(bean);
            } catch (DataAccessResourceFailureException dbe) {
                log.error("<*> database connection error", dbe);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
                task.cancel();
                Thread.interrupted();
			}
		}
	}


    public void read() {
        try {
            List<RDFBean> beanList = reader.getAll("RDFBeans");
            int batchSize = beanList.size();

            log.info("> retrieved " + batchSize + " members to invoice");


            if (batchSize > 0) {
                for (RDFBean p: beanList) {
                    executor.execute(new ReadingTask(p));
                }

                log.info("[!] processed " + batchSize + " invoices");
            }
        } catch (DataAccessResourceFailureException dbe) {
            log.error("<*> database connection error", dbe);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


	public void add() {
	}

	public void delete() {
}

	public void update() {
}


    /**
	 * Application entry point.
	 */
    public static void main(String[] args)
    {
        // parse command-line options
    	if (args.length == 1) {
            ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    	    final Main main = (Main)appContext.getBean("main");

		    // register shutdown handler
		    Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
				    main.shutdown();
			    }
		    });

            if ("read".equals(args[0])) {
                main.read();
            } else if ("add".equals(args[0])) {
                main.add();
            } else if ("delete".equals(args[0])) {
                main.delete();
            } else if ("update".equals(args[0])) {
                main.update();
            } else {
                System.out.println("Usage: Main <task> task Options: \n\t - billing \n\t - payment \n\t - invoicing \n\t - notification ");
            }

            main.shutdown();

        } else {
            System.out.println("Usage: Main <task> task options: \n\t - billing \n\t - payment \n\t - invoicing \n\t - notification ");
        }
    }
}
