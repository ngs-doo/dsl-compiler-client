package com.dslplatform.compiler.client.api.model;

public class Change {
    public static enum ChangeType { Unknown, Remove, Create, Rename, Move, Copy};

    final ChangeType Type;
    final String Definition;
    final String Description;

    public Change(ChangeType type, String definition, String description) {
        Type = type;
        Definition = definition;
        Description = description;
    }

    public Change(String type, String definition, String description) {
        Type = ChangeType.valueOf(type);
        Definition = definition;
        Description = description;
    }
}
