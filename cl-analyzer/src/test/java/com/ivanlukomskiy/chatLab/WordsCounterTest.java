package com.ivanlukomskiy.chatLab;

import org.junit.Test;

import static com.ivanlukomskiy.chatsLab.service.GatheringService.getWordsNumber;
import static org.junit.Assert.assertEquals;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 21.10.2017.
 */
public class WordsCounterTest {

    @Test
    public void test1() {
        assertEquals(getWordsNumber("Привет"),1);
    }

    @Test
    public void test2() {
        assertEquals(getWordsNumber("А на что ты подаешь ?"),5);
    }

    @Test
    public void test3() {
        assertEquals(getWordsNumber("У Ксюши 23.09 ДР, в твин-день с Бергамо"),9);
    }

    @Test
    public void test4() {
        assertEquals(getWordsNumber("А, ну да"),3);
    }

    @Test
    public void test5() {
        assertEquals(getWordsNumber("?"),0);
    }

    @Test
    public void test6() {
        assertEquals(getWordsNumber("Оля, мы фоткаемся, можедб выйти?"),5);
    }

    @Test
    public void test7() {
        assertEquals(getWordsNumber("Путь 4"),2);
    }

    @Test
    public void test8() {
        assertEquals(getWordsNumber("6"),1);
    }

    @Test
    public void test9() {
        assertEquals(getWordsNumber("Ну как1"),2);
    }
    @Test
    public void test10() {
        assertEquals(getWordsNumber(""),0);
    }
    @Test
    public void test11() {
        assertEquals(getWordsNumber("1 2 3"),3);
    }
    @Test
    public void test12() {
        assertEquals(getWordsNumber(" раз.два;три\"четыре пять,шесть   "),6);
    }
    @Test
    public void test13() {
        assertEquals(getWordsNumber(" скобочки)))) )   "),1);
    }
    @Test
    public void test14() {
        assertEquals(getWordsNumber("))))"),0);
    }
    @Test
    public void test15() {
        assertEquals(getWordsNumber("Ктооо?)"),1);
    }
}
