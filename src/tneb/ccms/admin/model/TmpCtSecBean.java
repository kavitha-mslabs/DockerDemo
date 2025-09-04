package tneb.ccms.admin.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "TMP_CT_SEC")
public class TmpCtSecBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
	@SequenceGenerator(name = "id_seq", sequenceName = "TMP_CT_SEC_SEQ_NEW", allocationSize = 1)
	@Column(name = "ID")
    Integer id;

    @Column(name = "CIRCODE", length = 10)
    private String cirCode;

    @Column(name = "SECCODE", length = 10)
    private String secCode;

    @Column(name = "SECNAME", length = 50)
    private String secName;

    @Column(name = "BLTOT")
    private Integer blTot;

    @Column(name = "BLCOM")
    private Integer blCom;

    @Column(name = "BLPEN")
    private Integer blPen;

    @Column(name = "METOT")
    private Integer meTot;

    @Column(name = "MECOM")
    private Integer meCom;

    @Column(name = "MEPEN")
    private Integer mePen;

    @Column(name = "PFTOT")
    private Integer pfTot;

    @Column(name = "PFCOM")
    private Integer pfCom;

    @Column(name = "PFPEN")
    private Integer pfPen;

    @Column(name = "VFTOT")
    private Integer vfTot;

    @Column(name = "VFCOM")
    private Integer vfCom;

    @Column(name = "VFPEN")
    private Integer vfPen;

    @Column(name = "FITOT")
    private Integer fiTot;

    @Column(name = "FICOM")
    private Integer fiCom;

    @Column(name = "FIPEN")
    private Integer fiPen;

    @Column(name = "THTOT")
    private Integer thTot;

    @Column(name = "THCOM")
    private Integer thCom;

    @Column(name = "THPEN")
    private Integer thPen;

    @Column(name = "TETOT")
    private Integer teTot;

    @Column(name = "TECOM")
    private Integer teCom;

    @Column(name = "TEPEN")
    private Integer tePen;

    @Column(name = "CSTOT")
    private Integer csTot;

    @Column(name = "CSCOM")
    private Integer csCom;

    @Column(name = "CSPEN")
    private Integer csPen;

    @Column(name = "OTTOT")
    private Integer otTot;

    @Column(name = "OTCOM")
    private Integer otCom;

    @Column(name = "OTPEN")
    private Integer otPen;

    @Column(name = "CIRNAME", length = 40)
    private String cirName;

    @Column(name = "REGCODE", length = 5)
    private String regCode;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCirCode() {
		return cirCode;
	}

	public void setCirCode(String cirCode) {
		this.cirCode = cirCode;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getSecName() {
		return secName;
	}

	public void setSecName(String secName) {
		this.secName = secName;
	}

	public Integer getBlTot() {
		return blTot;
	}

	public void setBlTot(Integer blTot) {
		this.blTot = blTot;
	}

	public Integer getBlCom() {
		return blCom;
	}

	public void setBlCom(Integer blCom) {
		this.blCom = blCom;
	}

	public Integer getBlPen() {
		return blPen;
	}

	public void setBlPen(Integer blPen) {
		this.blPen = blPen;
	}

	public Integer getMeTot() {
		return meTot;
	}

	public void setMeTot(Integer meTot) {
		this.meTot = meTot;
	}

	public Integer getMeCom() {
		return meCom;
	}

	public void setMeCom(Integer meCom) {
		this.meCom = meCom;
	}

	public Integer getMePen() {
		return mePen;
	}

	public void setMePen(Integer mePen) {
		this.mePen = mePen;
	}

	public Integer getPfTot() {
		return pfTot;
	}

	public void setPfTot(Integer pfTot) {
		this.pfTot = pfTot;
	}

	public Integer getPfCom() {
		return pfCom;
	}

	public void setPfCom(Integer pfCom) {
		this.pfCom = pfCom;
	}

	public Integer getPfPen() {
		return pfPen;
	}

	public void setPfPen(Integer pfPen) {
		this.pfPen = pfPen;
	}

	public Integer getVfTot() {
		return vfTot;
	}

	public void setVfTot(Integer vfTot) {
		this.vfTot = vfTot;
	}

	public Integer getVfCom() {
		return vfCom;
	}

	public void setVfCom(Integer vfCom) {
		this.vfCom = vfCom;
	}

	public Integer getVfPen() {
		return vfPen;
	}

	public void setVfPen(Integer vfPen) {
		this.vfPen = vfPen;
	}

	public Integer getFiTot() {
		return fiTot;
	}

	public void setFiTot(Integer fiTot) {
		this.fiTot = fiTot;
	}

	public Integer getFiCom() {
		return fiCom;
	}

	public void setFiCom(Integer fiCom) {
		this.fiCom = fiCom;
	}

	public Integer getFiPen() {
		return fiPen;
	}

	public void setFiPen(Integer fiPen) {
		this.fiPen = fiPen;
	}

	public Integer getThTot() {
		return thTot;
	}

	public void setThTot(Integer thTot) {
		this.thTot = thTot;
	}

	public Integer getThCom() {
		return thCom;
	}

	public void setThCom(Integer thCom) {
		this.thCom = thCom;
	}

	public Integer getThPen() {
		return thPen;
	}

	public void setThPen(Integer thPen) {
		this.thPen = thPen;
	}

	public Integer getTeTot() {
		return teTot;
	}

	public void setTeTot(Integer teTot) {
		this.teTot = teTot;
	}

	public Integer getTeCom() {
		return teCom;
	}

	public void setTeCom(Integer teCom) {
		this.teCom = teCom;
	}

	public Integer getTePen() {
		return tePen;
	}

	public void setTePen(Integer tePen) {
		this.tePen = tePen;
	}

	public Integer getCsTot() {
		return csTot;
	}

	public void setCsTot(Integer csTot) {
		this.csTot = csTot;
	}

	public Integer getCsCom() {
		return csCom;
	}

	public void setCsCom(Integer csCom) {
		this.csCom = csCom;
	}

	public Integer getCsPen() {
		return csPen;
	}

	public void setCsPen(Integer csPen) {
		this.csPen = csPen;
	}

	public Integer getOtTot() {
		return otTot;
	}

	public void setOtTot(Integer otTot) {
		this.otTot = otTot;
	}

	public Integer getOtCom() {
		return otCom;
	}

	public void setOtCom(Integer otCom) {
		this.otCom = otCom;
	}

	public Integer getOtPen() {
		return otPen;
	}

	public void setOtPen(Integer otPen) {
		this.otPen = otPen;
	}

	public String getCirName() {
		return cirName;
	}

	public void setCirName(String cirName) {
		this.cirName = cirName;
	}

	public String getRegCode() {
		return regCode;
	}

	public void setRegCode(String regCode) {
		this.regCode = regCode;
	}


}
