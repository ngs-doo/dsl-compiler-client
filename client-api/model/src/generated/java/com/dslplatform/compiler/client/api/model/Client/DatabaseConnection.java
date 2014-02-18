package com.dslplatform.compiler.client.api.model.Client;

import com.fasterxml.jackson.annotation.*;

public final class DatabaseConnection implements java.io.Serializable {
    public DatabaseConnection(
            final String Server,
            final int Port,
            final String Database,
            final String Username,
            final String Password) {
        setServer(Server);
        setPort(Port);
        setDatabase(Database);
        setUsername(Username);
        setPassword(Password);
    }

    public DatabaseConnection() {
        this.Server = "";
        this.Port = 0;
        this.Database = "";
        this.Username = "";
        this.Password = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 1614492885;
        result = prime * result
                + (this.Server != null ? this.Server.hashCode() : 0);
        result = prime * result + (this.Port);
        result = prime * result
                + (this.Database != null ? this.Database.hashCode() : 0);
        result = prime * result
                + (this.Username != null ? this.Username.hashCode() : 0);
        result = prime * result
                + (this.Password != null ? this.Password.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof DatabaseConnection)) return false;
        final DatabaseConnection other = (DatabaseConnection) obj;

        if (!(this.Server.equals(other.Server))) return false;
        if (!(this.Port == other.Port)) return false;
        if (!(this.Database.equals(other.Database))) return false;
        if (!(this.Username.equals(other.Username))) return false;
        if (!(this.Password.equals(other.Password))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "DatabaseConnection(" + Server + ',' + Port + ',' + Database
                + ',' + Username + ',' + Password + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String Server;

    @JsonProperty("Server")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getServer() {
        return Server;
    }

    public DatabaseConnection setServer(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Server\" cannot be null!");
        this.Server = value;

        return this;
    }

    private int Port;

    @JsonProperty("Port")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public int getPort() {
        return Port;
    }

    public DatabaseConnection setPort(final int value) {
        this.Port = value;

        return this;
    }

    private String Database;

    @JsonProperty("Database")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getDatabase() {
        return Database;
    }

    public DatabaseConnection setDatabase(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Database\" cannot be null!");
        this.Database = value;

        return this;
    }

    private String Username;

    @JsonProperty("Username")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getUsername() {
        return Username;
    }

    public DatabaseConnection setUsername(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Username\" cannot be null!");
        this.Username = value;

        return this;
    }

    private String Password;

    @JsonProperty("Password")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getPassword() {
        return Password;
    }

    public DatabaseConnection setPassword(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Password\" cannot be null!");
        this.Password = value;

        return this;
    }
}
