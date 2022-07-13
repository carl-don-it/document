1. hashtable 的key需要是定值，三个方法不要变化。

> Atomic classes are not general purpose replacements for `java.lang.Integer` and related classes. They do *not* define methods such as `equals`, `hashCode` and `compareTo`. (Because atomic variables are expected to be mutated, they are poor choices for hash table keys.)

