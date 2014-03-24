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

public class BarHourAction implements IAction {

	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {

		Connection connection = null;
		String resultString = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			// 获取参数
			String citycode = request.getParameter("city");
			String barcode = request.getParameter("bar");
			String st = request.getParameter("st");
			String et = request.getParameter("et");

			// 广告列表
			String AdSql = "select ad_code  as code,ad_name as name,ad_sourcetable as tableName from basic_ad_type ";
			List<ADBean> adList = runner.query(connection, AdSql,
					new BeanListHandler<ADBean>(ADBean.class));

			// 遍历所有广告类型，构造map<"广告类型",map<"日期:小时",“数量”>>
			Map<Object, Map<String, Object>> mainMap = new HashMap<>();
			Map<String, Object> map = null;
			String countWhereString = "  where  area='" + citycode
					+ "'  and barno='" + barcode + "' ";
			for (ADBean adBeanbean : adList) {
				String adCode = adBeanbean.getCode();
				String tableName = adBeanbean.getTableName()+et;
				String countColumnName = " sum(showcount)as count ";
				if(adCode.equals("1"))
				{
					countColumnName="sum(case  when perch=1 then 1 else 0 end)as count";
				}
				if (adCode.equals("2")|| adCode.equals("3")) {
					countColumnName = " sum(case when perch=1 then 1*showcount else 0 end) ||':'|| sum(case  when perch=0 then 1*showcount else 0 end) as count ";
				}
				String querySql = "select "+et+" as statstime,substr(reporttime,9,2) as statshour,"
						+ countColumnName
						+ " from "
						+ tableName
						+ countWhereString + "  group by  substr(reporttime,9,2)";
				// 查询出数据
				List<QueryResutlBean> resultList = runner.query(connection,
						querySql, new BeanListHandler<>(QueryResutlBean.class));
				map = new HashMap<>();
				for (int i = 0; i < resultList.size(); i++) {
					// 获取时间和小时组成key
					String timeString = resultList.get(i).getStatstime();
					String hourString = resultList.get(i).getStatshour();
					String keyString = timeString + ":" + hourString;//10点之前的小时前面带0
					map.put(keyString, resultList.get(i).getCount());
				}
				mainMap.put(adCode, map);
			}
			resultString = createJson(adList, mainMap, st, et);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	private String createJson(List<ADBean> adList,
			Map<Object, Map<String, Object>> mainMap, String st, String et) {
		// 构造Json
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{\"head\":[");
		for (CommonBean adType : adList) {
			jsonBuilder.append("{\"name\":\"" + adType.getName() + "\"},");
		}
		if (jsonBuilder.toString().endsWith(","))// 去掉最后一列后面的逗号。
		{
			jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		}
		jsonBuilder.append("],");// end head

		jsonBuilder.append("\"data\":[");
		// 遍历一天的24小时，构造Json字符串。
		String statsTime = et;
		for (int i = 0; i < 24; i++) {
			String statsHour = Integer.toString(i);
			if(i<10)
			{
				statsHour="0"+Integer.toString(i);
			}
			String keyString = statsTime + ":" + statsHour;
			jsonBuilder.append("{\"statstime\":\"" + statsTime + "\",");
			jsonBuilder.append("\"statshour\":\"" + statsHour + "\",");
			jsonBuilder.append("\"rows\":"
					+ getADTypeCounts(keyString,adList, mainMap) + "},");
		}
		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]}");
		return jsonBuilder.toString();
	}

	private String getADTypeCounts(String statasTimeHour, List<ADBean> adList,
			Map<Object, Map<String, Object>> mainMap) {
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("[");
		for (ADBean adBean : adList) {
			String adcode = adBean.getCode();
			String count = "0";
			if (mainMap.get(adcode).containsKey(statasTimeHour)) {
				count = mainMap.get(adcode).get(statasTimeHour).toString();
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
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",");
				jsonBuilder.append("\"count\":\"" + c1 + "\"},");
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",");
				jsonBuilder.append("\"count\":\"" + c2 + "\"},");
			} else {
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",");
				jsonBuilder.append("\"count\":\"" + count + "\"},");
			}
		}
		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]");
		return jsonBuilder.toString();
	}
}
