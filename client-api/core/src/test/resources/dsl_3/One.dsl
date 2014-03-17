module detail
{
  root Node (name)
  {
    String name;
    Node? *parent;

    specification withName 'it => it.name == name '{
      String name;
    }
  }
}
