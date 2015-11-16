package cn.bmob.im.task;

import java.io.Serializable;

/** 查询的条件
  * @ClassName: BTable
  * @Description: TODO
  * @author smile
  * @date 2014-6-26 下午4:46:33
  */
public class BTable implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1529852555219097018L;
	private String tableFiled; // 字段名
	private Object[] tableFiledValue;// 字段值

	public BTable(String tableFiled,Object[] tableFiledValue){
		this. tableFiled =  tableFiled;
		this. tableFiledValue =  tableFiledValue;
	}
	public String getTableFiled() {
		return tableFiled;
	}

	public void setTableFiled(String tableFiled) {
		this.tableFiled = tableFiled;
	}

	public Object[] getTableFiledValue() {
		return tableFiledValue;
	}

	public void setTableFiledValue(Object[] tableFiledValue) {
		this.tableFiledValue = tableFiledValue;
	}

}
