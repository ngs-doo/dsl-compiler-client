package com.dslplatform.compiler.client.api.model.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.ApplicationServer;
import com.dslplatform.compiler.client.api.model.DatabaseConnection;
import com.dslplatform.compiler.client.api.model.Project;

public class ProjectJsonDeserialization {
    public static List<Project> fromJsonArray(final JsonReader jsonReader) throws IOException {
        jsonReader.assertRead('[');
        boolean needComma = false;

        final List<Project> projects = new ArrayList<Project>();
        while (jsonReader.read() != ']') {
            if (needComma) jsonReader.assertLast(',');
            projects.add(fromJsonObject(jsonReader));
            needComma = true;
        }
        jsonReader.invalidate();

        return projects;
    }

    public static Project fromJsonObject(final JsonReader jsonReader) throws IOException {
        jsonReader.assertRead('{');
        boolean needComma = false;

        UUID _ID = null;
        String _userID = null;
        DateTime _createdAt = null;
        String _nick = null;

        String _applicationServerHost = null;
        String _applicationServerName = null;
        String _applicationServerPool = null;

        boolean _isExternal = false;
        String _databaseConnectionHost = null;
        int _databaseConnectionPort = 0;
        String _databaseConnectionName = null;
        String _databaseConnectionUser = null;
        char[] _databaseConnectionPassword = null;

        while (jsonReader.read() != '}') {
            if (needComma) jsonReader.assertLast(',');

            final String property = jsonReader.readString();
            jsonReader.assertNext(':');

            if (property.equals("ID")) {
                _ID = UUID.fromString(jsonReader.readString());
            }
            else if (property.equals("UserID")) {
                _userID = jsonReader.readString();
            }
            else if (property.equals("CreatedAt")) {
                _createdAt = new DateTime(jsonReader.readString());
            }
            else if (property.equals("Nick")) {
                _nick = jsonReader.readString();
            }
            else if (property.equals("ApplicationServer")) {
                _applicationServerHost = jsonReader.readString();
            }
            else if (property.equals("ApplicationName")) {
                _applicationServerName = jsonReader.readString();
            }
            else if (property.equals("ApplicationPoolName")) {
                _applicationServerPool = jsonReader.readString();
            }
            else if (property.equals("IsExternal")) {
                _isExternal = jsonReader.readBoolean();
            }
            else if (property.equals("DatabaseServer")) {
                _databaseConnectionHost = jsonReader.readString();
            }
            else if (property.equals("DatabasePort")) {
                _databaseConnectionPort = Integer.parseInt(jsonReader.readRawNumber());
            }
            else if (property.equals("DatabaseName")) {
                _databaseConnectionName = jsonReader.readString();
            }
            else if (property.equals("CustomDatabaseAdmin")) {
                _databaseConnectionUser = jsonReader.readString();
            }
            else if (property.equals("CustomDatabasePassword")) {
                _databaseConnectionPassword = jsonReader.readString().toCharArray();
            }
            else {
                jsonReader.readString();
            }

            needComma = true;
        }
        jsonReader.invalidate();

        return new Project(
                _ID,
                _userID,
                _createdAt,
                _nick,
                new ApplicationServer(
                        _applicationServerHost,
                        _applicationServerName,
                        _applicationServerPool),
                _isExternal
                    ? new DatabaseConnection(
                            _databaseConnectionHost,
                            _databaseConnectionPort,
                            _databaseConnectionName,
                            _databaseConnectionUser,
                            _databaseConnectionPassword)
                    : null);
    }
}
