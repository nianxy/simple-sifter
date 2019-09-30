package com.nianxy.simplesifter;

import java.util.HashMap;
import java.util.Map;

class StateNode {
    private char ch;
    private byte depth;
    private boolean end;
    private Map<Character,StateNode> children;

    public StateNode() {
        depth = 0;
        end = false;
        children = new HashMap<Character,StateNode>();
    }

    public char getCh() {
        return ch;
    }

    public byte getDepth() {
        return depth;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public StateNode getChild(char ch) {
        return children.get(ch);
    }

    public StateNode addChild(char ch) {
        StateNode node = new StateNode();
        node.ch = ch;
        node.depth = (byte)(depth + 1);
        node.end = false;
        children.put(ch, node);
        return node;
    }

    private void print(String prefix, boolean needLinkNeighbor, StateNode node) {
        StringBuilder line = new StringBuilder();
        line.append(prefix);
        line.append("+ ");
        line.append(node.ch);
        if (node.isEnd()) {
            line.append("(E)");
        }
        System.out.println(line);

        if (needLinkNeighbor) {
            prefix += "| ";
        } else {
            prefix += "  ";
        }
        int i = 0;
        for (StateNode child:node.children.values()) {
            boolean neighbor = i<node.children.size()-1;
            print(prefix, neighbor, child);
            ++i;
        }
    }

    /**
     * 打印状态树结点，可用于调试
     */
    public void print() {
        print("", children.size()>0, this);
    }
}
