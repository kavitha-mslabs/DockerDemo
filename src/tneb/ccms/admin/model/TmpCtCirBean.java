package tneb.ccms.admin.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

public class TmpCtCirBean {
	  String regcode;

	    String regname;

	    String circode;

	    String cirname;

	    private BigDecimal blTot;
	    private BigDecimal blCom;
	    private BigDecimal blPen;
	    private BigDecimal meTot;
	    private BigDecimal meCom;
	    private BigDecimal mePen;
	    private BigDecimal pfTot;
	    private BigDecimal pfCom;
	    private BigDecimal pfPen;
	    private BigDecimal vfTot;
	    private BigDecimal vfCom;
	    private BigDecimal vfPen;
	    private BigDecimal fiTot;
	    private BigDecimal fiCom;
	    private BigDecimal fiPen;
	    private BigDecimal thTot;
	    private BigDecimal thCom;
	    private BigDecimal thPen;
	    private BigDecimal teTot;
	    private BigDecimal teCom;
	    private BigDecimal tePen;
	    private BigDecimal csTot;
	    private BigDecimal csCom;
	    private BigDecimal csPen;
	    private BigDecimal otTot;
	    private BigDecimal otCom;
	    private BigDecimal otPen;
		public String getRegcode() {
			return regcode;
		}
		public void setRegcode(String regcode) {
			this.regcode = regcode;
		}
		public String getRegname() {
			return regname;
		}
		public void setRegname(String regname) {
			this.regname = regname;
		}
		public String getCircode() {
			return circode;
		}
		public void setCircode(String circode) {
			this.circode = circode;
		}
		public String getCirname() {
			return cirname;
		}
		public void setCirname(String cirname) {
			this.cirname = cirname;
		}
		public BigDecimal getBlTot() {
			return blTot;
		}
		public void setBlTot(BigDecimal blTot) {
			this.blTot = blTot;
		}
		public BigDecimal getBlCom() {
			return blCom;
		}
		public void setBlCom(BigDecimal blCom) {
			this.blCom = blCom;
		}
		public BigDecimal getBlPen() {
			return blPen;
		}
		public void setBlPen(BigDecimal blPen) {
			this.blPen = blPen;
		}
		public BigDecimal getMeTot() {
			return meTot;
		}
		public void setMeTot(BigDecimal meTot) {
			this.meTot = meTot;
		}
		public BigDecimal getMeCom() {
			return meCom;
		}
		public void setMeCom(BigDecimal meCom) {
			this.meCom = meCom;
		}
		public BigDecimal getMePen() {
			return mePen;
		}
		public void setMePen(BigDecimal mePen) {
			this.mePen = mePen;
		}
		public BigDecimal getPfTot() {
			return pfTot;
		}
		public void setPfTot(BigDecimal pfTot) {
			this.pfTot = pfTot;
		}
		public BigDecimal getPfCom() {
			return pfCom;
		}
		public void setPfCom(BigDecimal pfCom) {
			this.pfCom = pfCom;
		}
		public BigDecimal getPfPen() {
			return pfPen;
		}
		public void setPfPen(BigDecimal pfPen) {
			this.pfPen = pfPen;
		}
		public BigDecimal getVfTot() {
			return vfTot;
		}
		public void setVfTot(BigDecimal vfTot) {
			this.vfTot = vfTot;
		}
		public BigDecimal getVfCom() {
			return vfCom;
		}
		public void setVfCom(BigDecimal vfCom) {
			this.vfCom = vfCom;
		}
		public BigDecimal getVfPen() {
			return vfPen;
		}
		public void setVfPen(BigDecimal vfPen) {
			this.vfPen = vfPen;
		}
		public BigDecimal getFiTot() {
			return fiTot;
		}
		public void setFiTot(BigDecimal fiTot) {
			this.fiTot = fiTot;
		}
		public BigDecimal getFiCom() {
			return fiCom;
		}
		public void setFiCom(BigDecimal fiCom) {
			this.fiCom = fiCom;
		}
		public BigDecimal getFiPen() {
			return fiPen;
		}
		public void setFiPen(BigDecimal fiPen) {
			this.fiPen = fiPen;
		}
		public BigDecimal getThTot() {
			return thTot;
		}
		public void setThTot(BigDecimal thTot) {
			this.thTot = thTot;
		}
		public BigDecimal getThCom() {
			return thCom;
		}
		public void setThCom(BigDecimal thCom) {
			this.thCom = thCom;
		}
		public BigDecimal getThPen() {
			return thPen;
		}
		public void setThPen(BigDecimal thPen) {
			this.thPen = thPen;
		}
		public BigDecimal getTeTot() {
			return teTot;
		}
		public void setTeTot(BigDecimal teTot) {
			this.teTot = teTot;
		}
		public BigDecimal getTeCom() {
			return teCom;
		}
		public void setTeCom(BigDecimal teCom) {
			this.teCom = teCom;
		}
		public BigDecimal getTePen() {
			return tePen;
		}
		public void setTePen(BigDecimal tePen) {
			this.tePen = tePen;
		}
		public BigDecimal getCsTot() {
			return csTot;
		}
		public void setCsTot(BigDecimal csTot) {
			this.csTot = csTot;
		}
		public BigDecimal getCsCom() {
			return csCom;
		}
		public void setCsCom(BigDecimal csCom) {
			this.csCom = csCom;
		}
		public BigDecimal getCsPen() {
			return csPen;
		}
		public void setCsPen(BigDecimal csPen) {
			this.csPen = csPen;
		}
		public BigDecimal getOtTot() {
			return otTot;
		}
		public void setOtTot(BigDecimal otTot) {
			this.otTot = otTot;
		}
		public BigDecimal getOtCom() {
			return otCom;
		}
		public void setOtCom(BigDecimal otCom) {
			this.otCom = otCom;
		}
		public BigDecimal getOtPen() {
			return otPen;
		}
		public void setOtPen(BigDecimal otPen) {
			this.otPen = otPen;
		}
		public TmpCtCirBean(String regcode, String regname, String circode, String cirname, BigDecimal blTot,
				BigDecimal blCom, BigDecimal blPen, BigDecimal meTot, BigDecimal meCom, BigDecimal mePen, BigDecimal pfTot,
				BigDecimal pfCom, BigDecimal pfPen, BigDecimal vfTot, BigDecimal vfCom, BigDecimal vfPen, BigDecimal fiTot,
				BigDecimal fiCom, BigDecimal fiPen, BigDecimal thTot, BigDecimal thCom, BigDecimal thPen, BigDecimal teTot,
				BigDecimal teCom, BigDecimal tePen, BigDecimal csTot, BigDecimal csCom, BigDecimal csPen, BigDecimal otTot,
				BigDecimal otCom, BigDecimal otPen) {
			super();
			this.regcode = regcode;
			this.regname = regname;
			this.circode = circode;
			this.cirname = cirname;
			this.blTot = blTot;
			this.blCom = blCom;
			this.blPen = blPen;
			this.meTot = meTot;
			this.meCom = meCom;
			this.mePen = mePen;
			this.pfTot = pfTot;
			this.pfCom = pfCom;
			this.pfPen = pfPen;
			this.vfTot = vfTot;
			this.vfCom = vfCom;
			this.vfPen = vfPen;
			this.fiTot = fiTot;
			this.fiCom = fiCom;
			this.fiPen = fiPen;
			this.thTot = thTot;
			this.thCom = thCom;
			this.thPen = thPen;
			this.teTot = teTot;
			this.teCom = teCom;
			this.tePen = tePen;
			this.csTot = csTot;
			this.csCom = csCom;
			this.csPen = csPen;
			this.otTot = otTot;
			this.otCom = otCom;
			this.otPen = otPen;
		}
		public TmpCtCirBean() {
			super();
			// TODO Auto-generated constructor stub
		} 

}
