https://blog.csdn.net/u011392897/article/details/60149314

# 零、主要改动

相对于1.6：

1. 懒初始化 lazy init，默认构造的HashMap底层数组并不会直接初始化，而是先使用空数组，等到实际要添加数据时再真正初始化。

2. 引入hashSeed，用于得到更好的hash值，以及在扩容时判断是否需要重新计算每个Entry的hash值（Entry的hash不再是final的，可以变了）。

3. 修复1.6的一些小问题，加上其他的一些小改动。

   大的方面没什么改动。

# 一、基本性质

和1.6的一样。
整体结构和1.6的一致。

# 二、常量和变量

## 1、常量

变化比较少，多的两个常量对HashMap的基本实现基本没什么影响。

```java
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
static final int MAXIMUM_CAPACITY = 1 << 30;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
 
/** An empty table instance to share when the table is not inflated. */
static final Entry<?,?>[] EMPTY_TABLE = {}; // 懒初始化使用的空数组，
 
// 一个默认阈值。高于该值，且以String作为Key时，使用备用哈希hash函数（sun.misc.Hashing.stringHash32），这个备用hash函数能减少String的hash冲突。
// 此值是最大int型，表明hash表再也不能扩容了，继续put下去，hash冲突会越来越多。
// 至于为什么是String型才有这种特殊待遇，因为String是最常见的Key的类型。Integer虽然也很常见，但是Integer有范围限制，并且它的hashCode设计得就非常好（就是自身的数值）。
// 可以通过定义系统属性jdk.map.althashing.threshold来覆盖这个值。
// 值为1时，总是会对String类型的key使用备用的hash函数；值为-1，则一定不使用备用hash函数。
static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;
```

## 2、变量

基本没啥大的变化。

```java
/** The table, resized as necessary. Length MUST Always be a power of two. */
transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE; // 懒初始化，因此给一个空数组 ，其实空数组也没什么用，用null也行
 
transient int size;
final float loadFactor;
transient int modCount;
 
// 通常的扩容阈值，另外在懒初始化中，真正初始化时会使用这个值作为第一次的容量
int threshold;
 
// 一个hash种子，用于参与hash函数中的运算，获得更好的hash值来减少hash冲突
// 值为0时会禁止掉备用hash函数
// hashseed相关的可以不用理解太多，它只影响hash值的生成，以及扩容时是否需要重新计算hash值（rehash），本身对HashMap的基本的实现没影响
transient int hashSeed = 0;
 
private transient Set<Map.Entry<K,V>> entrySet = null;
// keySet values继承使用AbstractMap的父类的属性
```

# 三、基本类

```java
// Entry就一个变化，hash值不再是final的
static class Entry<K,V> implements Map.Entry<K,V> {
    final K key;
    V value;
    Entry<K,V> next;
    int hash; // 就这一个改变，hash不再是final的，扩容时可以重新计算hash值
 
    // 后面的跟1.6的一样，不说了
}
 
/** holds values which can't be initialized until after VM is booted.  */
// 这个类就是用来持有 ALTERNATIVE_HASHING_THRESHOLD的，影响String在极端情况下的hash值的计算，不影响HashMap基本的实现
private static class Holder {
 
    /**  Table capacity above which to switch to use alternative hashing. */
    static final int ALTERNATIVE_HASHING_THRESHOLD;
 
    static {
        String altThreshold = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction( "jdk.map.althashing.threshold")); 
 
        int threshold;
        try {
            threshold = (null != altThreshold) ? Integer.parseInt(altThreshold) : ALTERNATIVE_HASHING_THRESHOLD_DEFAULT;
 
            // disable alternative hashing if -1
            if (threshold == -1) {
                threshold = Integer.MAX_VALUE;
            }
 
            if (threshold < 0) {
                throw new IllegalArgumentException("value must be positive integer.");
            }
        } catch(IllegalArgumentException failed) {
            throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
        }
 
        ALTERNATIVE_HASHING_THRESHOLD = threshold;
    }
}
```

# 四、构造方法

## 构造器

主要是针对懒初始化的改动，比较简单，也没什么好说的。

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
 
    this.loadFactor = loadFactor;
    threshold = initialCapacity; // 初始容量保存在threshold中，真正初始化后threshold才是阈值
    init(); // 这个方法什么也没做
    // 初始化后数组table是默认值空数组，没有真正进行初始化
}
 
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
 
