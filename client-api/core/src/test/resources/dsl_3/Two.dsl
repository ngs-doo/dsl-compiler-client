module simple
{
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

  report R {
    int i;
    float? f;
    SimpleRoot[] roots 'it => true';
    SimpleSnow snowflake 'it => true';
  }

  root SimpleRoot
  {
    History;

    Int i { default scala '5'; }
    Float f;
    String s;

    specification odd 'it => it.i % 2 == 0';
    specification oddWithS 'it => it.i % 2 == 0 && it.s == s' {
      String s;
    }

    specification withS 'it => it.s == s' {
      String s;
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
    isOdd;

    calculated even from 'it => !it.isOdd';

    specification oddWithSInSnow 'it => it.i % 2 == 0 && it.s == s' {
      String s;
    }

    specification withSInSnow 'it => it.s == s' {
      String s;
    }
  }

  cube SimpleCube from SimpleRoot {
    dimension s;

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
  }

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