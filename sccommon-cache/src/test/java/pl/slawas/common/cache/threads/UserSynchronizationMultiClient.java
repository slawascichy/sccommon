package pl.slawas.common.cache.threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.cache.beans.CachedElement;
import pl.slawas.common.cache.beans.UserSynchronizationCache;

public class UserSynchronizationMultiClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// TODO sparametryzować!
	/** maksymalna liczba spraw w jednej przetwarzanej paczce */
	public static final int TRANSFORMER_RECOMENDED_LIST_SIZE = 10;
	/** maksymalna liczba wątków */
	public static final int TRANSFORMER_MAX_THREADS = 10;
	/** podstawa czasu uśpienia wątku monitorującego [ms] */
	public static final int TRANSFORMER_MT_SLEEP_MS = 15;
	public static final long SYNC_PERIOD = 60 * 1000L;

	public void check(List<CachedElement> list, Properties props) throws ExecutionException {

		Map<Integer, UserSynchronizationCallable> threads = new HashMap<Integer, UserSynchronizationCallable>();
		ExecutorService transformerThreadPool = ThreadPoolManager.getInstance().getMainThreadPool();
		UserSynchronizationCache cache = new UserSynchronizationCache(props);

		try {
			/* Krok 6. zapisuję sprawy */

			Long startDate = Calendar.getInstance().getTimeInMillis();
			int processedDocuments = 0;
			int maxSizeOfTransformation = 1;
			int packageCounter = 0;
			Integer threadId = 1;

			/** wyliczam rozmiar listy przetwarzanej przez jeden wątek - START */
			/* rozmiar listy */
			int listSize = list.size();
			/* wyliczam ile przypada na jeden wątek */
			int p = listSize / TRANSFORMER_MAX_THREADS + 1;
			/*
			 * jeżeli na jeden wątek przypada mniej pozycji, to wybieram rekomendowany
			 * rozmiar, jeżeli nie to się dostosowuję
			 */
			final int packageSize = (p <= TRANSFORMER_RECOMENDED_LIST_SIZE ? TRANSFORMER_RECOMENDED_LIST_SIZE : p);
			logger.debug("-->UserSynchronizationMultiClient.check: packageSize: {}.", new Object[] { packageSize });
			/** wyliczam rozmiar listy przetwarzanej przez jeden wątek - KONIEC */

			/** wyliczam czas uśpienia wątku monitorującego - START */
			int transformerMtSleepTime = TRANSFORMER_MT_SLEEP_MS;
			if (packageSize > TRANSFORMER_RECOMENDED_LIST_SIZE) {
				int t = packageSize / TRANSFORMER_RECOMENDED_LIST_SIZE + 1;
				transformerMtSleepTime = t * TRANSFORMER_RECOMENDED_LIST_SIZE;
			}
			/** wyliczam czas uśpienia wątku monitorującego - KONIEC */

			List<Future<Integer>> transformerThreadPoolFutures = new ArrayList<Future<Integer>>();
			Map<Long, Boolean> isDoneMap = new HashMap<Long, Boolean>();

			UserSynchronizationCallable call = null;
			for (CachedElement lVal : list) {
				logger.trace("-->UserSynchronizationMultiClient.check: dodaję '{}' z wartością: '{}'",
						new Object[] { lVal.getName(), lVal.getValue() });
				call = threads.get(threadId);
				if (call == null) {
					call = new UserSynchronizationCallable(SYNC_PERIOD, cache);
					threads.put(threadId, call);
				}
				call.add(lVal);

				if (maxSizeOfTransformation < packageSize) {
					/* zwiększam licznik tak długo aż nie przekroczę maksymalnego rozmiaru listy */
					maxSizeOfTransformation = call.sizeOfProcessingList();
				}
				packageCounter++;

				if (packageCounter >= packageSize) {
					packageCounter = 0;
					threadId = threadId + 1;
				}

			}
			int ii = 0;
			for (UserSynchronizationCallable thread : threads.values()) {
				transformerThreadPoolFutures.add(transformerThreadPool.submit(thread));
				isDoneMap.put(ii + 0L, Boolean.FALSE);
				ii++;
			}

			if (!threads.isEmpty()) {
				/** są dokumenty do aktualizacji - START */

				/* monitoruję wątki */
				boolean isNotDone = true;
				while (isNotDone) {
					processedDocuments = checkThreads(isDoneMap, transformerThreadPoolFutures);
					boolean finish = true;
					for (Entry<Long, Boolean> isDone : isDoneMap.entrySet()) {
						finish = isDone.getValue() && finish;
					}
					isNotDone = !finish;
					if (isNotDone) {
						long sleepTime = transformerMtSleepTime * 1L;
						/** GU-7: Czekaj chwilę */
						Thread.sleep(sleepTime);
					}
					/** Pętla monitorująca działanie wątków - KONIEC */
				}

				/* przygotowanie wyniku działania */
				/** są dokumenty do aktualizacji - KONIEC */
			}

			Long endDate = Calendar.getInstance().getTimeInMillis();
			logger.debug("-->UserSynchronizationMultiClient.check: Przeprocesowałem: {} w czasie {} [ms]",
					new Object[] { processedDocuments, endDate - startDate });

		} catch (InterruptedException e) {
			logger.warn("-->UserSynchronizationMultiClient.check: Przerwano oczekiwanie monitoringu.", e);
			Thread.currentThread().interrupt();
		}
		return;
	}

	private int checkThreads(Map<Long, Boolean> isDoneMap, List<Future<Integer>> transformerThreadPoolFutures)
			throws InterruptedException, ExecutionException {
		/** Pętla monitorująca działanie wątków - START */
		int processed = 0;
		for (int i = 0; i < transformerThreadPoolFutures.size(); i++) {
			Future<Integer> currFuture = transformerThreadPoolFutures.get(i);
			if (!currFuture.isDone()) {
				/* zadanie jeszcze nie zrobione */
				continue;
			}
			/*
			 * zadanie zrobione i nie jest na liście wykonanych - START
			 */
			/* oznaczam na liście wykonanych */
			isDoneMap.put(i + 0L, Boolean.TRUE);
			processed = sumProcessedDocuments(processed, currFuture);
			/*
			 * zadanie zrobione i nie jest na liście wykonanych - KONIEC
			 */
		}
		return processed;
	}

	private int sumProcessedDocuments(int processedDocuments, Future<Integer> currFuture)
			throws InterruptedException, ExecutionException {
		int processed = processedDocuments;
		Integer curFutureResult = currFuture.get();
		if (curFutureResult != -1) {
			processed = processedDocuments + curFutureResult;
		}
		return processed;
	}
}
