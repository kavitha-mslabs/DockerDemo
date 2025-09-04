package tneb.ccms.admin.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "CONSUMER_SERVICE")
public class ConsumerServiceBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CONSUMER_SERVICE_ID_SEQ")
	@Column(name = "ID")
	Integer id;
	
	@Column(name = "SERVICE_NUMBER")
	String serviceNumber;
	
	@Column(name = "SERVICE_TARIFF")
	String serviceTariff;
	
	@Column(name = "SERVICE_NAME")
	String serviceName;
	
	@Column(name = "SERVICE_ADDRESS")
	String serviceAddress;
	
	@Column(name = "LANDMARK")
	String landmark;
	
	@Column(name = "MOBILE")
	String mobile;
	
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	PublicUserBean publicUserBean;
	
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
	
	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	Timestamp createdOn;

	@Column(name = "UPDATED_ON", insertable = false, updatable = true)
	Timestamp updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getServiceNumber() {
		return serviceNumber;
	}

	public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	public String getServiceTariff() {
		return serviceTariff;
	}

	public void setServiceTariff(String serviceTariff) {
		this.serviceTariff = serviceTariff;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public PublicUserBean getPublicUserBean() {
		return publicUserBean;
	}

	public void setPublicUserBean(PublicUserBean publicUserBean) {
		this.publicUserBean = publicUserBean;
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

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}
	
}