public HashMap() {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
}
 
// loadFactor使用默认值0.75f，因为m是接口类型，可能没有loadFactor这个属性
public HashMap(Map<? extends K, ? extends V> m) {
    this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
    inflateTable(threshold);
 
    putAllForCreate(m);
}
```

## 正式初始化

下面是真正的初始化的相关代码。

```java
// 真正初始化HashMap
private void inflateTable(int toSize) {
    // Find a power of 2 >= toSize
    int capacity = roundUpToPowerOf2(toSize);// 使用比较高大上的算法求不小于number的满足2^n的数
 
    threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);//待解决：为什么要+1？
    table = new Entry[capacity];
    initHashSeedAsNeeded(capacity);//初始化hashseed，这个是和hash函数相关的，有可能改变，
}
```

```java
private static int roundUpToPowerOf2(int number) {
    // assert number >= 0 : "number must be non-negative";
    return number >= MAXIMUM_CAPACITY
            ? MAXIMUM_CAPACITY
            : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1; 
}
 
// 初始化hash种子
// hashseed相关的可以不用理解太多，它只影响hash值的生成，以及扩容时是否需要重新计算hash值（rehash），本身对HashMap的基本的实现没影响
final boolean initHashSeedAsNeeded(int capacity) {
    boolean currentAltHashing = hashSeed != 0;
    boolean useAltHashing = sun.misc.VM.isBooted() && (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
    boolean switching = currentAltHashing ^ useAltHashing;
    if (switching) {
        hashSeed = useAltHashing ? sun.misc.Hashing.randomHashSeed(this) : 0;
    }
    return switching;
}
```

# 五、一些内部方法

这部分基本没变。

```java
// 多了个hashSeed，以及对String的特别处理
// null 依然视为hash = 0，总是放在index = 0的hash桶中
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }
 
    h ^= k.hashCode();
 
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
 
static int indexFor(int h, int length) {
    // assert Integer.bitCount(length) == 1 : "length must be a non-zero power of 2";
    return h & (length-1);
}
```

# 六、扩容

```java
// resize 修复了threshold过早地变为Integer.MAX_VALUE的问题，其余跟1.6一致
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) { // 此时数组才是真正不能扩容了
        threshold = Integer.MAX_VALUE;
        return;
    }
 
    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable, initHashSeedAsNeeded(newCapacity));
    table = newTable;
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    // 上面这行代码，可以避免1.6可能发生的因为newCapacity * LoadFacotr大于Integer.MAX_VALUE，强转int后变小，比size还小，导致后续再次扩容，浪费空间
    // 因此取未强转前，较小的MAXIMUM_CAPACITY，此时的newCapacity应该比较接近MAXIMUM_CAPACITY了（经历过很多次rehash，或者本身初始容量大），因此不会导致链条过长
}
 
