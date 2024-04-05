package com.playtech.assignment.util;

import com.playtech.assignment.pojo.BinMapping;

import java.util.ArrayDeque;
import java.util.Deque;

public class IntervalTree {
    private IntervalNode root;

    public IntervalTree() {
        this.root = null;
    }

    public void insert(BinMapping binMapping) {
        root = insert(root, binMapping);
    }

    private IntervalNode insert(IntervalNode root, BinMapping binMapping) {
        IntervalNode newNode = new IntervalNode(binMapping.getRangeFrom(), binMapping.getRangeTo(), binMapping);
        if (root == null) {
            return newNode;
        }

        IntervalNode current = root;
        IntervalNode parent = null;

        while (current != null) {
            parent = current;
            if (binMapping.getRangeFrom() < current.low) {
                current = current.left;
            } else if (binMapping.getRangeFrom() > current.high) {
                current = current.right;
            } else {
                current.low = Math.min(current.low, binMapping.getRangeFrom());
                current.high = Math.max(current.high, binMapping.getRangeTo());
                return root;
            }
        }

        if (binMapping.getRangeFrom() < parent.low) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        return root;
    }

    public BinMapping search(long point) {

        Deque<IntervalNode> stack = new ArrayDeque<>();
        IntervalNode current = root;

        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            if (point >= current.low && point <= current.high) {
                return current.binMapping;
            }
            current = current.right;
        }

        return null;
    }

    private static class IntervalNode {
        long low;
        long high;
        BinMapping binMapping;
        IntervalNode left;
        IntervalNode right;

        IntervalNode(long low, long high, BinMapping binMapping) {
            this.low = low;
            this.high = high;
            this.binMapping = binMapping;
        }
    }
}
