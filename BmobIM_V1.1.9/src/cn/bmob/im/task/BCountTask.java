package cn.bmob.im.task;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.CountListener;

/** 封装的个数查询类
  * @ClassName: BCountTask
  * @Description: TODO
  * @author smile
  * @date 2014-6-26 下午5:36:40
  */
public class BCountTask<T> extends QueryTask<T> {

	/**查询个数
	 * @param context：
	 * @param request：请求实体
	 * @param clas ：待查询的JavaBean对象
	 * @param countCallback
	 */
	@SuppressWarnings("unchecked")
	public BCountTask(Context context,BRequest request,Class<T> clas,final CountListener countCallback){
		setCachePolicy(CachePolicy.NETWORK_ONLY);
		//添加查询条件
		if(request.getEqualList()!=null && request.getEqualList().size()>0){
			int size = request.getEqualList().size();
			for(int i=0;i<size;i++){
				BQuery query = request.getEqualList().get(i);
				List<BTable> tables =query.getTable();
				int sizes = tables.size();
				if(sizes>0){
					for(int j=0;j<sizes;j++){
						BTable table =tables.get(j);
						if(query.getType()==BQuery.QueryType.EQUALTO){
						    addWhereEqualTo(table.getTableFiled(), table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NOTEQUALTO){//不相等
							addWhereNotEqualTo(table.getTableFiled(), table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NEAR){
							addWhereNear(table.getTableFiled(), (BmobGeoPoint) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.NOTCONTAINEDIN){//不包含
							addWhereNotContainedIn(table.getTableFiled(), (Collection<? extends Object>) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.CONTAINS){//包含
							addWhereContains(table.getTableFiled(), (String) table.getTableFiledValue()[0]);
						}else if(query.getType()==BQuery.QueryType.WITHINKILOMETERS){//指定100公里范围内的人
							addWhereWithinKilometers(table.getTableFiled(), (BmobGeoPoint) table.getTableFiledValue()[0],(Double)table.getTableFiledValue()[1]);
						}else if(query.getType()==BQuery.QueryType.RELATEDTO){//关联
							addWhereRelatedTo(table.getTableFiled(), (BmobPointer) table.getTableFiledValue()[0]);
						}
					}
				}
			}
		}
		//排序字段
		order(request.getOrderBy());
		//查询个数
		count(context, clas, countCallback);
	}
	
}
