package xf.ad.service;

import xf.ad.action.*;

public class ActionFactory {
	public static IAction getAction(String actionType) throws Exception {
		switch (actionType) {
		case "combobox":
			return new ComboAction();
		case "city":
			return new CityAction();
		case "cityday":
			return new CityDayAction();
		case "area":
			return new AreaAction();
		case "bar":
			return new BarAction();
		case "barhour":
			return new BarHourAction();
		case "barurl":
			return new BarUrlAction();
		case "showurl":
			return new ShowUrlAction();
		case "jk":
			return new InterfaceAction();
		default:
			throw new Exception("The Undefined ActionName!");
		}
	}
}