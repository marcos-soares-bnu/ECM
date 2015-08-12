package change;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import util.Constantes;
import util.DateUtil;
import database.DBUtil;

public class ChangeCheck {

	private Date dtNow = null;
	private DBUtil dbUtil = new DBUtil();
	private DateUtil dtUtil = new DateUtil();

	public ChangeCheck(Date dtNow) {
		this.dtNow = dtNow;
	}

	public void checkChanges() {
		this.removeAllChanges();
		this.checkOLDChanges();
		this.setActualChanges();
	}

	private void removeAllChanges() {
		String sql = "";
		sql = "UPDATE linde_check_itens SET is_onchange = 0";
		dbUtil.execSQL(sql);
		sql = "UPDATE check_scripts_itens SET on_change = 0";
		dbUtil.execSQL(sql);
	}

	private void setActualChanges() {
		String table = "linde_changes";
		String fields = "date_begin, date_end, change_checks";
		String condition = "old_change = 0";
		ResultSet rs = this.dbUtil.doSelect(fields, table, condition);

		try {
			while (rs.next()) {
				String strDateBegin = "";
				Timestamp timstBegin = rs.getTimestamp("date_begin");
				if (timstBegin != null) {
					strDateBegin = dtUtil
							.getStrDateFromDB(timstBegin.getTime());
				}
				String strDateEnd = "";
				Timestamp timstEnd = rs.getTimestamp("date_end");
				if (timstEnd != null) {
					strDateEnd = dtUtil.getStrDateFromDB(timstEnd.getTime());
				}
				String changeChecks = rs.getString("change_checks");

				Date dtBegin = dtUtil.getDateFromDBString(strDateBegin);
				Date dtEnd = dtUtil.getDateFromDBString(strDateEnd);

				if (dtNow.after(dtBegin) && dtNow.before(dtEnd)) {
					String[] checks = changeChecks.split(";");
					// poem os checks do array em status 'change'
					String sql = "";
					for (int i = 0; i < checks.length; i++) {
						sql = "UPDATE linde_check_itens SET is_onchange=1 WHERE code LIKE '"
								+ checks[i] + "'";
						dbUtil.execSQL(sql);
						sql = "UPDATE check_scripts_itens SET on_change=1 WHERE item_name LIKE '"
								+ checks[i] + "'";
						dbUtil.execSQL(sql);

						this.putChangeStatus(checks[i]);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void checkOLDChanges() {
		String table = "linde_changes";
		String fields = "id, date_end";
		String condition = "old_change = 0";
		ResultSet rs = this.dbUtil.doSelect(fields, table, condition);

		try {
			while (rs.next()) {
				int id = rs.getInt("id");
				String strDateEnd = "";
				Timestamp timstEnd = rs.getTimestamp("date_end");
				if (timstEnd != null) {
					strDateEnd = dtUtil.getStrDateFromDB(timstEnd.getTime());
				}
				Date dtEnd = dtUtil.getDateFromDBString(strDateEnd);

				// Data de agora, é a data após o termino da change
				if (dtNow.after(dtEnd)) {
					String sql = "UPDATE linde_changes SET old_change=1 WHERE id='"
							+ id + "'";
					dbUtil.execSQL(sql);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void putChangeStatus(String check) {
		String fields = "id";
		String condition = "item_name LIKE '" + check + "'";

		ResultSet rs = dbUtil.doSelect(fields, Constantes.DB_CheckItens_Table,
				condition);
		int itemID = 0;

		try {
			if (rs.next()){
				itemID = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (itemID != 0) {
			fields = "check_id";
			condition = "id=" + itemID;

			rs = dbUtil.doSelect(fields, Constantes.DB_CheckItens_Table,
					condition);
		}
		int checkID = 0;
		try {
			if (rs.next()) {
				checkID = rs.getInt("check_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Caso forem 0, deve ser um check manual
		if (checkID != 0 && itemID != 0) {
			fields = "check_id, check_item_id, status, output_error, exec_time, is_new";
			String values = "'" + checkID + "'," + "'" + itemID + "'," + "'"
					+ "CHANGE" + "'," + "'" + "" + "'," + "'"
					+ dtUtil.getDBFormat(dtNow) + "'," + 0;

			dbUtil.doINSERT(Constantes.DB_ChecksOutput_Table, fields, values);
		}
	}
}
