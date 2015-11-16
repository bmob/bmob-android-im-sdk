package cn.bmob.im.task;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;

/** 封装好的查询类
  * @ClassName: BFindTask
  * @Description: TODO
  * @author smile
  * @date 2014-6-27 上午11:48:25
  * @param <T>
  */
public class BFindTask<T> extends QueryTask<T> {
	
	/**携带限制条件-----分页加载
	 * @param tableName
	 * @param request
	 * @param callback
	 */
	@SuppressWarnings("unchecked")
	public BFindTask(Context context,BRequest request,final FindListener<T> findCallback){
		//刷新模式下--先从缓存中取，随后每次刷新就从网络获取最新的存入缓存中
//		if(request.isRefresh()){
//			BmobLog.i("BFindTask 缓存");
//			setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
//		}else{
//			BmobLog.i("BFindTask 网络");
//			setCachePolicy(CachePolicy.NETWORK_ONLY);
//		}
		setCachePolicy(CachePolicy.NETWORK_ONLY);
		//如果缓存策略不为空，则设置此缓存策略--因为有可能用户自定义其缓存策略
		if(request.getCachePolicy()!=null){
			setCachePolicy(request.getCachePolicy());
		}else{
			setLimit(request.getLimitLength());
		}
		//添加查询条件
		if(request.getEqualList()!=null && request.getEqualList().size()>0){
			int total = request.getEqualList().size();
			for(int i=0;i<total;i++){
				BQuery query = request.getEqualList().get(i);
				List<BTable> tables =query.getTable();
				int sizes = tables.size();
				if(sizes>0){
					for(int j=0;j<sizes;j++){
						BTable table =tables.get(j);
						if(query.getType()==BQuery.QueryType.EQUALTO){//相等
						    addWhereEqualTo(table.getTableFiled(), table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NOTEQUALTO){//不相等
							addWhereNotEqualTo(table.getTableFiled(), table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NOTCONTAINEDIN){//不包含
							addWhereNotContainedIn(table.getTableFiled(), (Collection<? extends Object>) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.CONTAINS){//包含
							addWhereContains(table.getTableFiled(), (String) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.RELATEDTO){//关联
							addWhereRelatedTo(table.getTableFiled(), (BmobPointer) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NEAR){//附近的人
							addWhereNear(table.getTableFiled(), (BmobGeoPoint) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.WITHINKILOMETERS){//指定范围内的人
							addWhereWithinKilometers(table.getTableFiled(), (BmobGeoPoint) table.getTableFiledValue()[0],(Double)table.getTableFiledValue()[1]);
						}
					}
				}
			}
		}
		//加载更多
		if(request.isLoadMore()){
			setSkip(request.getSkipPage()*request.getLimitLength());
		}
		//排序字段
		if(request.getOrderBy()!=null && !request.getOrderBy().equals("")){
			order(request.getOrderBy());
		}
		//查询
		findObjects(context, findCallback);
	}
	
	
}
