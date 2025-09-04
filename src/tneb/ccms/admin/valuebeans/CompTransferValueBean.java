package tneb.ccms.admin.valuebeans;

import java.io.Serializable;
import java.sql.Timestamp;

import tneb.ccms.admin.model.CompTransferBean;

public class CompTransferValueBean  implements Serializable{
	
	private static final long serialVersionUID = -5494326544707589973L;
	 private Integer id;
	 private Integer complaint;
	 private Timestamp transferOn; 
	 private String transferUser;
	 private String remarks;
     private String ipid;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getComplaint() {
		return complaint;
	}
	public void setComplaint(Integer complaint) {
		this.complaint = complaint;
	}
	public Timestamp getTransferOn() {
		return transferOn;
	}
	public void setTransferOn(Timestamp transferOn) {
		this.transferOn = transferOn;
	}
	public String getTransferUser() {
		return transferUser;
	}
	public void setTransferUser(String transferUser) {
		this.transferUser = transferUser;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getIpid() {
		return ipid;
	}
	public void setIpid(String ipid) {
		this.ipid = ipid;
	}
     
     

public CompTransferValueBean convertBeanToValueBean(CompTransferBean compTransferBean) {
		
	CompTransferValueBean compTransferValueBean = new CompTransferValueBean();
		
		if(compTransferValueBean != null) {
			compTransferValueBean.setId(compTransferBean.getId());
			compTransferValueBean.setComplaint(compTransferBean.getComplaint().getId());
			compTransferValueBean.setIpid(compTransferBean.getIpid());
			compTransferValueBean.setRemarks(compTransferBean.getRemarks());
			compTransferValueBean.setTransferOn(compTransferBean.getTransferOn());
		
		}
		
		return compTransferValueBean;
	}

	


}
