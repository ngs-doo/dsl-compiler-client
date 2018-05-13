/*MIGRATION_DESCRIPTION
MIGRATION_DESCRIPTION*/

/
/

INSERT INTO "-DSL-".Database_Migration (Ordinal, Dsls, Version) VALUES("-DSL-".DM_SEQ.nextval, '"test/alias.dsl"=>"type ExternalID = String(64);
", "test/quotes.dsl"=>"module spec
{
  aggregate Fact(date, account) {
    Date  date;
    Long  account { index; }

    specification ByAccounts ''it => ids.Contains(it.accountID)'' {
      Long[]  ids;
    }
    persistence { specification delete enabled; }
  }
", "test/escapes.dsl"=>"module escapes
{
  aggregate Cheque(number, bank) {
    String  number;
    String  bank { default ''it => "Test me"''; }
    Bool    cancelled; // Only an "Test me" can be used
  }
}
"', '2.1.0.14620')
/
COMMIT

