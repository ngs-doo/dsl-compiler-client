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
    // CleanProject;
    // CloneProject;
    // DeleteProject;
    /*
    A - All
    T - Test
    U - Unmanaged
    E - External
    */
    RegisterUser;  //  (A) arg: Mail

    CreateTestProject; // (T)
    CreateUnmanagedProject; // (U)
    CreateExternalProject;  // (E)

    InspectDatabaseChanges;       // (A) GetChanges

    DownloadServerBinaries;       // (UE) DownloadProject
    DownloadServerGeneratedModel; // (UE) DownloadGeneratedModel

    GetLastManagedDSL;                   // (1) (TE) Managed(id) 
    GetLastUnManagedDSL;                 // (2) (U) Unmanaged(DBCon)

    GetConfig;                    // (TE)

    DiffWithLastDSL;              // (A)
    ParseDSL;                     // (A)

    UpdateManagedProject;

    UpdateUnManagedProject;

    GenerateMigrationSQL; // (U)
    GenerateSources;      // (A)

    TemplateGet;     // (T)
    TemplateCreate;  // (T)
    TemplateListAll; // (T)
    TemplateDelete;  // (T)
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
