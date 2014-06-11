package com.dslplatform.compiler.client.params;

/**
 *  A joint parameter object for authenticating against a database.
 *
 *  Contains either all the credentials and connection parameters, or just the connection string
 */
public class DBAuth implements Param {

    public final DBUsername dbUsername;
    public final DBPassword dbPassword;
    public final DBHost dbHost;
    public final DBPort dbPort;
    public final DBDatabaseName dbDatabaseName;
    public final DBConnectionString dbConnectionString;

    public DBAuth(
            final DBUsername dbUsername
            , final DBPassword dbPassword
            , final DBHost dbHost
            , final DBPort dbPort
            , final DBDatabaseName dbDatabaseName
            , final DBConnectionString dbConnectionString
            ) {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbDatabaseName = dbDatabaseName;
        this.dbConnectionString = dbConnectionString;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBAuth) || that == null) return false;

        final DBAuth thatDBAuth = (DBAuth) that;
        return dbUsername.equals(thatDBAuth.dbUsername)
                && dbPassword.equals(thatDBAuth.dbPassword)
                && dbHost.equals(thatDBAuth.dbHost)
                && dbPort.equals(thatDBAuth.dbPort)
                && dbDatabaseName.equals(thatDBAuth.dbDatabaseName)
                && dbConnectionString.equals(thatDBAuth.dbConnectionString);
    }

    @Override
    public int hashCode() {
        return
                dbUsername.hashCode()
                +dbPassword.hashCode()
                +dbHost.hashCode()
                +dbPort.hashCode()
                +dbDatabaseName.hashCode()
                +dbConnectionString.hashCode();
    }

    @Override
    public String toString() {
        return "DBUsername(" + dbUsername + ")";
    }

    public DBUsername getDbUsername() {
        return dbUsername;
    }

    public DBPassword getDbPassword() {
        return dbPassword;
    }

    public DBHost getDbHost() {
        return dbHost;
    }

    public DBPort getDbPort() {
        return dbPort;
    }

    public DBDatabaseName getDbDatabaseName() {
        return dbDatabaseName;
    }

    public DBConnectionString getDbConnectionString() {
        return dbConnectionString;
    }


}
