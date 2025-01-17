<div style="text-align: center">simplesifter</div>

### 项目功能
过滤掉文本中的指定词汇列表，可用于敏感词过滤

### 原理
使用DFA状态树，最好的情况O(n)，最坏情况O(n!)
```properties
# 最好情况举例：
# 过滤词: 欢迎
# 文本源: 北京欢迎你

# 最坏情况举例：
# 过滤词: aaaab
# 文本源: aaaaa
```

### 参考
[《敏感词过滤的算法原理之DFA算法》](https://blog.csdn.net/chenssy/article/details/26961957)

### 对于参考算法的改进
上述参考文章算法并不完善，因为无法处理以下情况：
```properties
# 过滤词： 12345 235
# 文本源： 1235
```
参考算法对以上文本源的处理是无任何命中，但很显然，235应该被命中。
解决办法有两个：
1. 当匹配分支失败后，对文本源的处理应该回溯到首个匹配字符的下一个位置，继续尝试匹配，而不是从当前位置继续尝试匹配
2. 遍历文本源过程中，对每个出现的字符都在DFA中进行首字匹配，而不论当前是否有匹配的分支；如果当前有多个匹配分支，则需要同时处理多个分支。这个方法实现起来会更复杂一些，但是效率比第一个高

`simple-sifter`使用了第2种解决办法


### 使用方法
```java
import com.nianxy.simplesifter;

// ...

// 创建一个WordSifter对象
WordSifter wordSifter = new WordSifter();

// 加载过滤词列表，可以是段文本，每行一个过滤词
wordSifter.loadWords(words);
// 或是一个过滤词列表
// wordSifter.loadWords(List<String> words);

// 打印DFA结点，可用于调试
wordSifter.printRoot();

// 创建WordSifter.Filter对象，用于过滤文本
WordSifter.Filter filter = wordSifter.createFilter();

// 可选，设置用于替换命中词的字符串，如果不设置默认为"**"
filter.setReplaceStr("###");

// 过滤文本
String filtered = filter.filter("some text");

// filter对象可以重复使用，每次都可以通过setReplaceStr()设置不同的替换字符串
// 但它并不是线程安全的，请在不同线程内通过wordSifter.createFilter()创建单独的filter对象
```

### 性能测试
简单做了一下性能测试，用例如下：
- 1000个过滤词
- 100,000个字符的文本源
- 完成不同次数的过滤

测试环境：我的2016款MacbookPro

测试结果：
```bash
# 第1次测试
Loops:10, time cost(ms):194, avg(ms):19.4
Loops:100, time cost(ms):405, avg(ms):4.05
Loops:1000, time cost(ms):3088, avg(ms):3.088
# 第2次测试
Loops:10, time cost(ms):205, avg(ms):20.5
Loops:100, time cost(ms):419, avg(ms):4.19
Loops:1000, time cost(ms):3172, avg(ms):3.172
# 第3次测试
Loops:10, time cost(ms):177, avg(ms):17.7
Loops:100, time cost(ms):418, avg(ms):4.18
Loops:1000, time cost(ms):3149, avg(ms):3.149
```
