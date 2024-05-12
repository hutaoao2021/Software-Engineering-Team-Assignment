package com.yrp.utils;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * 生产者
 */
class Producer implements Runnable {
    private BlockingQueue<Integer> q;

    public Producer(BlockingQueue<Integer> q) {
        this.q = q;
    }
    @Override
    public void run() {
        try {
            for(int i = 0 ; i< 100; i ++){
                Thread.sleep(20);
                q.put(i);
                System.out.println(Thread.currentThread().getName() + "生产" + q.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
class Consumer implements Runnable {
    private BlockingQueue<Integer> q;
    public  Consumer(BlockingQueue<Integer> q) {
        this.q = q;
    }
    @Override
    public void run() {
        try {
            while(true){
                Thread.sleep(new Random().nextInt(1000));
                q.take();
                System.out.println(Thread.currentThread().getName() + "消费" + q.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}