package tneb.ccms.admin.valuebeans;
 
import java.io.Serializable;
 
import tneb.ccms.admin.model.CompDeviceBean;
 
public class CompDeviceValueBean implements Serializable{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String deviceCode;
	private String deviceName;
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
    public static CompDeviceBean convertCompDeviceValueBeanToCompDeviceBean(CompDeviceValueBean compDeviceValueBean) {
    	CompDeviceBean compDeviceBean = new CompDeviceBean();
    	compDeviceBean.setDevCode(compDeviceValueBean.getDeviceCode());
    	compDeviceBean.setDevName(compDeviceValueBean.getDeviceName());
		return compDeviceBean;
	}
    public static CompDeviceValueBean convertCompDeviceBeanToCompDeviceValueBean(CompDeviceBean compDeviceBean) {
    	CompDeviceValueBean compDeviceValueBean = new CompDeviceValueBean();
    	compDeviceValueBean.setDeviceCode(compDeviceBean.getDevCode());
    	compDeviceValueBean.setDeviceName(compDeviceBean.getDevName());
		return compDeviceValueBean;
	}
	public CompDeviceValueBean(String deviceCode, String deviceName) {
		super();
		this.deviceCode = deviceCode;
		this.deviceName = deviceName;
	}
	public CompDeviceValueBean() {
		super();
		// TODO Auto-generated constructor stub
	}

 
}