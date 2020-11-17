package com.company.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyLock {
    public volatile static Map<Class ,Boolean> LockMap=new HashMap();
    public static void Lock(Class c){
        if(LockMap.get(c)==null||!LockMap.get(c)){
            LockMap.put(c,true);
            while (LockMap.get(c)){
                try {
//                    System.out.println(LockMap.get(c));
//                    System.out.println(c+"锁定");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            throw new IllegalStateException("这个类已经锁定了");
        }
    }
   public static void Unlock(Class c){
       while (LockMap.get(c)==null||!LockMap.get(c)){
           try {
//               System.out.println(LockMap.get(c));
//               System.out.println(c+"还未锁定");
               Thread.sleep(100);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
        LockMap.put(c,false);
       System.out.println(c+"解锁");
    }

    public static void ShowALLLock(){
        Iterator<Map.Entry<Class,Boolean>> i=LockMap.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry<Class,Boolean> MapEntry=i.next();
            System.out.println(MapEntry.getKey());
            System.out.println(MapEntry.getValue());

        }

        }




}
