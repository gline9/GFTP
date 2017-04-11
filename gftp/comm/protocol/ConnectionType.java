package gftp.comm.protocol;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * connection type handles the different types of connections a client can have
 * with a server.
 * 
 * @author Gavin
 *
 */
public enum ConnectionType {
	Backup {
		@Override
		public String getSubDirectory() {
			// get the current time from the calendar class
			Calendar calendar = new GregorianCalendar();
			
			//get the date in the specified format
			String date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
					+ calendar.get(Calendar.DAY_OF_MONTH);
			
			//get the time in the specified format
			String time = calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE) + "-"
					+ calendar.get(Calendar.SECOND);
			
			//combine them so the date is separate from the backup
			return date + "/" + "Backup-" + time + "/";
		}
	};

	/**
	 * generates a sub directory of the current working directory for future
	 * actions to take place.
	 * 
	 * @return
	 */
	public abstract String getSubDirectory();
}
