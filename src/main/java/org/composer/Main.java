package org.composer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;

import org.composer.core.Manager;
import org.composer.beans.RDFBean;
import org.composer.annotations.Domain;
import org.composer.annotations.Context;

import java.util.List;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutorService;
import java.io.InterruptedIOException;

import com.db4o.ObjectContainer;
import com.db4o.Db4o;
import com.db4o.config.Configuration;

public class Main {
    private Log log = LogFactory.getLog(Main.class);
    private ThreadPoolTaskExecutor executor;
    private static ObjectContainer database;

    private static Manager manager = Manager.getInstance();
    
    private long sleepTime = 1000000;
    private long timeout = 120000;

    // flag that specifies whether the program should keep running or be allowed to exit
	private volatile boolean keepRunning = true;

    public void setManager(Manager manager) {
        this.manager = manager;
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

    public void setDatabase(String filename) {
        this.database = Db4o.openFile(filename);
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
        database.close();
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
				manager.read(bean);
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

    private class InsertTask implements Runnable {
            private RDFBean bean;

            public InsertTask(RDFBean bean) {
                this.bean = bean;
            }

            public void run() {
                log.info("> processing inserting task ");

                TimerTask task = new TimerTask() {
                       Thread thread = Thread.currentThread();
                       public void run() {
                           thread.interrupt();
                       }
                    };

                Timer timer = new Timer();
                timer.schedule(task,timeout);

                try {
                    manager.add(bean);
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


    @Domain("e-commerce")
    @Context("cafepress")
    public void read() {
        try {
            List<RDFBean> beanList = manager.getAll("RDFBeans");
            int batchSize = beanList.size();

            log.info("> retrieved " + batchSize + " beans to process");


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

    @Domain("e-commerce")
    @Context("cafepress")
    public void add(String value) {
        log.info("inserting new bean named: "+value);
            try {
              executor.execute(new InsertTask(new RDFBean(value)));

            } catch (DataAccessResourceFailureException dbe) {
                log.error("<*> database connection error", dbe);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
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
    	if (args.length >= 1) {
            ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    	    final Main main = (Main)appContext.getBean("main");
            manager.setDatabase(database);

            // register shutdown handler
		    Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
				    main.shutdown();
			    }
		    });

            if ("read".equals(args[0])) {
                main.read();
            } else if ("add".equals(args[0])) {
                if (args[1] != null)
                    main.add(args[1]);
                else
                    main.add();
            } else if ("delete".equals(args[0])) {
                main.delete();
            } else if ("update".equals(args[0])) {
                main.update();
            } else {
                System.out.println("Usage: Main <task> task Options: \n\t - read \n\t - add \n\t - delete \n\t - update ");
            }

        } else {
            System.out.println("Usage: Main <task> task Options: \n\t - read \n\t - add \n\t - delete \n\t - update ");
        }
    }
}
