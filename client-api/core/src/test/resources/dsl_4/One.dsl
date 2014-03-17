module detail
{
  root Leaf
  {
    Int i;
    Float f;
    String s;
    simple.E e;

    Node? *node;
  }

  root Node (name)
  {
    String name;
    Node? *parent;

    detail leafs Leaf.node;
    detail others Node.parent;

    specification withName 'it => it.name == name '{
      String name;
    }
  }
}
