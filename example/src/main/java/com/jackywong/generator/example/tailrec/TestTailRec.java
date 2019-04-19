package com.jackywong.generator.example.tailrec;

import com.jackywong.generator.annotation.TailRec;

import java.util.List;

/**
 * Created by huangziqi on 2019/4/19
 */
public class TestTailRec {

    @TailRec
    public long fact(long num, long times){
        if(times == 0)
            return num;
        return fact(num+times, times -1);
    }

    public long factOld(long num, long times){
        if(times == 0)
            return num;
        return factOld(num+times, times -1);
    }

    @TailRec
    public long list(long res, List<Long> list) {
        if(list.isEmpty()){
            return res;
        } else {
            Long head = list.get(0);
            List<Long> tail = list.subList(1,list.size());
            res = res + head;
            return list(res,tail);
        }
    }
}
