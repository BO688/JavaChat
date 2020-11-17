package com.company.BIO.TCP.client;

import javax.swing.*;

public class UpdateScroll extends Thread{
        JScrollPane J;
        UpdateScroll(JScrollPane J){
        this.J=J;
        }
@Override
public void run() {
        JScrollBar scrollBar=J.getVerticalScrollBar();
        int Old=scrollBar.getMaximum();
        int New=scrollBar.getMaximum();
        int count=0;
        while (Old==New&&count<20){
        try {
        Thread.sleep(100);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        New=scrollBar.getMaximum();
        scrollBar.setValue(scrollBar.getMaximum());
        count++;
        }
        }
        }