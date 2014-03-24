package xf.ad.model;

public class CommonBean {
	public CommonBean() {
		super();
	}

	public CommonBean(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}

	private String code;
	private String name;

	public void setCode(String code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

}
