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
@Table(name = "ADMIN_USER")
public class AdminUserBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ADMIN_USER_ID_SEQ")
	@Column(name = "ID")
	Integer id;

	@Column(name = "OFFICE_NAME")
	String officeName;
	
	@Column(name = "SECTION_CODE")
	String sectionCode;

	@Column(name = "USER_NAME")
	String userName;

	@Column(name = "PASSWORD")
	String password;

	@Column(name = "MOBILE")
	String mobile;

	@Column(name = "EMAIL")
	String email;
	
	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	RoleBean roleBean;
	
	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	RegionBean regionBean;

	@ManyToOne
	@JoinColumn(name = "CIRCLE_ID")
	CircleBean circleBean;

	@ManyToOne
	@JoinColumn(name = "DIVISION_ID")
	DivisionBean divisionBean;

	@ManyToOne
	@JoinColumn(name = "SUB_DIVISION_ID")
	SubDivisionBean subDivisionBean;

	@ManyToOne
	@JoinColumn(name = "SECTION_ID")
	SectionBean sectionBean;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RoleBean getRoleBean() {
		return roleBean;
	}

	public void setRoleBean(RoleBean roleBean) {
		this.roleBean = roleBean;
	}

	public RegionBean getRegionBean() {
		return regionBean;
	}

	public void setRegionBean(RegionBean regionBean) {
		this.regionBean = regionBean;
	}

	public CircleBean getCircleBean() {
		return circleBean;
	}

	public void setCircleBean(CircleBean circleBean) {
		this.circleBean = circleBean;
	}

	public DivisionBean getDivisionBean() {
		return divisionBean;
	}

	public void setDivisionBean(DivisionBean divisionBean) {
		this.divisionBean = divisionBean;
	}

	public SubDivisionBean getSubDivisionBean() {
		return subDivisionBean;
	}

	public void setSubDivisionBean(SubDivisionBean subDivisionBean) {
		this.subDivisionBean = subDivisionBean;
	}

	public SectionBean getSectionBean() {
		return sectionBean;
	}

	public void setSectionBean(SectionBean sectionBean) {
		this.sectionBean = sectionBean;
	}
	
}
