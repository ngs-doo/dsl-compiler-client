package com.dslplatform.compiler.client.api.model.json;

import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.Change;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchemaChangeJsonDeserialization {
    public static List<Change> fromJsonArray(final JsonReader jsonReader) throws IOException {
        jsonReader.assertRead('[');
        boolean needComma = false;

        final List<Change> changes = new ArrayList<Change>();
        while (jsonReader.read() != ']') {
            if (needComma) jsonReader.assertLast(',');
            changes.add(fromJsonObject(jsonReader));
            needComma = true;
        }
        jsonReader.invalidate();

        return changes;
    }

    public static Change fromJsonObject(final JsonReader jsonReader) throws IOException {
        jsonReader.assertRead('{');
        boolean needComma = false;

        Change.ChangeType _type = null;
        String _definition = null;
        String _description = null;
        while (jsonReader.read() != '}') {
            if (needComma) jsonReader.assertLast(',');

            final String property = jsonReader.readString();
            jsonReader.assertNext(':');

            if (property.equals("Type")) {
                _type = Change.ChangeType.valueOf(jsonReader.readString());
            }
            else if (property.equals("Definition")) {
                _definition = jsonReader.readString();
            }
            else if (property.equals("CreatedAt")) {
                _description = jsonReader.readString();
            } else {
                jsonReader.readString();
            }

            needComma = true;
        }
        jsonReader.invalidate();

        return new Change(_type, _definition, _description);
    }
}
