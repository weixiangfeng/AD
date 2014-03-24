$(function() {
			var curDate = new Date();
			var year = curDate.getFullYear();
			var month = curDate.getMonth() + 1;
			var day = curDate.getDate() - 1;
			var time = year + (month < 10 ? ('0' + month) : month)
					+ (day < 10 ? ('0' + day) : day);
			$('#dateStart').datebox('setValue', time);
			$('#dateEnd').datebox('setValue', time);
			loadADType();
			LoadArea();
			InitGrid();
			$('#btnQuery').click(Query);
		});

function InitGrid() {
	$('#grid').datagrid({
				nowrap : false,
				singleSelect : true,
				columns : [[{
							field : 'barCode',
							title : '网吧名',
							align : 'center',
							width : fixWidth(0.2)
						}, {
							field : 'adCode',
							title : '广告位',
							align : 'center',
							width : fixWidth(0.2)
						}, {
							field : 'url',
							title : 'URL',
							align : 'center',
							width : fixWidth(0.5)
						}, {
							field : 'showCount',
							title : '使用次数',
							align : 'center',
							width : fixWidth(0.1)
						}]],
				pagination : false,
				rownumbers : true,
				fitColumns : true,
				fit : true
			});
}

function Query() {
	var ad= $('#ccAD').combobox('getValue');
	var city = $('#ccArea').combobox('getValue');
	var netbar = $('#ccNetbar').combobox('getValue');
	var st = $('#dateStart').datebox('getValue');
	var et = $('#dateEnd').datebox('getValue');
	//判断数据准确性。
	$("#grid").datagrid({
				pageNumber : 1,
				url : '/wxf/ActionService?action=bar',
				queryParams : {
					ad : ad,
					st : st,
					et : et,
					city : city,
					barcode:netbar
				}
			});
}

function LoadData(city, netbar) {
	$("#grid").datagrid({
				pageNumber : 1,
				url : '/wxf/ActionService?action=barurl',
				queryParams : {
					ad : ad,
					city : city,
					barcode:netbar,
					bgt : bgt,
					edt : edt
				}
			});
}

function loadADType() {
	$('#ccAD').combobox({
				url : '/wxf/ActionService?action=combobox&type=adtype&addselect=0',
				valueField : 'code',
				textField : 'name',
				panelHeight : '350',
				width : '133',
				editable : false,
				onLoadSuccess : function() {
					var data = $('#ccAD').combobox('getData');
					if (data.length > 0) {
						$("#ccAD").combobox('select', data[0].code);
					}
				}
			});
}

function LoadArea() {
	$('#ccSubArea').combobox({
				width : '133'
			});
	$('#ccArea').combobox({
				url : '/wxf/ActionService?action=combobox&type=area',
				valueField : 'code',
				textField : 'name',
				panelHeight : '400',
				width : '133',
				onChange : function(newValue, oldValue) {
					loadSubArea(newValue);
				},
				editable : false,
				onLoadSuccess : function() {
					var data = $('#ccArea').combobox('getData');
					if (data.length > 0) {
						$("#ccArea").combobox('select', data[0].code);
					}
				}
			});
}
// 通过地区号去查询分局
function loadSubArea(areaCode) {
	if (areaCode == '0') {
		$('#ccSubArea').combobox('clear');
	} else {
		var paramStr = '&type=subarea&areacode=' + areaCode + '';
		$('#ccSubArea').combobox({
					url : '/wxf/ActionService?action=combobox' + paramStr + '',
					valueField : 'code',
					textField : 'name',
					panelHeight : '300',
					width : '133',
					editable : false,
					onChange : function(newValue, oldValue) {
						loadPS(areaCode, newValue);
					},
					onLoadSuccess : function() {
						var data = $('#ccSubArea').combobox('getData');
						if (data.length > 0) {
							$("#ccSubArea").combobox('select', data[0].code);
						}
					}
				});
	}
}

function loadPS(areaCode, subAreaCode) {
	if (subAreaCode == '0') {
		$('#ccPS').combobox('clear');
	} else {
		var paramStr = '&type=ps&areacode=' + areaCode + '&subareacode='
				+ subAreaCode + '';
		$('#ccPS').combobox({
					url : '/wxf/ActionService?action=combobox' + paramStr + '',
					valueField : 'code',
					textField : 'name',
					panelHeight : '300',
					width : '133',
					editable : false,
					onChange : function(newValue, oldValue) {
						loadNetbar(areaCode, newValue);
					},
					onLoadSuccess : function() {
						var data = $('#ccPS').combobox('getData');
						if (data.length > 0) {
							$("#ccPS").combobox('select', data[0].code);
						}
					}
				});
	}
}

function loadNetbar(areaCode, psCode) {
	if (psCode == '0') {
		$('#ccNetbar').combobox('clear');
	} else {
		var paramStr = '&type=netbar&areacode=' + areaCode + '&pscode='
				+ psCode + '';
		$('#ccNetbar').combobox({
					url : '/wxf/ActionService?action=combobox' + paramStr + '',
					valueField : 'code',
					textField : 'name',
					panelHeight : '300',
					width : '153',
					editable : true,
					onLoadSuccess : function() {
						var data = $('#ccNetbar').combobox('getData');
						if (data.length > 0) {
							$("#ccNetbar").combobox('select', data[0].code);
						}
					}
				});
	}
}

function fixWidth(percent) {
	return (document.body.clientWidth) * percent;
}
