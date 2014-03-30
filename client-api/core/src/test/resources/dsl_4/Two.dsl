module simple
{
  root Self { Self? *self;  }
  snowflake<Self> Selfy { ancestors<self> fat; ancestors<selfID> slim; }
  mixin M;
  mixin X;
  value V {
    has mixin M;
    has mixin X;
    url url;
  }
  value I {
    M m;
  }
  value Z {
    has mixin M;
  }
  entity copy(ID) {
    int ID;
  }
  aggregate clone1 { M m; }
  snowflake<clone1> snow1 { m; }
  aggregate clone2(ID) { int ID; }
  report R {
    E e;
    int i;
    float? f;
    SimpleRoot[] roots 'it => true';
    SimpleSnow snowflake 'it => true';
  }

  enum E { A; B; C; }

  root SimpleRoot
  {
    History;

    Int i { default scala '5'; }
    Float f;
    String s;
    E e { default scala 'E.B'; }

    specification odd 'it => it.i % 2 == 0';
    specification oddWithS 'it => it.i % 2 == 0 && it.s == s' {
      String s;
    }

    specification withS 'it => it.s == s' {
      String s;
    }

    specification withE 'it => it.e == e' {
      E e;
    }

    calculated isOdd from odd;
  }

  report SimpleReport
  {
    SimpleRoot [] all 'sr => true';
    SimpleRoot [] odd 'sr => sr.isOdd';
  }

  snowflake<SimpleRoot> SimpleSnow {
    i;
    f;
    s;
    e;
    isOdd;

    calculated even from 'it => !it.isOdd';

    specification oddWithSInSnow 'it => it.i % 2 == 0 && it.s == s' {
      String s;
    }

    specification withSInSnow 'it => it.s == s' {
      String s;
    }

    specification withEInSnow 'it => it.e == e' {
      E e;
    }
  }

  cube SimpleCube from SimpleRoot {
    dimension s;
    dimension e;

  specification oddInCube 'it => it.i % 2 == 0';

    max i max_i;
    min i min_i;
    min f min_f;
  }

  entity SimpleEntity
  {
    Int vin;
    Float vfl;
    String vstr;
  }

  value Val
  {
    Int vin;
    Float vfl;
    String vstr;
    E e;
  }

  event TrivialEvent{}

  value ValDTD
  {
    decimal D;
    date DT;
    timestamp T;
  }

  root RootDTD
  {
    decimal D;
    date DT;
    timestamp T;
  }

  snowflake<RootDTD> snowDTD { D; DT; T; }

  event EveDTD
  {
    decimal D;
    date DT;
    timestamp T;
    SimpleSnow SS;
    SimpleSnow? SS2;
  }
}
