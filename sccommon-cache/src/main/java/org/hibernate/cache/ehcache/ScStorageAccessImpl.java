package org.hibernate.cache.ehcache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import pl.slawas.common.cache.IObjectCache;
import pl.slawas.common.cache.exceptions.CacheErrorException;

public class ScStorageAccessImpl implements DomainDataStorageAccess {

	private final IObjectCache cache;

	public ScStorageAccessImpl(IObjectCache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public Object getFromCache(Object key, SharedSessionContractImplementor session) {
		try {
			return this.cache.get(key);
		} catch (CacheErrorException e) {
			throw new CacheException(String.format("-->getFromCache('%s')", key), e);
		}
	}

	@Override
	public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
		try {
			this.cache.put(key, value);
		} catch (CacheErrorException e) {
			throw new CacheException(String.format("-->putIntoCache('%s')", key), e);
		}
	}

	@Override
	public boolean contains(Object key) {
		return this.cache.contains(key);
	}

	@Override
	public void evictData() {
		try {
			this.cache.clear();
		} catch (CacheErrorException e) {
			throw new CacheException("-->evictData()", e);
		}
	}

	@Override
	public void evictData(Object key) {
		try {
			this.cache.remove(key);
		} catch (CacheErrorException e) {
			throw new CacheException(String.format("-->evictData('%s')", key), e);
		}
	}

	@Override
	public void release() {
		try {
			this.cache.destroy();
		} catch (CacheErrorException e) {
			throw new CacheException("-->release()", e);
		}
	}

}
