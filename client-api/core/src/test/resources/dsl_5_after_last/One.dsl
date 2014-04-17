module detail
{
  root Leaf
  {
    Int i;
    Float ff;
    String str;
    simple.E e;

    Node? *node;
  }

  root Node (id)
  {
    String id;
    Node? *parent;

    detail leafs Leaf.node;
    detail others Node.parent;

    specification named 'it => it.name == name '{
      String name;
    }
  }
}
