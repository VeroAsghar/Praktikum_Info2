package org.tudresden.info2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Random;

public class Spielfeld {

    private static Spielfeld instance;

    private static final int BREITE = 1000;
    private static final int LAENGE = 1000;

    private ArrayList<Rechteck> hindernisse;

    private static Random zufallsgenerator;
    private static Leinwand leinwand;
    private Roboter robot;

    private Punkt[] poi;

    // private constructor
    private Spielfeld() {
        zufallsgenerator = new Random();
        leinwand = Leinwand.getInstance();
        this.robot = new Roboter(new Punkt(0, 0), Color.GREEN, 30);
        this.zeichnen(this.robot);
    }

    // singleton
    public static Spielfeld getInstance() {
        if(instance == null) {
            return instance = new Spielfeld();
        } else {
            return instance;
        }
    }

    // gets the robot
    public Roboter getRobot() {
        return this.robot;
    }

    // function to add in points for the robot to take
    public Punkt[] punkte_eingeben() {
        try {
            System.out.println("How many Points of Interest?:");
            int i = Main.scan.nextInt();
            Punkt[] enteredPoi = new Punkt[i];
            int x, y;

            for(int index = 0; index < i; index++) {
                System.out.println("X: ");
                x = Main.scan.nextInt();
                System.out.println("Y: ");
                y = Main.scan.nextInt();
                enteredPoi[index] = new Punkt(x, y);
            }

            return enteredPoi;

        } catch(InputMismatchException e) {
            System.out.println(e);
            throw e;
        }
    }

    // sorts out the given points based on absolute distance between them
    public void POI_sortieren() {
        
        this.poi = punkte_eingeben();
        
        Punkt nextPoint;
        double sD, pSD;

        for(int x = 0; x < this.poi.length; x++) {
            sD = 0;
            pSD = Double.MAX_VALUE;
            for(int y = x + 1; y < this.poi.length; y++) {
                sD = this.poi[x].gibAbstand(poi[y]);
                if(sD < pSD) {
                    nextPoint = poi[x + 1];
                    poi[x + 1] = poi[y];
                    poi[y] = nextPoint;
                    pSD = sD;
                }       
            }
        }
    }

    // creates a random number of obstacles between 15 and 50
    public void hindernissliste_erzeugen() {
        try {
            System.out.println("How many Hindernisse?");
            int size = Main.scan.nextInt();
            this.hindernisse = new ArrayList<Rechteck>(size);

            Rechteck neuHinderniss;
            int count = 0;

            for(int index = 0; index < size; index++){
                if(count < 50) {
                    neuHinderniss = this.zufallsrechteck(index);
                    this.hindernisse.add(neuHinderniss);
                    Iterator<Rechteck> iter = this.hindernisse.iterator();
                    int iterI = 0;
                    while(iter.hasNext() && iterI != this.hindernisse.size() - 1){
                        if(iter.next().ueberlappt(neuHinderniss)) {
                            iter.remove();
                            count++;
                        }
                        iterI++;
                    }
                }
                leinwand.warten(1);
            }
            
            this.zeichnen(hindernisse);
            // s.close();

        } catch(Exception e) {
            // s.close();
            throw e;
        }
    }

    // robot navigation code, iterates over each point given by the punkte_eingeben() function
    public void hindernisse_umfahren() {
        for(Punkt p : this.poi) {
            while(this.robot.getPosition().gibAbstand(p) > 50 && Roboter.status != Roboter.Status.FINISH) {
                Roboter.status = Roboter.Status.CONTINUE;
                for(int index = 0; index < this.hindernisse.size(); index++) {
                    if(this.robot.ZuNah_vertikaleKante(this.hindernisse.get(index), 35.0)) {
                        for(int indexD = 0; indexD < this.hindernisse.size(); indexD++) {
                            if(this.robot.ZuNah_horizontaleKante(this.hindernisse.get(indexD), 20.0) && indexD != index) {
                                Roboter.status = Roboter.Status.FINISH;
                                break; 
                            } else {
                                Roboter.status = Roboter.Status.MOVEDOWN;
                            }

                        }
                    }
                    if(this.robot.ZuNah_horizontaleKante(this.hindernisse.get(index), 35.0)) {
                        for(int indexR = 0; indexR < this.hindernisse.size(); indexR++) {
                            if(this.robot.ZuNah_vertikaleKante(this.hindernisse.get(indexR), 20.0) && indexR != index) {
                                Roboter.status = Roboter.Status.FINISH;
                                break;
                            } else {
                                Roboter.status = Roboter.Status.MOVERIGHT;
                            }
                            
                        }
                        
                    }
                    if(this.robot.anWand(Spielfeld.BREITE, Spielfeld.LAENGE)) {
                        Roboter.status = Roboter.Status.FINISH;
                        break;
                    }
                }
                switch(Roboter.status) {
                    case CONTINUE:
                        int dx = p.getX() - this.robot.getPosition().getX();
                        int dy = p.getY() - this.robot.getPosition().getY();
                        this.robot.bewegeUm((int)Math.ceil(dx/p.gibAbstand(this.robot.getPosition())), (int) Math.ceil(dy/p.gibAbstand(this.robot.getPosition())));
                        break;
                    case MOVEDOWN:
                        this.robot.bewegeUm(0,1);
                        break;
                    case MOVERIGHT:
                        this.robot.bewegeUm(1,0);
                        break;
                    case FINISH:
                        System.out.println("Robot cannot continue...");
                        break;
                }
                this.zeichnen(robot);
                leinwand.warten(10);
            }
        }
    }

    public void zeichnen(ArrayList<Rechteck> hindernisse) {
        leinwand.zeichnen(hindernisse);
    }

    public void zeichnen(Roboter robot) {
        leinwand.zeichnen(robot);
    }
    
    // random number generator
    private int zufallszahl(int von, int bis) {
        return zufallsgenerator.nextInt(bis - von + 1) + von; 
    }

    // random color generator
    private Color zufallsfarbe() {
        return new Color(this.zufallszahl(50, 255), this.zufallszahl(50, 255), this.zufallszahl(50, 255));
    }

    // random rectangle generator
    private Rechteck zufallsrechteck(int index) {
        return new Rechteck(new Punkt(this.zufallszahl(50, BREITE), this.zufallszahl(50, LAENGE)), zufallszahl(25, 100), zufallszahl(25, 100), "Rechteck " + index, zufallsfarbe());
    }

    
}