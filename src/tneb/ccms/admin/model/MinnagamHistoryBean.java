package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MINNAGAM_HISTORY")
public class MinnagamHistoryBean {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "ID")
    private Long id;
	
	@Column(name = "UPTO")
	Timestamp upTo;
	
	@Column(name="TOTALCOMP")
	Long totalComp;

	public Timestamp getUpTo() {
		return upTo;
	}

	public void setUpTo(Timestamp upTo) {
		this.upTo = upTo;
	}

	public Long getTotalComp() {
		return totalComp;
	}

	public void setTotalComp(Long totalComp) {
		this.totalComp = totalComp;
	}

	public MinnagamHistoryBean(Timestamp upTo, Long totalComp) {
		super();
		this.upTo = upTo;
		this.totalComp = totalComp;
	}

	public MinnagamHistoryBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
