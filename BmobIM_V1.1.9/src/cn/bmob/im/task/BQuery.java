package cn.bmob.im.task;

import java.io.Serializable;
import java.util.List;

/**
 * 查询对象
 * 
 * @ClassName: BQuery
 * @Description: TODO
 * @author smile
 * @date 2014-6-26 下午5:15:06
 */
public class BQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 查询类型
	 */
	private int type;

	/**
	 * 该类型下的查询条件集合：有可能这个查询类型下有多个查询条件：比如想查询username等于A和查询username等于B的
	 */
	private List<BTable> table;

	public BQuery(int type,List<BTable> table){
		this.type = type;
		this.table = table;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<BTable> getTable() {
		return table;
	}

	public void setTable(List<BTable> table) {
		this.table = table;
	}

	/**
	 * Supported 查询类型:
	 */
	public interface QueryType {
		int EQUALTO = 0;//相等
		int NOTEQUALTO = 1;//不相等
		int CONTAINS = 2;//不包含
		int NOTCONTAINEDIN = 3;//不包含
		int RELATEDTO = 4;//关联对象：查询某关联字段
		int NEAR = 5;//附近的人
		int WITHINKILOMETERS = 6;//指定范围内的人
	}

}
