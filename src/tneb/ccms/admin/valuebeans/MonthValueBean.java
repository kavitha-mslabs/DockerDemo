package tneb.ccms.admin.valuebeans;

public class MonthValueBean {
	private String label;
    private String value;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public MonthValueBean(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
	public MonthValueBean() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    

}
