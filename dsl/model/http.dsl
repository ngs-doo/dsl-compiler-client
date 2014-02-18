module http
{
  enum Method { GET; PUT; POST; }

  value Header {
    String key;
    String value;
  }

  value Request {
    URL           url;
    Method        method;
    List<Header>  headers;
    Binary?       body;
  }

  value Response {
    Int           code;
    String        status;
    List<Header>  headers;
    Binary        body;
  }
}
