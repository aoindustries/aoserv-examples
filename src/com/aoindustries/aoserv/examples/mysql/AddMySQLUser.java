/*
 * Copyright 2001-2013, 2017, 2018 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.examples.mysql;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.SimpleAOClient;
import com.aoindustries.aoserv.client.account.Username;
import com.aoindustries.aoserv.client.billing.Package;
import com.aoindustries.aoserv.client.mysql.Database;
import com.aoindustries.aoserv.client.mysql.Server;
import com.aoindustries.aoserv.client.mysql.User;
import com.aoindustries.aoserv.client.mysql.UserServer;
import com.aoindustries.aoserv.client.validator.AccountingCode;
import com.aoindustries.aoserv.client.validator.MySQLDatabaseName;
import com.aoindustries.aoserv.client.validator.MySQLServerName;
import com.aoindustries.aoserv.client.validator.MySQLUserId;
import com.aoindustries.net.DomainName;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Adds a <code>User</code> to the system.
 *
 * @author  AO Industries, Inc.
 */
final public class AddMySQLUser {

	/**
	 * Adds a <code>User</code> to the system.
	 *
	 * @param  aoClient     the <code>SimpleAOClient</code> to use
	 * @param  packageName  the name of the <code>Package</code>
	 * @param  username     the new username to allocate
	 * @param  mysqlServer  the name of the MySQL instance
	 * @param  server       the hostname of the server to add the account to
	 * @param  database     the new user will be granted access to this database
	 * @param  password     the password for the new account
	 */
	public static void addMySQLUser(
		SimpleAOClient aoClient,
		AccountingCode packageName,
		MySQLUserId username,
		MySQLServerName mysqlServer,
		String server,
		MySQLDatabaseName database,
		String password
	) throws IOException, SQLException {
		// Reserve the username
		aoClient.addUsername(packageName, username);

		// Indicate the username will be used for MySQL accounts
		aoClient.addMySQLUser(username);

		// Grant access to the server
		aoClient.addMySQLServerUser(username, mysqlServer, server, UserServer.ANY_LOCAL_HOST);

		// Grant access to the database
		aoClient.addMySQLDBUser(database, mysqlServer, server, username, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);

		// Commit the changes before setting the password
		aoClient.waitForMySQLUserRebuild(server);

		// Set the password
		aoClient.setMySQLServerUserPassword(username, mysqlServer, server, password);
	}

	/**
	 * Adds a <code>User</code> to the system.
	 *
	 * @param  conn         the <code>AOServConnector</code> to use
	 * @param  packageName  the name of the <code>Package</code>
	 * @param  username     the new username to allocate
	 * @param  mysqlServer  the name of the MySQL instance
	 * @param  server       the hostname of the server to add the account to
	 * @param  database     the new user will be granted access to this database
	 * @param  password     the password for the new account
	 *
	 * @return  the new <code>UserServer</code>
	 */
	public static UserServer addMySQLUser(
		AOServConnector conn,
		AccountingCode packageName,
		MySQLUserId username,
		MySQLServerName mysqlServer,
		DomainName server,
		MySQLDatabaseName database,
		String password
	) throws IOException, SQLException {
		// Find the Package
		Package pk=conn.getBilling().getPackages().get(packageName);

		// Resolve the Host
		com.aoindustries.aoserv.client.linux.Server ao=conn.getLinux().getAoServers().get(server);

		// Resolve the Server
		Server ms=ao.getMySQLServer(mysqlServer);

		// Reserve the username
		pk.addUsername(username);
		Username un=conn.getAccount().getUsernames().get(username);

		// Indicate the username will be used for MySQL accounts
		un.addMySQLUser();
		User mu=un.getMySQLUser();

		// Grant access to the server
		int msuPKey=mu.addMySQLServerUser(ms, UserServer.ANY_LOCAL_HOST);
		UserServer msu=conn.getMysql().getMysqlServerUsers().get(msuPKey);

		// Find the Database
		Database md=ms.getMySQLDatabase(database);

		// Grant access to the database
		md.addMySQLServerUser(msu, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);

		// Commit the changes before setting the password
		ao.waitForMySQLUserRebuild();

		// Set the password
		msu.setPassword(password);

		// Return the object
		return msu;
	}

	private AddMySQLUser() {}
}
