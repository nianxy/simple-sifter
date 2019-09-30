package com.nianxy.simplesifter;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

public class WordSifterTest {
    @Ignore
    @Test
    public void test1() {
        String words = "一二三四 \n 二三四 \n 一二五 \n 一二三四五六七 \n 123 \n";

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        wordSifter.printRoot();
    }

    @Test
    public void test2() {
        String words = "12\n1234\n12345\n13\n235\n34\n346\n";

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        //wordSifter.printRoot();

        WordSifter.Filter filter = wordSifter.createFilter();

        Assert.assertEquals("**6", filter.filter("12346"));
        Assert.assertEquals("**6", filter.filter("123456"));
        Assert.assertEquals("****", filter.filter("1334"));
        Assert.assertEquals("111**35", filter.filter("1111235"));
        Assert.assertEquals("2**", filter.filter("2346"));
        Assert.assertEquals("****", filter.filter("235346"));
        Assert.assertEquals("2366497979", filter.filter("2366497979"));
        Assert.assertEquals("6598789**222**555**6", filter.filter("659878913222346555123456"));
    }

    @Test
    public void test3() {
        String words  = "aaaaaab";
        String text   = "aaaaaaa";
        String wanted = "aaaaaaa";

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        //wordSifter.printRoot();

        WordSifter.Filter filter = wordSifter.createFilter();

        Assert.assertEquals(wanted, filter.filter(text));
    }

    @Test
    public void test4() {
        String words = "国庆\n电信\n电脑\n中华人民共和国\n中国";

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        //wordSifter.printRoot();

        WordSifter.Filter filter = wordSifter.createFilter();

        Assert.assertEquals("**迎来了70周年**节", filter.filter("中国迎来了70周年国庆节"));
        Assert.assertEquals("**是中华人民共和的简称", filter.filter("中国是中华人民共和的简称"));
        Assert.assertEquals("我是**的公民，我有一台电话电**", filter.filter("我是中华人民共和国的公民，我有一台电话电电脑"));
    }

    @Test
    public void test5() {
        String words = "国庆\n电信\n电脑\n中华人民共和国\n中国";

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        //wordSifter.printRoot();

        WordSifter.Filter filter = wordSifter.createFilter();
        filter.setReplaceStr("#");

        Assert.assertEquals("#迎来了70周年#节", filter.filter("中国迎来了70周年国庆节"));
        Assert.assertEquals("#是中华人民共和的简称", filter.filter("中国是中华人民共和的简称"));
        Assert.assertEquals("我是#的公民，我有一台电话电#", filter.filter("我是中华人民共和国的公民，我有一台电话电电脑"));
    }

    private String loadFile(String path) throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        //BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
        BufferedInputStream bis = new BufferedInputStream(this.getClass().getResourceAsStream(path));
        byte[] data = new byte[4096];
        int rsz;
        while ((rsz=bis.read(data))>0) {
            content.write(data, 0, rsz);
        }
        return new String(content.toByteArray(), "utf8");
    }

    private void doFilterLoop(WordSifter.Filter filter, String doc, int count) {
        long tm = System.currentTimeMillis();
        for (int i=0; i<count; ++i) {
            filter.filter(doc);
        }
        long tm2 = System.currentTimeMillis();
        System.out.println("Loops:" + count + ", time cost(ms):" + (tm2-tm) + ", avg(ms):" + (tm2-tm)*1.0/count);
    }

    @Test
    public void testCap1() throws IOException {
        // 1000个关键词
        String words = loadFile("/sifterwords.txt");

        // 100,000个字符的文本文件
        String doc = loadFile("/doc.txt");

        WordSifter wordSifter = new WordSifter();
        wordSifter.loadWords(words);
        //wordSifter.printRoot();

        System.out.println("words count:" + wordSifter.getWordCount() + ", doc size:" + doc.length());

        WordSifter.Filter filter = wordSifter.createFilter();

        doFilterLoop(filter, doc, 10);
        doFilterLoop(filter, doc, 100);
        doFilterLoop(filter, doc, 1000);
    }
}
