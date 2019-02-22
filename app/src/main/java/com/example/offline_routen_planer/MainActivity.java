package com.example.offline_routen_planer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("Routenplaner started");

        CSA csa = new CSA();


        long timeStart = System.currentTimeMillis();
        //csa.makeConnections();
        //csa.read();
        long timeEnd = System.currentTimeMillis();

        long timeStart2 = System.currentTimeMillis();
        csa.sort();
        long timeEnd2 = System.currentTimeMillis();

        System.out.println("Laufzeit (makeConnection): " + (timeEnd - timeStart) / 1000 + " s");
        System.out.println("Laufzeit (sort): " + (timeEnd2 - timeStart2) + " ms");
        System.out.println();
        //Input input = new Input(csa);
        //run(csa);

        //csa.csa("Stagecoach Hotel & Casino (Demo)",0,"E Main St / S Irving St (Demo)");
        //csa.csa("Kleiststraße", "Feuersee", 140000, 20190124);
    }
}
