package tneb.ccms.admin.valuebeans;

import java.math.BigDecimal;
import java.util.Date;


public class OutageDetailsValueBean {
	    private String outgId;
	    private String cirId;
	    private String cirName;
	    private String secName;
	    private String ssName;
	    private String fdrName;
	    private String dtName;
	    private Date frprd;
	    private Date toprd;
	    
		public String getCirId() {
			return cirId;
		}
		public void setCirId(String cirId) {
			this.cirId = cirId;
		}
		public String getOutgId() {
			return outgId;
		}
		public void setOutgId(String outgId) {
			this.outgId = outgId;
		}
		public String getCirName() {
			return cirName;
		}
		public void setCirName(String cirName) {
			this.cirName = cirName;
		}
		public String getSecName() {
			return secName;
		}
		public void setSecName(String secName) {
			this.secName = secName;
		}
		public String getSsName() {
			return ssName;
		}
		public void setSsName(String ssName) {
			this.ssName = ssName;
		}
		public String getFdrName() {
			return fdrName;
		}
		public void setFdrName(String fdrName) {
			this.fdrName = fdrName;
		}
		public String getDtName() {
			return dtName;
		}
		public void setDtName(String dtName) {
			this.dtName = dtName;
		}
		public Date getFrprd() {
			return frprd;
		}
		public void setFrprd(Date frprd) {
			this.frprd = frprd;
		}
		public Date getToprd() {
			return toprd;
		}
		public void setToprd(Date toprd) {
			this.toprd = toprd;
		}
	    
}
