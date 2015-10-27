package com.dongluhitec.card.domain.db.singlecarpark;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dongluhitec.card.domain.db.DomainObject;
import com.dongluhitec.card.domain.util.StrUtil;

@Entity
public class SingleCarparkUser extends DomainObject {
	public enum Property{
		id,plateNo,name,type,address,carparkNo,createDate,validTo,remark
	}
	
	private String name;
	@Column(unique=true)
	private String plateNo;
	private String type;
	private String address;
	private String carparkNo;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date validTo;
	private Integer remindDays;
	private Integer delayDays;
	
	private String remark;
	@ManyToOne
	private SingleCarparkCarpark carpark;
	
	public String getValitoLabel(){
		return StrUtil.formatDate(validTo, "yyyy-MM-dd HH:mm:ss");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		if (pcs != null)
			pcs.firePropertyChange("name", null, null);
	}
	public String getPlateNo() {
		return plateNo;
	}
	public void setPlateNo(String plateNo) {
		this.plateNo = plateNo;
		if (pcs != null)
			pcs.firePropertyChange("plateNo", null, null);
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
		if (pcs != null)
			pcs.firePropertyChange("type", null, null);
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
		if (pcs != null)
			pcs.firePropertyChange("address", null, null);
	}
	public String getCarparkNo() {
		return carparkNo;
	}
	public void setCarparkNo(String carparkNo) {
		this.carparkNo = carparkNo;
		if (pcs != null)
			pcs.firePropertyChange("carparkNo", null, null);
	}
	public SingleCarparkCarpark getCarpark() {
		return carpark;
	}
	public void setCarpark(SingleCarparkCarpark carpark) {
		this.carpark = carpark;
		if (pcs != null)
			pcs.firePropertyChange("carpark", null, null);
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
		if (pcs != null)
			pcs.firePropertyChange("createDate", null, null);
	}
	public Date getValidTo() {
		return validTo;
	}
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
		if (pcs != null)
			pcs.firePropertyChange("validTo", null, null);
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
		if (pcs != null)
			pcs.firePropertyChange("remark", null, null);
	}
	public Integer getRemindDays() {
		return remindDays;
	}
	public void setRemindDays(Integer remindDays) {
		this.remindDays = remindDays;
		if (pcs != null)
			pcs.firePropertyChange("remindDays", null, null);
	}
	public Integer getDelayDays() {
		return delayDays;
	}
	public void setDelayDays(Integer delayDays) {
		this.delayDays = delayDays;
		if (pcs != null)
			pcs.firePropertyChange("delayDays", null, null);
	}
}
