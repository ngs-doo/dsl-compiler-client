/*MIGRATION_DESCRIPTION
MIGRATION_DESCRIPTION*/

DO $$ BEGIN
	IF EXISTS(SELECT * FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = '-DSL-' AND c.relname = 'database_setting') THEN
		IF EXISTS(SELECT * FROM "-DSL-".Database_Setting WHERE Key ILIKE 'mode' AND NOT Value ILIKE 'unsafe') THEN
			RAISE EXCEPTION 'Database upgrade is forbidden. Change database mode to allow upgrade';
		END IF;
	END IF;
END $$ LANGUAGE plpgsql;

DO $$ BEGIN
END $$ LANGUAGE plpgsql;

SELECT "-DSL-".Persist_Concepts('"test/alias.dsl"=>"type ExternalID = String(64);
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
    String  bank { default ''it => \"Test me\"''; }
    Bool    cancelled; // Only an \"Test me\" can be used
  }
}
"', '\x','2.1.0.14620');
SELECT pg_notify('migration', 'new');

