package cn.bmob.im.task;

import java.util.List;

import cn.bmob.v3.BmobQuery.CachePolicy;

/**获取各种查询的参数
  * @ClassName: BRequest
  * @Description: TODO
  * @author smile
  * @date 2014-6-26 下午4:25:56
  * @param <T>
  */
public abstract class BRequest {

	private List<BQuery> equalList;// 查询条件
	private boolean isRefresh;// 刷新模式--用于区分缓存策略
	private boolean isLoadMore;// 加载更多

	private String orderBy;// 排序字段
	private CachePolicy cachePolicy;// 缓存策略

	private int limitLength;// 限制查询个数
	private int skipPage;// 跳过的页数-分页加载

	/**
	 * 每页查询的个数默认为10
	 */
	public static int QUERY_LIMIT_COUNT = 10;
	
	public interface Field {
		String DEFAULT_ORDER_BY = "-updatedAt";
	}

	public interface Model {
		int COMMON = 1;// 普通模式
		int REFRESH = 2;// 下拉刷新模式
	}

	/**
	 * 普通查询->默认初始值：缓存策略为网络获取，排序默认按照updateAt字段降序排列
	 * @param isDescending
	 * @param orderby
	 * @param cachePolicy
	 */
	public BRequest(String orderby,CachePolicy cachePolicy) {
		this.orderBy = orderby;
		this.cachePolicy = cachePolicy;
	}
	
	public BRequest(String orderby,CachePolicy cachePolicy,int limitLength) {
		this.orderBy = orderby;
		this.cachePolicy = cachePolicy;
		this.limitLength = limitLength;
	}

	/** 适合普通查询
	 * @param equalList
	 * @param cachePolicy
	 */
	public BRequest(List<BQuery> equalList, CachePolicy cachePolicy) {
		this("", cachePolicy);
		this.equalList = equalList;
	}
	
	/**普通查询-指定排序方式和缓存策略
	 * @param equalList
	 * @param isDescending
	 * @param orderby
	 * @param cachePolicy
	 */
	public BRequest(List<BQuery> equalList,String orderby,CachePolicy cachePolicy ) {
		this(orderby, cachePolicy);
		this.equalList = equalList;
	}
	
	/**适合查询数量-默认其采用排序规则："-updateAt"，缓存策略是CachePolicy.NETWORK_ONLY(只读取网络数据)
	 * @param equalList
	 * 默认按照updateAt降序排列、缓存策略设为NETWORK_ELSE_CACHE
	 */
	public BRequest(List<BQuery> equalList) {
		this(Field.DEFAULT_ORDER_BY,  CachePolicy.NETWORK_ONLY);
		this.equalList = equalList;
	}
	
	/**适合查询数量--指定排序规则，因为有些查询是不需要指定排序方式的，比如查询附近的人，其默认已经按照查询位置远近排序
	 * @param equalList
	 * 默认按照updateAt降序排列、缓存策略设为NETWORK_ELSE_CACHE
	 */
	public BRequest(String orderby,List<BQuery> equalList) {
		this(orderby,  CachePolicy.NETWORK_ONLY);
		this.equalList = equalList;
	}
	
	/**分页查询
	 * @param isRefresh：是否属于下拉或者上拉刷新动作
	 * @param isLoadMore：是否加载更多
	 * @param equalList：查询条件
	 * @param skipPage：查询页码
	 * 默认按照updateAt降序排列、每页查询数为QUERY_LIMIT_COUNT=10
	 */
	public BRequest(boolean isRefreshAction, boolean isLoadMore,List<BQuery> equalList, int skipPage) {
		this("", null,QUERY_LIMIT_COUNT);
		this.equalList = equalList;
		this.skipPage = skipPage;
		this.isRefresh = isRefreshAction;
		this.isLoadMore = isLoadMore;
	}
	
	/**分页查询-指定排序方式，有些查询不需要排序
	 * @param isRefresh：是否属于下拉或者上拉刷新动作
	 * @param isLoadMore：是否加载更多
	 * @param equalList：查询条件
	 * @param skipPage：查询页码
	 * 默认按照updateAt降序排列、每页查询数为QUERY_LIMIT_COUNT=10
	 */
	public BRequest(String orderBy,boolean isRefreshAction, boolean isLoadMore,List<BQuery> equalList, int skipPage) {
		this(orderBy, null,QUERY_LIMIT_COUNT);
		this.equalList = equalList;
		this.skipPage = skipPage;
		this.isRefresh = isRefreshAction;
		this.isLoadMore = isLoadMore;
	}
	
	/**适合刷新
	 * @param orderby
	 * @param equalList
	 * 默认按照updateAt降序排列、缓存策略设为NETWORK_ELSE_CACHE
	 */
	public BRequest(boolean isRefresh,List<BQuery> equalList ) {
		this(Field.DEFAULT_ORDER_BY, CachePolicy.NETWORK_ELSE_CACHE);
		this.equalList = equalList;
		this.isRefresh = isRefresh;
	}
	
	public void setEqualList(List<BQuery> equalList) {
		this.equalList = equalList;
	}

	public void setRefresh(boolean isRefresh) {
		this.isRefresh = isRefresh;
	}

	public void setLoadMore(boolean isLoadMore) {
		this.isLoadMore = isLoadMore;
	}

	public boolean isLoadMore() {
		return isLoadMore;
	}

	public boolean isRefresh() {
		return isRefresh;
	}

	public int getSkipPage() {
		return skipPage;
	}

	public void setSkipPage(int skipPage) {
		this.skipPage = skipPage;
	}

	public List<BQuery> getEqualList() {
		return equalList;
	}

	public CachePolicy getCachePolicy() {
		return cachePolicy;
	}

	public void setCachePolicy(CachePolicy cachePolicy) {
		this.cachePolicy = cachePolicy;
	}

	public int getLimitLength() {
		return limitLength;
	}

	public void setLimitLength(int limitLength) {
		this.limitLength = limitLength;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

}
