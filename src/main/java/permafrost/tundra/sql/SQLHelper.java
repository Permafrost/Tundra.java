/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper methods for databases and SQL statements.
 */
public class SQLHelper {
    /**
     * The default query timeout used if no other timeout is set on a prepared or callable statement.
     */
    public static final int DEFAULT_QUERY_TIMEOUT_SECONDS = 60 * 10; // 10 minutes

    /**
     * Disallow instantiation of this class.
     */
    private SQLHelper() {}

    /**
     * Closes the given JDBC database result sets, ignoring any thrown SQLException exceptions.
     *
     * @param resultSets    The result sets to close.
     */
    public static void close(ResultSet ...resultSets) {
        if (resultSets != null) {
            for (ResultSet resultSet : resultSets) {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch(SQLException ex) {
                        // ignore exception
                    }
                }
            }
        }
    }

    /**
     * Closes the given JDBC database statements, ignoring any thrown SQLException exceptions.
     *
     * @param statements    The statements to close.
     */
    public static void close(Statement ...statements) {
        if (statements != null) {
            for (Statement statement : statements) {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch(SQLException ex) {
                        // ignore exception
                    }
                }
            }
        }
    }

    /**
     * Closes the given JDBC database connections, ignoring any thrown SQLException exceptions.
     *
     * @param connections    The connections to close.
     */
    public static void close(Connection ...connections) {
        if (connections != null) {
            for (Connection connection : connections) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch(SQLException ex) {
                        // ignore exception
                    }
                }
            }
        }
    }

    /**
     * Returns a PreparedStatement to execute the given SQL statement against the given JDBC database connection.
     *
     * @param connection        The database connection.
     * @param statement         The SQL statement to prepare.
     * @return                  The prepared statement.
     * @throws SQLException     If a database error occurs.
     */
    public static PreparedStatement prepareStatement(Connection connection, String statement) throws SQLException {
        if (connection == null || statement == null) return null;
        return sanitizePreparedStatement(connection.prepareStatement(statement));
    }

    /**
     * Returns a PreparedStatement to execute the given SQL statement against the given JDBC database connection.
     *
     * @param connection        The database connection.
     * @param statement         The SQL statement to prepare.
     * @return                  The prepared statement.
     * @throws SQLException     If a database error occurs.
     */
    public static CallableStatement prepareCall(Connection connection, String statement) throws SQLException {
        if (connection == null || statement == null) return null;
        return sanitizeCallableStatement(connection.prepareCall(statement));
    }

    /**
     * Sanitizes the given statement by clearing warnings, batches and parameters, and setting a default timeout.
     *
     * @param preparedStatement The statement to sanitize.
     * @return                  The sanitized statement.
     * @throws SQLException     If a database error occurs.
     */
    private static PreparedStatement sanitizePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) {
            sanitizeStatement(preparedStatement);
            preparedStatement.clearParameters();
        }
        return preparedStatement;
    }

    /**
     *  Sanitizes the given statement by clearing warnings, batches and parameters, and setting a default timeout.
     *
     * @param callableStatement The statement to sanitize.
     * @return                  The sanitized statement.
     * @throws SQLException     If a database error occurs.
     */
    private static CallableStatement sanitizeCallableStatement(CallableStatement callableStatement) throws SQLException {
        if (callableStatement != null) {
            sanitizeStatement(callableStatement);
            callableStatement.clearParameters();
        }
        return callableStatement;
    }

    /**
     * Sanitizes the given statement by clearing warnings and batches, and setting a default timeout.
     *
     * @param statement     The statement to sanitize.
     * @throws SQLException If a database error occurs.
     */
    private static void sanitizeStatement(Statement statement) throws SQLException {
        statement.setQueryTimeout(DEFAULT_QUERY_TIMEOUT_SECONDS);
        statement.clearBatch();
        statement.clearWarnings();
    }
}
