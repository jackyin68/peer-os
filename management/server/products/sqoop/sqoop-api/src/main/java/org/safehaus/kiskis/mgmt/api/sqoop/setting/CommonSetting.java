package org.safehaus.kiskis.mgmt.api.sqoop.setting;

public class CommonSetting extends BasicSetting {

    String connectionString;
    String tableName;
    String username;
    String password;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return super.toString() + ", connectionString=" + connectionString
                + ", tableName=" + tableName + ", username=" + username
                + ", password=" + password;
    }

}
