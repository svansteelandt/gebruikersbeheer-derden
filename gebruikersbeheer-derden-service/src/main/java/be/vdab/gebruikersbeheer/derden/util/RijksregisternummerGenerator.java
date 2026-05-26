package be.vdab.gebruikersbeheer.derden.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@DependsOnDatabaseInitialization
@RequiredArgsConstructor
@Slf4j
public class RijksregisternummerGenerator {

	private final DataSource isimDataSource;

	public long getNextRijksregisterNummer() {
		if (log.isDebugEnabled()){
			log.debug("getNextRijksregisterNummer");
		}

		Connection con = null;
		PreparedStatement prep = null;
		ResultSet rs = null;

		try {
			con = this.isimDataSource.getConnection();
			prep = con.prepareStatement("SELECT sch_algemeen.seq_dummy_rrnr.nextval FROM dual");
			rs = prep.executeQuery();

			if (rs.next()) {
				return rs.getLong(1);
			}
		} catch (SQLException s) {
			throw new RuntimeException(s);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
			}
			if (prep != null) {
				try {
					prep.close();
				} catch (Exception e) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
				}
			}
		}

		return -1;
	}
}
