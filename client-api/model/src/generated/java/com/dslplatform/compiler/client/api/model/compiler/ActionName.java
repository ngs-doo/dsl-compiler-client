package com.dslplatform.compiler.client.api.model.compiler;

public enum ActionName {
    CleanProject,
    CloneProject,
    CreateProject,
    DeleteProject,
    InspectDatabaseChanges,
    DownloadServerBinaries,
    DownloadServerGeneratedModel,
    GetLastDSL,
    GetConfig,
    DiffWithLastDSL,
    ParseDSL,
    UpdateProject,
    GenerateMigrationSQL,
    GenerateSources,
    TemplateGet,
    TemplateCreate,
    TemplateListAll,
    TemplateDelete
}
