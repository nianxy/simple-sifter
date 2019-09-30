<div style="text-align: center">simplesifter</div>

### 项目功能
过滤掉文本中的指定词汇列表，可用于敏感词过滤

### 原理
使用DFA状态树，最好的情况O(n)，最坏情况O(n!)
```properties
# 最好情况举例：
# 过滤词: 你好
# 文本源: 北京欢迎你

# 最坏情况举例：
# 过滤词: aaaab
# 文本源: aaaaa
```

### 参考
[《敏感词过滤的算法原理之DFA算法》](https://blog.csdn.net/cdj0311/article/details/79789480)

### 对于参考算法的改进
上述参考文章算法并不完善，因为无法处理以下情况：
```properties
# 过滤词： 12345 235
# 文本源： 1235
```
参考算法对以上文本源的处理是无任何命中，但很显然，235应该被命中。
解决办法有两个：
1. 当匹配分支失败后，对文本源的处理应该回溯到首个匹配字符的下一个位置，继续尝试匹配，而不是从当前位置继续尝试匹配
2. 遍历文本源过程中，对每个出现的字符都在DFA中进行首字匹配，而不论当前是否有匹配的分支；如果当前有多个匹配分支，则需要同时处理多个分支

`simple-sifter`使用了第2种解决办法（其实，第1种办法应该更简单一些。可是我为什么要用第2种呢？我也想不明白了:cry:）


### 使用方法
```java
import com.nianxy.simplesifter;

// ...

// 创建一个WordSifter对象
WordSifter wordSifter = new WordSifter();
wordSifter.loadWords(words);

// 打印DFA结点，可用于调试
wordSifter.printRoot();

// 创建WordSifter.Filter对象，用于过滤文本
WordSifter.Filter filter = wordSifter.createFilter();

// 可选，设置用于替换命中词的字符串，如果不设置默认为"**"
filter.setReplaceStr("###");

// 过滤文本
String filtered = filter.filter("some text");

// filter对象可以重复使用，每次都可以通过setReplaceStr()设置不同的替换字符串
// 但它并不是线程安全的，如果请在不同线程内创建单独的filter对象
```