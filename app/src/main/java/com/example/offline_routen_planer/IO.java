package com.example.offline_routen_planer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IO {

    public BufferedReader readFile(String file) {
        BufferedReader br = null;

        try {
            br  = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8")); //"ISO-8859-1"
            br.readLine(); // skip first line
        } catch (IOException e) {
            e.printStackTrace();
        }

        return br;
    }

    protected void save(Object o) {
        try {
            FileOutputStream fos = new FileOutputStream("C://Users//Jonas//Desktop//Bachelorarbeit//data.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    protected int[][] read() {
        int[][] al = null;
        try {
            FileInputStream fis = new FileInputStream("C://Users//Jonas//Desktop//Bachelorarbeit//data.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            al =  (int[][]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return al;

    }

}
