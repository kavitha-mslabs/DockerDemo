package tneb.ccms.admin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "COMP_CONTACT_MAP")
public class CompContactMap {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "COMP_CONTACT_MAP_ID_SEQ") 
    @Column(name = "ID", nullable = false)
    private Long id;

	@Column(name = "CONTACTNO", nullable = false, length = 15)
    private String contactNo;
	
	 @Column(name = "COMP_ID")
	    private Integer complaint;;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public Integer getComplaint() {
		return complaint;
	}

	public void setComplaint(Integer complaint) {
		this.complaint = complaint;
	}

	@Override
	public String toString() {
		return "CompContactMap [id=" + id + ", contactNo=" + contactNo + ", complaint=" + complaint + "]";
	}

	
}
