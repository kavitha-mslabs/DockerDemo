package tneb.ccms.admin.model;
import java.sql.Timestamp;

import javax.persistence.*;


@Entity
@Table(name = "COMP_TRANSFER")
public class CompTransferBean {

	 @Id
	 @GeneratedValue(strategy = GenerationType.AUTO, generator = "COMP_TRANSFER_ID_SEQ")
	 private Integer id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "COMP_ID", nullable = false)
	    private ComplaintBean complaint;

	    @Column(name = "TRF_ON", nullable = false)
	    private Timestamp transferOn;


	    @Column(name = "TRF_USER", nullable = false, length = 20)
	    private String transferUser;

	    @Column(name = "REMARKS", length = 100)
	    private String remarks;

	    @Column(name = "IPID", length = 25)
	    private String ipid;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public ComplaintBean getComplaint() {
			return complaint;
		}

		public void setComplaint(ComplaintBean complaint) {
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
	    
	    
	    
}
