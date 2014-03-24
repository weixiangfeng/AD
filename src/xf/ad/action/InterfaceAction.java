package xf.ad.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import xf.ad.model.ADBean;
import xf.ad.model.CommonBean;
import xf.ad.model.QueryResutlBean;
import xf.ad.service.Tools;

public class InterfaceAction implements IAction {

	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {
		String resultsString = null;
		Connection connection = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			// 获取参数
			String citycode = request.getParameter("city");
			String starttime = request.getParameter("st");
			String endtime = request.getParameter("et");
			
			if(citycode==null||starttime==null||endtime==null)
			{
				resultsString="param error";
				return resultsString;
			}
			
			// Sql
			String BarSql = "select netbar_code as code,netbar_name as name from basic_netbar where area_code='"
					+ citycode + "' ";
			String AdSql = "select ad_code  as code,ad_name as name,ad_table as tableName from basic_ad_type ";
			// 连接数据库读取数据
			List<ADBean> adList = runner.query(connection, AdSql,
					new BeanListHandler<ADBean>(ADBean.class));
			List<CommonBean> barList = runner.query(connection, BarSql,
					new BeanListHandler<CommonBean>(CommonBean.class));
			if (barList.size() < 1) {
				resultsString="no data";
				return resultsString;
			}
			// 遍历所有广告类型，构造数据map<"广告类型",map<"网吧","数量">>
			Map<Object, Map<String, Object>> mainMap = new HashMap<>();
			Map<String, Object> map = null;
			for (ADBean adBeanbean : adList) {
				String mainMap_key = adBeanbean.getCode();
				String tableName = adBeanbean.getTableName();
				String countColumnName = " sum(count)as count ";
				if (mainMap_key.equals("2") || mainMap_key.equals("3")) {
					countColumnName = " sum(count) ||':'|| sum(ncount) as count ";
				}
				String querySql = "select bar_code as code," + countColumnName
						+ " from " + tableName + "  where stats_time between '"
						+ starttime + "' and '" + endtime + "' and  area_code='"
						+ citycode + "'  group by bar_code";
				// 查询出数据
				List<QueryResutlBean> resultList = runner.query(connection,
						querySql, new BeanListHandler<>(QueryResutlBean.class));
				map = new HashMap<>();
				for (int i = 0; i < resultList.size(); i++) {
					map.put(resultList.get(i).getCode(), resultList.get(i)
							.getCount());
				}
				mainMap.put(mainMap_key, map);
			}//end MainMap
			StringBuilder jsonBuilder = new StringBuilder();
		
			for (CommonBean bar : barList) {
				String barCode = bar.getCode();
				// 传入网吧参数生成各广告类型的数量存放到对象中。
				jsonBuilder.append("$" + barCode + "");
				for (ADBean adBean : adList) {
					String adcode = adBean.getCode();
					String count = "0";
					if (mainMap.get(adcode).containsKey(barCode)) {
						count = mainMap.get(adcode).get(barCode).toString();
					}
					// 主页展现和搜索特殊处理
					if (adcode.equals("2") || adcode.equals("3")) {
						String c1 = "0";
						String c2 = "0";
						if (!count.equals("0")) {
							// 以：分解count变量，赋值给c1,c2
							String[] cArray = count.split(":");
							c1 = cArray[0];
							c2 = cArray[1];
						}
					
						jsonBuilder.append("," + c1 + "");
						
						jsonBuilder.append("," + c2 + "");
					} else {
						
						jsonBuilder.append("," + count + "");
					}
				}			
			}
			if (jsonBuilder.toString().startsWith("$"))// 去掉最后一列后面的逗号。
			{
				jsonBuilder.deleteCharAt(0);
			}
			resultsString=jsonBuilder.toString();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resultsString;
	}
}
