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
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.google.gson.Gson;

import xf.ad.model.ADBean;
import xf.ad.model.BasicDataBean;
import xf.ad.model.CommonBean;
import xf.ad.model.QueryResutlBean;
import xf.ad.service.Tools;

public class AreaAction implements IAction {

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
			String starttime = request.getParameter("st");
			String endtime = request.getParameter("et");
			// 分局列表
			String areaWhereString = " where area_code='" + citycode
					+ "'   ";
			String AreaSql = "select subarea_code as code,subarea_name as name from basic_subarea "
					+ areaWhereString;
			List<CommonBean> areaList = runner.query(connection, AreaSql,
					new BeanListHandler<CommonBean>(CommonBean.class));
			// 广告列表
			String AdSql = "select ad_code  as code,ad_name as name,ad_table as tableName from basic_ad_type ";
			List<ADBean> adList = runner.query(connection, AdSql,
					new BeanListHandler<ADBean>(ADBean.class));
			// 遍历所有广告类型，构造数据map<"广告类型",map<"分局",“数量”>>
			Map<Object, Map<String, Object>> mainMap = new HashMap<>();
			Map<String, Object> map = null;
			String countWhereString = "  where stats_time between '" + starttime
					+ "' and '" + endtime + "' and  area_code='" + citycode
					+ "'   ";
			for (ADBean adBeanbean : adList) {
				String mainMap_key = adBeanbean.getCode();
				String tableName = adBeanbean.getTableName();
				String countColumnName = " sum(count)as count ";
				if (tableName.equals("stats_homepage_show")
						|| tableName.equals("stats_homepage_srch")) {
					countColumnName = " sum(count) ||':'|| sum(ncount) as count ";
				}
				String querySql = "select subarea_code as code,"
						+ countColumnName + " from " + tableName
						+ countWhereString + "  group by subarea_code";
				// 查询出数据
				List<QueryResutlBean> resultList = runner.query(connection,
						querySql, new BeanListHandler<>(QueryResutlBean.class));
				map = new HashMap<>();
				for (int i = 0; i < resultList.size(); i++) {
					map.put(resultList.get(i).getCode(), resultList.get(i)
							.getCount());
				}
				mainMap.put(mainMap_key, map);
			}
			resultString = createJson(citycode, areaList, adList, mainMap,
					starttime, endtime);
		} catch (SQLException e) {

			e.printStackTrace();

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

	private String createJson(String cityCode, List<CommonBean> areaList,
			List<ADBean> adList, Map<Object, Map<String, Object>> mainMap,
			String st, String et) {
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
		// 遍历分局表，构造Json字符串。
		for (CommonBean area : areaList) {
			String areaCode = area.getCode();
			String areaName = area.getName();
			// 传入分局参数生成各广告类型的数量存放到对象中。
			jsonBuilder.append("{\"areacode\":\"" + areaCode + "\",");
			jsonBuilder.append("\"areaname\":\"" + areaName + "\",");
			jsonBuilder.append("\"basicrows\":"
					+ getBasicJson(cityCode, areaCode, st, et) + ",");
			jsonBuilder.append("\"rows\":"
					+ getADTypeCounts(areaCode, adList, mainMap) + "},");
		}
		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]}");
		return jsonBuilder.toString();
	}

	private String getBasicJson(String cityCode, String areaCode, String st,
			String et) {
		// 查询地区的基本数据(激活终端、上网人数、活跃终端)
		Connection connection = null;
		String resultString = "";
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "select sum(activeseats_count)as seatscount,sum(online_count)as onlinecount,sum(activeterminal_count) as terminalcount from stats_basicdata  where area_code='"
					+ cityCode
					+ "' and subarea_code='"
					+ areaCode
					+ "' and  statstime between " + st + " and " + et + "";
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

	// /不用根据地市编号查询，原理是根据分局对应的广告数来获取的
	private String getADTypeCounts(String areaCode, List<ADBean> adList,
			Map<Object, Map<String, Object>> mainMap) {
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("[");
		for (ADBean adBean : adList) {
			String adcode = adBean.getCode();
			String count = "0";
			if (mainMap.get(adcode).containsKey(areaCode)) {
				count = mainMap.get(adcode).get(areaCode).toString();
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
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",\"subadcode\":\"1\",");
				jsonBuilder.append("\"count\":\"" + c1 + "\"},");
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",\"subadcode\":\"0\",");
				jsonBuilder.append("\"count\":\"" + c2 + "\"},");
			} else {
				jsonBuilder.append("{\"adcode\":\"" + adcode + "\",\"subadcode\":\"1\",");
				jsonBuilder.append("\"count\":\"" + count + "\"},");
			}
		}
		jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
		jsonBuilder.append("]");
		return jsonBuilder.toString();
	}

}
