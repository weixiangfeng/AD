package xf.ad.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import xf.ad.model.UrlBean;
import xf.ad.service.Tools;

import com.google.gson.Gson;

public class BarUrlAction implements IAction {
	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {
		// 显示网吧URL的数量，每种广告位不同，可以分为三种显示类型，倘若查看所有类型的话，那么就没法显示了。
		Connection connection = null;
		String resultString = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			ResultSetHandler<List<UrlBean>> urlHandler = new BeanListHandler<UrlBean>(UrlBean.class);
			// 获取参数
			String st = request.getParameter("st");
			String et = request.getParameter("et");
			String city = request.getParameter("city");
			String barcode = request.getParameter("barcode");
			String ad = request.getParameter("ad");
			List<UrlBean> listResult = new ArrayList<>();
			if (ad.equals("0")) {
				// 遍历所有广告类型，
			} else {
				// 获取表名称，
				Object tableName = runner.query(connection,
						"select ad_table  from basic_ad_type where ad_code=?",
						new ScalarHandler<Object>(), ad);
				String selectColumnName = "url";
				String groupByColumnName = "url";
				
				String sqlString = "select bar_code as barcode,"
						+ selectColumnName + ",sum(count)as showcount from "
						+ tableName.toString() + " where area_code='" + city
						+ "' and bar_code='" + barcode
						+ "' and stats_time between '" + st + "' and '" + et
						+ "'  group by bar_code," + groupByColumnName + "";
				List<UrlBean> urlList = runner.query(connection, sqlString,
						urlHandler);
				listResult.addAll(urlList);
			}

			int size = listResult.size();
			Gson gson = new Gson();
			String jsonString = gson.toJson(listResult);
			StringBuilder jsonBuilder = new StringBuilder();
			jsonBuilder.append("{\"total\":\"" + size + "\",");
			jsonBuilder.append("\"rows\":");
			jsonBuilder.append(jsonString);
			jsonBuilder.append("}");
			resultString = jsonBuilder.toString();

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
}
