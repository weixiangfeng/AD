package xf.ad.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import xf.ad.model.ADBean;
import xf.ad.model.BasicDataBean;
import xf.ad.model.CommonBean;
import xf.ad.model.QueryResutlBean;
import xf.ad.service.Tools;

import com.google.gson.Gson;

public class CityDayAction implements IAction {
	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {
		Connection connection = null;
		String resultString = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();

			ResultSetHandler<List<ADBean>> adHandler = new BeanListHandler<ADBean>(
					ADBean.class);
			// 获取参数
			String citycode = request.getParameter("city");
			// String adtype = request.getParameter("adtype");
			String starttime = request.getParameter("st");
			String endtime = request.getParameter("et");
			// String citycode = "3702";
			String adtype = "0";
			// String starttime = "20130910";
			// String endtime = "20130925";

			// 构造查询语句

			String adtypeWhereString = "";
			String countWhereString = " where stats_time between '" + starttime
					+ "' and '" + endtime
					+ "'  and  area_code='" + citycode
					+ "' ";

			if (!adtype.equals("0")) {
				adtypeWhereString = " where ad_code='" + adtype + "' ";
			}

			String adSql = "select ad_code  as code,ad_name as name,ad_table as tableName from basic_ad_type "
					+ adtypeWhereString + " order by sort";

			List<ADBean> adList = runner.query(connection, adSql, adHandler);

			Object cityname = runner.query(connection,
					"select  area_name from area where area_code=?",
					new ScalarHandler<Object>(), citycode);

			// 遍历所有广告表数据，构造数据map<"广告类型",map<"日期(天)",“数量”>>
			Map<Object, Map<String, Object>> mainMap = new HashMap<>();
			Map<String, Object> map = null;
			for (ADBean adBeanbean : adList) {
				String mainMap_key = adBeanbean.getCode();
				String tableName = adBeanbean.getTableName();
				String countColumnName = " sum(count)as count ";
				if (tableName.equals("stats_homepage_show")
						|| tableName.equals("stats_homepage_srch")) {
					countColumnName = " sum(count) ||':'|| sum(ncount) as count  ";
				}
				String querySql = "select stats_time as statstime,"
						+ countColumnName + " from " + tableName
						+ countWhereString + "  group by stats_time";
				// 查询出数据
				List<QueryResutlBean> resultList = runner.query(connection,
						querySql, new BeanListHandler<>(QueryResutlBean.class));
				map = new HashMap<>();
				for (int i = 0; i < resultList.size(); i++) {
					map.put(resultList.get(i).getStatstime(), resultList.get(i)
							.getCount());
				}
				mainMap.put(mainMap_key, map);
			}
			// 生成Json
			resultString = createJson(citycode, cityname, adList, mainMap,
					starttime, endtime);

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

	private String createJson(String cityCode, Object cityname,
			List<ADBean> adList, Map<Object, Map<String, Object>> mainMap,
			String st, String et) throws ParseException {
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

		// 根据时间差，来循环
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date sDate = format.parse(st);
		Date edDate = format.parse(et);
		long subtimes = edDate.getTime() - sDate.getTime();
		long c = subtimes / (24 * 60 * 60 * 1000);// 天
		for (int i = 0; i <= c; i++) {

			Date currentDate = new Date(sDate.getTime() + i * 24 * 60 * 60
					* 1000);
			String currentDateStr = format.format(currentDate);
			jsonBuilder.append("{\"citycode\":\"" + cityCode + "\",");
			jsonBuilder.append("\"cityname\":\"" + cityname + "\",");
			jsonBuilder.append("\"day\":\"" + currentDateStr + "\",");
			jsonBuilder.append("\"week\":\"" + getWeekDayString(currentDate) + "\",");
			jsonBuilder.append("\"basicrows\":"
					+ getBasicJson(cityCode, currentDateStr) + ",");
			jsonBuilder.append("\"rows\":"
					+ getADTypeCounts(currentDateStr, adList, mainMap) + "},");

		}

		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]}");
		return jsonBuilder.toString();
	}

	public String getWeekDayString(Date date)
	{
		String weekString = "";
		String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		weekString = dayNames[dayOfWeek - 1];
		return weekString;
	}

	private String getBasicJson(String cityCode, String currentDateString) {
		// 查询地区的基本数据(激活终端、上网人数、活跃终端)
		Connection connection = null;
		String resultString = "";
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "select sum(activeseats_count)as seatscount,sum(online_count)as onlinecount,sum(activeterminal_count) as terminalcount from stats_basicdata  where area_code='"
					+ cityCode + "' and  statstime = " + currentDateString + "";
			ResultSetHandler<List<BasicDataBean>> basicHandler = new BeanListHandler<BasicDataBean>(
					BasicDataBean.class);
			List<BasicDataBean> basicList = runner.query(connection, sql,
					basicHandler);
			Gson gson = new Gson();
			String jsonString = gson.toJson(basicList);
			resultString = jsonString;
		} catch (SQLException e) {
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

	private String getADTypeCounts(String dayString, List<ADBean> adList,
			Map<Object, Map<String, Object>> mainMap) {
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("[");
		for (ADBean adBean : adList) {
			String adcode = adBean.getCode();
			String count = "0";
			if (mainMap.get(adcode).containsKey(dayString)) {
				count = mainMap.get(adcode).get(dayString).toString();
			}
			if (adcode.equals("2") || adcode.equals("3")) {
				String c1 = "0";
				String c2 = "0";
				if (!count.equals("0")) {
					// 以：分解count变量，赋值给c1,c2
					String[] cArray = count.split(":");
					c1 = cArray[0];
					c2 = cArray[1];
				}
				jsonBuilder.append("{\"adcode\":\"" + adcode
						+ "\",\"subadcode\":\"1\",");
				jsonBuilder.append("\"count\":\"" + c1 + "\"},");
				jsonBuilder.append("{\"adcode\":\"" + adcode
						+ "\",\"subadcode\":\"0\",");
				jsonBuilder.append("\"count\":\"" + c2 + "\"},");
			} else {
				jsonBuilder.append("{\"adcode\":\"" + adcode
						+ "\",\"subadcode\":\"1\",");
				jsonBuilder.append("\"count\":\"" + count + "\"},");
			}
		}
		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]");
		return jsonBuilder.toString();
	}
}
