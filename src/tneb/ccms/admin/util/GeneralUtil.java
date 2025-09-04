package tneb.ccms.admin.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralUtil {

	private static Logger logger = LoggerFactory.getLogger(GeneralUtil.class.getName());
	
	public static boolean validateMobileNumber(String numberString) {
		boolean validMobileNumber = false;
		try {
			Long.parseLong(numberString);
			if(numberString.trim().length() == 10) {
				validMobileNumber = true;
			}
		} catch (NumberFormatException e) {
			validMobileNumber = false;
		}
		return validMobileNumber;
	}
	
	
	public static boolean validateNumber(String numberString) {
		boolean validNumber = false;
		try {
			Long.parseLong(numberString);
			validNumber = true;
		} catch (NumberFormatException e) {
			validNumber = false;
		}
		return validNumber;
	}
	
	public static Date parseDateWithTime(String dateString) {
		if(StringUtils.isNotBlank(dateString)) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				return format.parse(dateString);
			} catch (ParseException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String formatDateWithTime(Date date) {
		if(date != null) {
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			return format.format(date);
		} else {
			return "";
		}
	}
	
	public static String formatDateForHql(Date date) {
		if(date != null) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			return format.format(date);
		} else {
			return "";
		}
	}
	
	public static String formatDate(Date date) {
		if(date != null) {
			DateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			return format.format(date);
		} else {
			return "";
		}
	}
	
	public static long findDifference(Date startDate, Date endDate, String duration) {

		long opt = 0;

		long diff = startDate.getTime() - endDate.getTime();
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000);
		long diffInDays = (int) (diff / (1000 * 60 * 60 * 24));

		if (duration.equals("sec")) {
			opt = diffSeconds;
		} else if (duration.equals("min")) {
			opt = diffMinutes;
		} else if (duration.equals("hour")) {
			opt = diffHours;
		} else if (duration.equals("day")) {
			opt = diffInDays;
		}

		return opt;

	}

	public static String findInterval(String intDate) {
		String opt = "";
		Date tdate = new Date();
		try {
			Date date_prev = new SimpleDateFormat("yyyy-MMM-dd hh:mm").parse(intDate);

			long hr = findDifference(tdate, date_prev, "hour");
			if (hr > 48) {
				opt = findDifference(tdate, date_prev, "day") + " days ago";
			}
			if (hr < 48 && hr > 24) {
				opt = "yesterday";
			} else if (hr < 1) {
				long min = findDifference(tdate, date_prev, "min");
				if (min < 5) {
					opt = "just now";
				} else if (min > 5) {
					opt = min + " minutes ago (" + intDate + ")";
				}
			} else if (hr > 1 && hr <= 24) {
				opt = hr + " hours ago";
			} else if (hr == 1) {
				opt = hr + " hour ago";
			}

		} catch (ParseException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		return opt;
	}
	
	public String generateOtp() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		System.out.println("Generated OTP Is:::: " + otp);
		return String.valueOf(otp);
	}
}
