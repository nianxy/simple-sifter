package com.nianxy.simplesifter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class WordSifter {
    private StateNode sensitiveRoot;

    private int wordCount;

    public WordSifter() {
        sensitiveRoot = new StateNode();
        wordCount = 0;
    }

    private static void addWord(StateNode root, String word) {
        StateNode current = root;
        for (int i=0; i<word.length(); ++i) {
            StateNode node = current.getChild(word.charAt(i));
            if (node==null) {
                // 新分支， 追加所有字符
                while (i<word.length()) {
                    current = current.addChild(word.charAt(i++));
                }
                break;
            } else {
                // 命中现有分支，往后走
                current = node;
            }
        }
        if (current!=root) {
            current.setEnd(true);
        }
    }

    /**
     * 加载过滤词，以换行符分隔
     * @param words
     */
    protected void loadWords(String words) {
        // 构建一棵新的状态树
        StateNode root = new StateNode();
        int count = 0;
        int lastp = 0;
        while (lastp<words.length()) {
            int p = words.indexOf('\n', lastp);
            // 提取过滤词
            String word;
            if (p<0) {
                word = words.substring(lastp).trim();
            } else {
                word = words.substring(lastp, p).trim();
            }
            // 加入状态树
            if (!word.isEmpty()) {
                addWord(root, word);
                ++count;
            }
            //
            if (p<0) {
                break;
            } else {
                lastp = p+1;
            }
        }

        // 指向新树
        sensitiveRoot = root;
        wordCount = count;
    }

    /**
     * 加载过滤词
     * @param words
     */
    protected void loadWords(List<String> words) {
        // 构建一棵新的状态树
        StateNode root = new StateNode();
        int count = 0;
        for (String word:words) {
            word = word.trim();
            if (!word.isEmpty()) {
                addWord(root, word);
                ++count;
            }
        }

        // 指向新树
        sensitiveRoot = root;
        wordCount = count;
    }

    /**
     * 获取过滤词总数
     * @return
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * 打印状态树结点信息，用于调试
     */
    public void printRoot() {
        sensitiveRoot.print();
    }

    private static class HitNode {
        StateNode start;
        int startPosition;
        StateNode current;
        // 当前搜索路径上的最后一个结束结点
        StateNode end;
    }

    public Filter createFilter() {
        return new Filter(sensitiveRoot);
    }

    public static class Filter {
        /**
         * 保存当前状态树，防止过程中状态树被更新
         */
        private StateNode root;

        /**
         * 过滤后的内容
         */
        private StringBuilder filteredContent;

        /**
         * 当前向filteredContent追加内容的索引
         */
        private int contentAppendIndex;

        /**
         * 当前命中的结点列表
         */
        private LinkedList<HitNode> hitNodes;

        /**
         * 当前已匹配成功的结点列表
         */
        private LinkedList<HitNode> successNodes;

        /**
         * 替换字符串
         */
        private String replaceStr;

        protected Filter(StateNode root) {
            this.root = root;
            replaceStr = "**";
        }

        /**
         * 设置替换用的字符串，默认为"**"
         * @param replaceStr
         */
        public void setReplaceStr(String replaceStr) {
            this.replaceStr = replaceStr;
        }

        protected void reset() {
            filteredContent = new StringBuilder();
            contentAppendIndex = 0;
            hitNodes = new LinkedList<HitNode>();
            successNodes = new LinkedList<HitNode>();
        }

        private void addSortedHitNode(LinkedList<HitNode> nodes, HitNode addNode) {
            for (ListIterator<HitNode> dd = nodes.listIterator(); dd.hasNext();) {
                HitNode hit = dd.next();
                if (addNode.startPosition<hit.startPosition) {
                    dd.previous();
                    dd.add(addNode);
                    return;
                }
            }
            nodes.addLast(addNode);
        }

        private void applySuccessNodes(String content) {
            if (successNodes.size()>0) {
                // 先出现的命中，优先处理
                int lastHitEndPosition = 0;
                for (Iterator<HitNode> dd = successNodes.iterator(); dd.hasNext();) {
                    HitNode hit = dd.next();
                    if (hit.startPosition<lastHitEndPosition) {
                        // 与前一个命中序列重叠，不处理
                        continue;
                    }
                    lastHitEndPosition = hit.startPosition + hit.end.getDepth();
                    filteredContent.append(content, contentAppendIndex, hit.startPosition).append(replaceStr);
                    contentAppendIndex = lastHitEndPosition;
                }
                successNodes.clear();
            }
        }

        public String filter(String content) {
            reset();

            for (int i=0; i<content.length(); ++i) {
                char c = content.charAt(i);
                StateNode layer1Node = root.getChild(c);

                // 在已命中结点中继续寻找
                for (Iterator<HitNode> dd = hitNodes.iterator(); dd.hasNext();) {
                    HitNode hit = dd.next();
                    // 寻找命中的子结点
                    StateNode nextNode = hit.current.getChild(c);
                    if (nextNode==null) {
                        // 该分支走到尽头
                        if (hit.end!=null) {
                            // 路径上有结束结点
                            addSortedHitNode(successNodes, hit);
                        }
                        // 删除该命中分支
                        dd.remove();
                    } else {
                        // 命中结点
                        hit.current = nextNode;
                        // 检查结束标记
                        if (nextNode.isEnd()) {
                            hit.end = nextNode;
                        }
                    }
                }

                // 如果上述寻找一遍后，发现已命中结点列表为空，则处理一次successNodes
                if (hitNodes.size()==0) {
                    applySuccessNodes(content);
                }

                // 命中新的一级结点
                if (layer1Node!=null) {
                    HitNode newNode = new HitNode();
                    newNode.start = layer1Node;
                    newNode.current = layer1Node;
                    newNode.end = null;
                    newNode.startPosition = i;
                    hitNodes.add(newNode);
                }
            }

            // 命中序列中有结束标记的，加入success列表
            for (Iterator<HitNode> dd = hitNodes.iterator(); dd.hasNext();) {
                HitNode hit = dd.next();
                if (hit.end!=null) {
                    addSortedHitNode(successNodes, hit);
                }
            }

            // 处理现有命中序列
            applySuccessNodes(content);

            // 追加剩余字符
            filteredContent.append(content, contentAppendIndex, content.length());

            return filteredContent.toString();
        }
    }
}
