module myModule {
    root A {
        int i;
        string s;
        float f;
    }

    root B {
        A *a;
        int i;
        string s;
        float f;
    }

    snowflake snowBA B {
        a.i  ai;
        a.s  as;
        a.f  af;
        i;
        s;
        f;
    }
}
