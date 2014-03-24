var StartTime = '';
var EndTime = '';
var City = '';
var Bar = '';
$(function() {
			StartTime = unescape(GetQueryString("bgt"));
			EndTime = unescape(GetQueryString("edt"));
			City = unescape(GetQueryString("city"));
			Bar = unescape(GetQueryString("bar"));
			var barname=unescape(GetQueryString("barname"));
			var title = barname + ParseDate(EndTime) + "每小时统计结果";
			$('#lblTitle').text(title);
			LoadData(City);
		});

function LoadData(city) {
	$.messager.progress({
				title : '请您稍等',
				msg : '正在处理,请稍等...'
			});
	$.post('/wxf/ActionService?action=barhour', {
				city : city,
				bar : Bar,
				st : StartTime,
				et : EndTime
			}, function(data) {
				$.messager.progress('close');
				var json = $.parseJSON(data);
				ShowContent(json);
				$("#queryTable").stupidtable();
			});
}

function ShowContent(json) {
	var THead = "<thead><tr><th>日期</th><th data-sort='int'>时间</th>";
	// var THead = "";
	var JsonHead = json.head;
	for (var c in JsonHead) {
		// 主页展现和搜索分为指定的和未指定的
		if (JsonHead[c].name == '主页访问' || JsonHead[c].name == '主页搜索') {
			THead += "<th data-sort='int'>指定" + JsonHead[c].name + "</th><th data-sort='int'>非指定"
					+ JsonHead[c].name + "</th>";
		} else {
			THead += "<th data-sort='int'>" + JsonHead[c].name + "</th>";
		}
	}
	THead += "</tr></thead>";
	var JsonData = json.data;
	var TRs = '<tbody>';
	for (var x in JsonData) {
		var JsonRows = JsonData[x].rows;

		var trsStr = '<tr><td>' + ParseDate(JsonData[x].statstime) + '</td><td>'
				+ JsonData[x].statshour + '</td>';

		for (var y in JsonRows) {
			trsStr += '<td><span style="font-size:16px;">' + JsonRows[y].count
					+ '</span></td>';
		}
		trsStr += '</tr>';
		TRs += trsStr;
	}
	var table = THead + TRs + '</tbody>';
	hStr = "<table id='queryTable'>" + table + "</table>";
	$("#queryRecords").html(hStr);
}
function ParseDate(time) {
	if (time != "" && time != undefined) {
		return time.substr(0, 4) + "年" + time.substr(4, 2) + "月"
				+ time.substr(6, 2) + "日";
	} else {
		return "";
	}
}
function GetQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}
