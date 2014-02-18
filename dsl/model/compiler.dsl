module compiler
{
  mixin Auth;

  value Credentials {
      has mixin Auth;
    String  user;
    Binary  password;
  }

  value Token {
      has mixin Auth;
    Binary  token;
  }

// --------------------------

  mixin Project;

  value ProjectID {
      has mixin Project;
    UUID  projectID;
  }

  value ProjectName {
      has mixin Project;
    String projectName;
  }

// --------------------------

  value PackageName {
    String  packageName;
  }

  value DSL {
    Map  dslFiles;
  }

// --------------------------

  enum ActionName {
    CleanProject;
    CloneProject;
    CreateProject;
    DeleteProject;

    InspectDatabaseChanges;

    DownloadServerBinaries;
    DownloadServerGeneratedModel;
    GetLastDSL;

    GetConfig;

    DiffWithLastDSL;
    ParseDSL;

    UpdateProject;
    GenerateMigrationSQL;
    GenerateSources;

    TemplateGet;
    TemplateCreate;
    TemplateListAll;
    TemplateDelete;
  }

  enum Target {
    CSharpClient;
    CSharpPortable;
    CSharpServer;
    JavaClient;
    PHPClient;
    ScalaClient;
    ScalaServer;
  }
}
