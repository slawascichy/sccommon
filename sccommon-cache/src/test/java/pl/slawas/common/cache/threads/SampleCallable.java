package pl.slawas.common.cache.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.cache.CacheUsage;
import pl.slawas.common.cache.beans.CachedElement;
import pl.slawas.common.cache.beans.CachedObjectFactory;
import pl.slawas.common.cache.beans.CachedObjectResult;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.entities.NameValuePair;

public class SampleCallable implements Callable<Integer> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private List<CachedElement> elements = new ArrayList<CachedElement>();

	private Properties props;

	public SampleCallable(Properties props) {
		super();
		this.props = props;
	}

	@Override
	public Integer call() throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		logger.debug("-->SampleCallable.call: {}", currentThreadName);
		for (CachedElement el : elements) {
			try {
				CachedObjectResult result = CachedObjectFactory.get(el, CacheUsage.TO_USE, props);
				NameValuePair e = result.getObj();
				if (logger.isTraceEnabled()) {
					logger.trace("-->SampleCallable.call: {}: '{}' pobrałem wartość: '{}' z {}", new Object[] {
							currentThreadName, e.getName(), e.getValue(), result.getFromCache() ? "CACHE" : "NOWY" });
				}
				NameValuePair o = el;
				if (!e.getValue().equals(o.getValue())) {
					result = CachedObjectFactory.get(el, CacheUsage.REFRESH, props);
					NameValuePair n = result.getObj();
					if (logger.isTraceEnabled()) {
						logger.trace(
								"-->SampleCallable.call: {}: '{}' zmiana wartości: robię refresh '{}'-->'{}', pobrałem wartość: '{}' ",
								new Object[] { currentThreadName, o.getName(), e.getValue(), o.getValue(),
										n.getValue() });
					}
				}

			} catch (CacheErrorException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return sizeOfProcessingList();
	}

	public void add(CachedElement element) {
		elements.add(element);
	}

	public int sizeOfProcessingList() {
		return elements.size();
	}
}