// 多一个真正的rehash的判断，其余跟1.6的一致
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) { // 为true就要进行真正的rehash，因为hashseed改变了，其实可以拿出来，判断一次，不必每个节点都判断，浪费性能，拖延resize时间
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

# 七、常用方法

## 1、读操作

跟1.6的基本一样，但还是贴一下代码。

```java
public int size() {
    return size;
}
 
public boolean isEmpty() {
    return size == 0;
}
 
public V get(Object key) {
    if (key == null)
        return getForNullKey();
    Entry<K,V> entry = getEntry(key);
 
    return null == entry ? null : entry.getValue();
}
 
private V getForNullKey() {
    if (size == 0) {
        return null;
    }
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null)
            return e.value;
    }
    return null;
}
 
public boolean containsKey(Object key) {
    return getEntry(key) != null;
}
 
final Entry<K,V> getEntry(Object key) {
    if (size == 0) {
        return null;
    }
 
    int hash = (key == null) ? 0 : hash(key);
    for (Entry<K,V> e = table[indexFor(hash, table.length)];
         e != null;
         e = e.next) {
        Object k;
        if (e.hash == hash &&
            ((k = e.key) == key || (key != null && key.equals(k))))
            return e;
    }
    return null;
}
 
public boolean containsValue(Object value) {
    if (value == null)
        return containsNullValue();
 
    Entry[] tab = table;
    for (int i = 0; i < tab.length ; i++)
        for (Entry e = tab[i] ; e != null ; e = e.next)
            if (value.equals(e.value))
                return true;
    return false;
}
 
private boolean containsNullValue() {
    Entry[] tab = table;
    for (int i = 0; i < tab.length ; i++)
        for (Entry e = tab[i] ; e != null ; e = e.next)
            if (e.value == null)
                return true;
    return false;
}
```

## 2、写操作

除了懒加载导致put/remove的判断多了些外，变化的地方就是添加节点触发扩容那里变了，注释上写了。

#### put

```java
public V put(K key, V value) {
    if (table == EMPTY_TABLE) { // 考虑还未真正初始化的情况
        inflateTable(threshold);
    }
    if (key == null)
        return putForNullKey(value);
    int hash = hash(key);
    int i = indexFor(hash, table.length);
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
 
    modCount++;
    addEntry(hash, key, value, i);//添加节点
    return null;
}
//没变化
private V putForNullKey(V value) {
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    modCount++;
    addEntry(0, null, value, 0);
    return null;
}
```

#### addEntry

```java
// 这个跟1.6有些区别
// 1、走实用主义，扩容多了个条件。当添加的节点是hash桶的第一个节点时，一定不扩容，所以会出现size > threshold的情况。
// 2、几步的操作顺序不一样。jdk1.6的是先把节点添加到链表中，再判断是否扩容；1.7这里是先判断是否扩容，扩容完再把节点添加到链表中。
void addEntry(int hash, K key, V value, int bucketIndex) {
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);
        hash = (null != key) ? hash(key) : 0;//重新计算hash，因为resize有可能改变hashseed，导致hash发生变化
        bucketIndex = indexFor(hash, table.length);
    }
 
    createEntry(hash, key, value, bucketIndex);//调用 createEntry
}
```

#### createEntry

```java
	/**
     * Like addEntry except that this version is used when creating entries
     * as part of Map construction or "pseudo-construction" (cloning,
     * deserialization).  This version needn't worry about resizing the table.
     *
     * Subclass overrides this to alter the behavior of HashMap(Map),
     * clone, and readObject.
     */
void createEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K,V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<>(hash, key, value, e);
    size++;
}
```

#### putAll

```java
public void putAll(Map<? extends K, ? extends V> m) {
    int numKeysToBeAdded = m.size();
    if (numKeysToBeAdded == 0)
        return;
 
    if (table == EMPTY_TABLE) { // 考虑还未真正初始化的情况
        inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
    }
 
    // 跟1.6的一样，稍微保守些，多判断下
    if (numKeysToBeAdded > threshold) {
        int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
        if (targetCapacity > MAXIMUM_CAPACITY)
            targetCapacity = MAXIMUM_CAPACITY;
        int newCapacity = table.length;
        while (newCapacity < targetCapacity)
            newCapacity <<= 1;
        if (newCapacity > table.length)
            resize(newCapacity);
    }
 
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
        put(e.getKey(), e.getValue());
}
```

#### remove

```java
//和1.6都一样
public V remove(Object key) {
    Entry<K,V> e = removeEntryForKey(key);
    return (e == null ? null : e.value);
}
 
final Entry<K,V> removeEntryForKey(Object key) {
    if (size == 0) { // 考虑还未真正初始化的情况
        return null;
    }
    int hash = (key == null) ? 0 : hash(key);
    int i = indexFor(hash, table.length);
    Entry<K,V> prev = table[i];
    Entry<K,V> e = prev;
 
    while (e != null) {
        Entry<K,V> next = e.next;
        Object k;
        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
            modCount++;
            size--;
            if (prev == e)
                table[i] = next;
            else
                prev.next = next;
            e.recordRemoval(this);
            return e;
        }
        prev = e;
        e = next;
    }
 
    return e;
}
```

#### clear

```java
public void clear() {
    modCount++;
    Arrays.fill(table, null); // 就是循环赋值，和1.6没区别，就是代理给工具类做
    size = 0;
}
```

# 八、视图和迭代器

跟1.6的一样。

# 最后

1.7相对1.6，改动不大，基本上没啥太重要的改动。理解1.6的之后，稍微过下1.7的就行。
接下来看下1.8的，相对来说就是比较大的改动了。