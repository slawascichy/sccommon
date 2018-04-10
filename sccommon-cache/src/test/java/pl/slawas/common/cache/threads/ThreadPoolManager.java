package pl.slawas.common.cache.threads;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolManager {

	protected final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
	private Map<String, ExecutorService> executorServices = new HashMap<String, ExecutorService>();
	private final Object executorServicesLock = new Object();
	private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();

	/**
	 * 
	 * ThreadPoolCloser - Watek zamykający pule wątków
	 *
	 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
	 * @version $Revision: 1.1 $
	 *
	 */
	private static class ThreadPoolCloser extends Thread {
		@Override
		public void run() {
			ThreadPoolManager.INSTANCE.shutdownAll();
		}
	}

	/**
	 * Domyślny konstruktor managera pul wątków.
	 */
	private ThreadPoolManager() {
		Runtime.getRuntime().addShutdownHook(new ThreadPoolCloser());
	}

	/**
	 * Metoda zamykająca wszystkie pule wątków.
	 */
	public void shutdownAll() {
		synchronized (executorServicesLock) {
			for (Entry<String, ExecutorService> entry : executorServices.entrySet()) {
				String poolName = entry.getKey();
				ExecutorService executorService = entry.getValue();
				try {
					if (!executorService.isShutdown()) {
						executorService.shutdownNow();
						logger.info("Pula wątków o nazwie " + poolName + " została poprawnie zamknięta.");
					}
				} catch (Exception e) {
					logger.error("Błąd podczas zamykania puli watków o nazwie " + poolName + ".");
				}
			}
			executorServices.clear();
		}
	}

	/**
	 * @return the {@link #instance}
	 */
	public static ThreadPoolManager getInstance() {
		return INSTANCE;
	}

	public ExecutorService getThreadPool(String poolName, int queueCapacity, int corePoolSize, int maxPoolSize,
			long keepAliveSeconds) {
		synchronized (executorServicesLock) {
			ExecutorService executorService = executorServices.get(poolName);
			if (executorService == null) {
				ThreadFactory factory = new NamedThreadFactory(poolName);
				if (logger.isInfoEnabled()) {
					logger.info("Initializing FixedThreadPool {}", poolName);
				}
				BlockingQueue<Runnable> queue = createQueue(queueCapacity);
				executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
						queue, factory);
				executorServices.put(poolName, executorService);

			}
			return executorService;
		}
	}

	protected int getSystemCorePoolSize() {
		return 20;

	}

	protected int getSystemQueueCapacity() {
		return -1;

	}

	protected int getSystemMaxPoolSize() {
		return 30;

	}

	protected long getKeepAliveSeconds() {
		return 0;

	}

	public ExecutorService getMainThreadPool() {
		return getThreadPool("TEST-THREADS", getSystemQueueCapacity(), getSystemCorePoolSize(), getSystemMaxPoolSize(),
				getKeepAliveSeconds());
	}

	/**
	 * Create the BlockingQueue to use for the ThreadPoolExecutor.
	 * <p>
	 * A LinkedBlockingQueue instance will be created for a positive capacity value;
	 * a SynchronousQueue else.
	 * 
	 * @param queueCapacity
	 *            the specified queue capacity
	 * @return the BlockingQueue instance
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingQueue<Runnable>(queueCapacity);
		} else {
			return new SynchronousQueue<Runnable>();
		}
	}

}
