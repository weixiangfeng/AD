var StartTime = '';
var EndTime = '';
var City = '';
var CityName = '';
$(function() {
			StartTime = unescape(GetQueryString("bgt"));
			EndTime = unescape(GetQueryString("edt"));
			City = unescape(GetQueryString("city"));
			CityName = unescape(GetQueryString("cityname"));
			var title = CityName + '市' + ParseDate(StartTime) + "至"
					+ ParseDate(EndTime) + "各分局统计结果";
			$('#lblTitle').text(title);
			LoadData(City);
		});

function LoadData(city) {
	$.messager.progress({
				title : '请您稍等',
				msg : '正在处理,请稍等...'
			});
	$.post('/wxf/ActionService?action=area', {
				city : city,
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
	var THead = "<thead><tr><th>分局</th><th data-sort='int'>装机量</th><th data-sort='int'>上机人次</th><th data-sort='int'>活跃终端数</th>";
	// var THead = "";
	var JsonHead = json.head;
	for (var c in JsonHead) {
		// 主页展现和搜索分为指定的和未指定的
		if (JsonHead[c].name == '主页访问' || JsonHead[c].name == '主页搜索') {
			THead += "<th data-sort='int'>指定" + JsonHead[c].name
					+ "</th><th data-sort='int'>非指定" + JsonHead[c].name
					+ "</th>";
		} else {
			THead += "<th data-sort='int'>" + JsonHead[c].name + "</th>";
		}
	}
	THead += "</tr></thead>";

	var JsonData = json.data;
	var TRs = '<tbody>';
	for (var x in JsonData) {
		var JsonRows = JsonData[x].rows;
		var BasicRows = JsonData[x].basicrows;
		var trsStr = '<tr><td><a href="javascript:void(0)" style="color:blue;" onclick="showBar(\''
				+ JsonData[x].areacode
				+ '\',\''
				+ JsonData[x].areaname
				+ '\')">' + JsonData[x].areaname + '</a></td>';
		var seatscount = '0';
		var onlinecount = '0';
		var terminalcount = '0';
		if (BasicRows[0].seatscount != undefined) {
			seatscount = BasicRows[0].seatscount;
		}
		if (BasicRows[0].onlinecount != undefined) {
			onlinecount = BasicRows[0].onlinecount;
		}
		if (BasicRows[0].terminalcount != undefined) {
			terminalcount = BasicRows[0].terminalcount;
		}
		trsStr += '<td>' + seatscount + '</td><td>' + onlinecount + '</td><td>'
				+ terminalcount + '</td>';

		for (var y in JsonRows) {
			trsStr += '<td><a href="javascript:void(0)" style="color:blue;" onclick="openURL(\''
					+ JsonData[x].areacode
					+ '\',\''
					+ JsonData[x].areaname
					+ '\',\''
					+ JsonRows[y].adcode
					+ '\',\''
					+ JsonRows[y].subadcode
					+ '\')"><span style="font-size:16px;color:green;">'
					+ JsonRows[y].count + '</span></a></td>';
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

function showBar(areacode, areaname) {
	var opage = window.open("Bar.html?city=" + City + "&area=" + areacode
			+ "&areaname=" + escape(areaname) + "&bgt=" + escape(StartTime)
			+ "&edt=" + escape(EndTime) + "");
	opage.focus();
}

function openURL(code, showname, ad, subad) {
	var opage = window.open("ShowUrl.html?city=" + City + "&code=" + code
			+ "&type=area&showname=" + escape(showname) + "&ad=" + ad
			+ "&subad=" + subad + "&bgt=" + escape(StartTime) + "&edt="
			+ escape(EndTime) + "");
	opage.focus();
}