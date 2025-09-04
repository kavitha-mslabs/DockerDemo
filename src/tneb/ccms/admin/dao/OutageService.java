package tneb.ccms.admin.dao;

import java.sql.Timestamp;
import java.util.List;

import tneb.ccms.admin.model.OutagesBean;

public class OutageService {
	
	GeneralDao generalDao = new GeneralDao();


	    public void updateExpiredOutages() {
	        
	        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	        
	        List<OutagesBean> expiredOutages = generalDao.findExpiredOutages(currentTimestamp);
	        
	        for (OutagesBean outage : expiredOutages) {
	            outage.setStatus("C");
	            generalDao.updateOutageStatus(outage);
	        }
	    }
}
