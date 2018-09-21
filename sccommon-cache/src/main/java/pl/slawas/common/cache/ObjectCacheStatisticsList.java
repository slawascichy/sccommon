package pl.slawas.common.cache;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * ObjectCacheStatisticsList
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class ObjectCacheStatisticsList implements Serializable {

	private static final long serialVersionUID = 3020671139321025065L;

	/** fragment listy obiektów ze statystykami */
	private final List<IObjectCacheStatistics> list;

	/** liczba wszystkich obiektów ze statystykami */
	private final int size;

	/** pozycja, od której została wystartowano ładowanie listy */
	private final int offset;

	/** zadeklarowany rozmiar strony (maksymalna liczba wyników) */
	private final int pageSize;

	public ObjectCacheStatisticsList(List<IObjectCacheStatistics> list,
			int size, int offset, int pageSize) {
		super();
		this.list = list;
		this.size = size;
		this.offset = offset;
		this.pageSize = pageSize;
	}

	/**
	 * @return the {@link #list}
	 */
	public List<IObjectCacheStatistics> getList() {
		return list;
	}

	/**
	 * @return the {@link #size}
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the {@link #offset}
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @return zwraca następną pozycję, od której należy zacząć jeżeli chcemy
	 *         pobrać wszystkie statystyki managera
	 */
	public int getNextOffset() {
		return offset + (this.list != null ? this.list.size() : 0);
	}

	/**
	 * @return the {@link #pageSize}
	 */
	public int getPageSize() {
		return pageSize;
	}

}
