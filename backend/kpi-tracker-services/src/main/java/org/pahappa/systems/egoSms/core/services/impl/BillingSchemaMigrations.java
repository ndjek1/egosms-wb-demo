package org.pahappa.systems.egoSms.core.services.impl;

import org.sers.webutils.model.migrations.Migration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
@Transactional
public class BillingSchemaMigrations {
    @Autowired private DataSource dataSource;

    @Migration(orderNumber = 2)
    public void upgradeMonthlySubscriptionSchema() throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            if (!tableExists(connection, "egosms_service_fee_charges")) return;
            makeNullable(connection, "egosms_service_fee_charges", "message_id");
            addMonthlyAccountConstraint(connection);
        } finally {
            connection.close();
        }
    }

    private void makeNullable(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet columns = metadata.getColumns(connection.getCatalog(), null, table, column);
        try {
            if (!columns.next() || columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable) return;
            String type = sqlType(columns);
            Statement statement = connection.createStatement();
            try { statement.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN " + column + " " + type + " NULL"); }
            finally { statement.close(); }
        } finally { columns.close(); }
    }

    private void addMonthlyAccountConstraint(Connection connection) throws SQLException {
        String expected = "uk_egosms_account_billing_period";
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, "egosms_service_fee_charges", true, false);
        try {
            while (indexes.next()) if (expected.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) return;
        } finally { indexes.close(); }
        Statement statement = connection.createStatement();
        try { statement.executeUpdate("CREATE UNIQUE INDEX " + expected + " ON egosms_service_fee_charges (connection_id, billing_period, charge_type)"); }
        finally { statement.close(); }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, table, new String[]{"TABLE"});
        try { return tables.next(); }
        finally { tables.close(); }
    }

    private String sqlType(ResultSet column) throws SQLException {
        int jdbcType = column.getInt("DATA_TYPE");
        String typeName = column.getString("TYPE_NAME");
        int size = column.getInt("COLUMN_SIZE");
        int scale = column.getInt("DECIMAL_DIGITS");
        if (jdbcType == Types.CHAR || jdbcType == Types.VARCHAR || jdbcType == Types.NCHAR || jdbcType == Types.NVARCHAR)
            return typeName + "(" + size + ")";
        if (jdbcType == Types.DECIMAL || jdbcType == Types.NUMERIC)
            return typeName + "(" + size + "," + scale + ")";
        return typeName;
    }
}
