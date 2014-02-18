module Client
{
  value ProjectDetails {
    UUID       ID;
    String     projectName;
    Timestamp  createdAt;
  }

  guid root Project {
    String?    Nick;
    Timestamp  CreatedAt;

    specification FindByUserAndName 'it => true' {
      String User;
      String Name;
    }

    specification FindByUser 'it => true' {
      String User;
    }
  }

  event Register {
    String  Email;
  }

  event CreateProject {
    String  ProjectName;
  }

  value DatabaseConnection
  {
    String  Server;
    Int     Port;
    String  Database;
    String  Username;
    String  Password;
  }

  event CreateExternalProject {
    String  ProjectName;
    String  ServerName;
    String  ApplicationName;
    DatabaseConnection  Database;
  }

  event RenameProject {
    String  OldName;
    String  NewName;
  }

  event CloneProject {
    String  Project;
  }

  event WipeCleanProject {
    String  Project;
  }

  event UploadTemplate {
    String  Project;
    String  Name;
    Binary  Content;
  }

  event DeleteTemplate {
    String  Project;
    String  Name;
  }

  event DeleteProject {
    String  Project;
  }
}
