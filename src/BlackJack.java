import java.util.ArrayList; 
import java.util.Random;    

import javax.swing.*;       

import java.awt.*;          
import java.awt.event.*;   

import java.io.File;
import javax.sound.sampled.*;


public class BlackJack { 
    private class Karte { 
        String typ; 
        String wert; 

        Karte(String wert, String typ) { 
            this.typ = typ;
            this.wert = wert;
        }

        @Override
        public String toString() { 
            return typ + "-" + wert;
        }

        public int getValue() {  //gibt den wert der karte fuer das spiel zurueck  
            if (wert.equals("Ass")) { 
                return 11;
            }
            
            if (wert.equals("Bube") || wert.equals("Dame") || wert.equals("Koenig")) {
                return 10;
            }
            return Integer.parseInt(wert); //return karten 2-10 
        }
        
        public boolean isAss() {
            return wert.equals("Ass");
        }

        public String getImagePath() { 
            return "./cardsDE/" + toString() + ".png";
        }
    }

    ArrayList<Karte> stapel; 
    Random random = new Random();
    MusikSpieler musikSpieler = new MusikSpieler();

    //dealer variablen
    Karte umgedrehteKarte;
    ArrayList<Karte> dealerHand;
    int dealerPunkte;
    int dealerAssZaehler;

    //spieler variablen
    ArrayList<Karte> spielerHand;
    ArrayList<Karte> spielerTeilenHand1;
    ArrayList<Karte> spielerTeilenHand2;
    boolean hatGeteilt = false;
    int aktuelleHand = 1; // 1 = spielerTeilenHand1, 2 = spielerTeilenHand2
    int spielerPunkte;
    int spielerAssZaehler;
    boolean spielerGewinnt = false;

    int konto = 10; //"guthaben"
    JLabel kontoLabel;
    int highscore = 0;
    JLabel highscoreLabel;
    int verdoppelnKonto;
    boolean verdoppelnCount = false;

    //fensterFrame variablen in pixel
    int fensterFrameBreite = 1094;
    int fensterFrameHoehe = 827; 

    //kartenFrame variablen pixel
    int karteBreite = 200;
    int karteHoehe = 280; //ratio 1 zu 1.4

    JFrame fensterFrame = new JFrame("Black Jack");
    JPanel spielPanel = new JPanel() {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            Image umgedrehteKarteImg = new ImageIcon(getClass().getResource("./cardsDE/BACK.png")).getImage();
            if (!passenButton.isEnabled()) {
                umgedrehteKarteImg = new ImageIcon(getClass().getResource(umgedrehteKarte.getImagePath())).getImage();
            }
            g.drawImage(umgedrehteKarteImg, 25, 20, karteBreite, karteHoehe, null);

            //zeichnet karten dealer
            for (int i = 0; i < dealerHand.size(); i++) {
                Karte karte = dealerHand.get(i);
                Image karteImg = new ImageIcon(getClass().getResource(karte.getImagePath())).getImage();
                g.drawImage(karteImg, karteBreite + 35 + (karteBreite + 10) * i, 20, karteBreite, karteHoehe, null);
            }

            if (hatGeteilt == true) {
                //zeichnet karten spieler (hand 1)
                for (int i = 0; i < spielerTeilenHand1.size(); i++) {
                    Karte karte = spielerTeilenHand1.get(i);
                    Image karteImg = new ImageIcon(getClass().getResource(karte.getImagePath())).getImage();
                    g.drawImage(karteImg, 25 + (karteBreite + 10) * i, 450, karteBreite, karteHoehe, null); // Hand 1 links
                }

                //zeichnet karten spieler (hand 2)
                for (int i = 0; i < spielerTeilenHand2.size(); i++) {
                    Karte karte = spielerTeilenHand2.get(i);
                    Image karteImg = new ImageIcon(getClass().getResource(karte.getImagePath())).getImage();
                    g.drawImage(karteImg, 635 + (karteBreite + 10) * i, 450, karteBreite, karteHoehe, null); // Hand 2 rechts
                }

            } else {
                //zeichnet karten der urspruenglichen hand
                for (int i = 0; i < spielerHand.size(); i++) {
                    Karte karte = spielerHand.get(i);
                    Image karteImg = new ImageIcon(getClass().getResource(karte.getImagePath())).getImage();
                    g.drawImage(karteImg, 25 + (karteBreite + 10) * i, 450, karteBreite, karteHoehe, null); // Urspruengliche Hand
                }
            }

            if (spielerHand.size() > 2) {
                teilenButton.setEnabled(false);
                verdoppelnButton.setEnabled(false);
            }

            if (!passenButton.isEnabled()) {
                dealerPunkte = reduceDealerAss();
                spielerPunkte = reduceSpielerAss();
                System.out.println("ENDERGEBNIS: ");
                System.out.println(dealerPunkte);
                System.out.println(spielerPunkte);

                //konto +-
                if (spielerPunkte > 21) {
                    konto -= 5;
                    spielerGewinnt = false;
                    verdoppelnCount = false;
                } else if (dealerPunkte > 21) {
                    konto += 5;
                    spielerGewinnt = true;
                } else if (spielerPunkte == dealerPunkte) {
                    konto -= 5;
                    spielerGewinnt = false;
                    verdoppelnCount = false;
                } else if (spielerPunkte > dealerPunkte) {
                    konto += 5;
                    spielerGewinnt = true;
                } else if (spielerPunkte < dealerPunkte) {
                    konto -= 5;
                    spielerGewinnt = false;
                    verdoppelnCount = false;
                }

                if (spielerGewinnt == true && verdoppelnCount == true) {
                    konto = (verdoppelnKonto*2);
                    verdoppelnCount = false;
                }

                if (spielerGewinnt == true && konto > highscore) {
                    highscore = konto;
                }

                if (spielerGewinnt == false && konto <= 0)  {
                    konto = 0;
                }

                // Siegesbedingungen
                String nachricht = "";
                if (spielerPunkte > 21) {
                    nachricht = "Du hast Verloren!";
                } else if (dealerPunkte > 21) {
                    nachricht = "Du hast Gewonnen!";
                } else if (spielerPunkte == dealerPunkte) {
                    nachricht = "Unentschieden!";
                } else if (spielerPunkte > dealerPunkte) {
                    nachricht = "Du hast Gewonnen!";
                } else if (spielerPunkte < dealerPunkte) {
                    nachricht = "Du hast Verloren!";
                }

                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.WHITE);
                g.drawString(nachricht, 430, 390);
            }
            
            //zeichnet dealerhand //if else weil UI
            if (passenButton.isEnabled()) {
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.WHITE);
                g.drawString("Dealer: ", 25, 358);
                g.drawString("" + (dealerPunkte - umgedrehteKarte.getValue()), 147, 358);
            }else {
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.WHITE);
                g.drawString("Dealer: ", 25, 358);
                g.drawString("" + dealerPunkte, 147, 358);
            }

            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(Color.WHITE);
            g.drawString("Spieler: " + spielerPunkte, 25, 412);

            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(Color.WHITE);
            g.drawString("Highscore: " + highscore, 847, 358);

            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(Color.WHITE);
            g.drawString("Konto: " + konto, 910, 412);

        } catch (Exception e) {
            e.printStackTrace();
        }
}
    };

    JPanel buttonPanel = new JPanel();
    JButton karteZiehenButton = new JButton("Karte Ziehen");
    JButton passenButton = new JButton("Passen");
    JButton verdoppelnButton = new JButton("Verdoppeln");
    JButton teilenButton = new JButton("Teilen");
    JButton neustartButton = new JButton ("Neustart");
    
    BlackJack() {
        startGame();
        musikSpieler.spielHintergrundMusik();
        fensterFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                musikSpieler.stopMusik();
            }
        });
        
        fensterFrame.setVisible(true);
        fensterFrame.setSize(fensterFrameBreite, fensterFrameHoehe);
        fensterFrame.setLocationRelativeTo(null);
        fensterFrame.setResizable(false);
        fensterFrame.setAlwaysOnTop(true);
        fensterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        spielPanel.setLayout(new BorderLayout());
        spielPanel.setBackground(new Color(53, 101, 77)); //r, g, b farben
        fensterFrame.add(spielPanel);

        karteZiehenButton.setFocusable(false);
        buttonPanel.add(karteZiehenButton);
        passenButton.setFocusable(false);
        buttonPanel.add(passenButton);
        verdoppelnButton.setFocusable(false);
        buttonPanel.add(verdoppelnButton);
        teilenButton.setFocusable(false);
        buttonPanel.add(teilenButton);
        neustartButton.setFocusable(false);
        buttonPanel.add(neustartButton);

        fensterFrame.add(buttonPanel, BorderLayout.SOUTH);
    
        karteZiehenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String basisPfad = "src/music/soundeffekt/karte_ziehen/";
                    Random random = new Random();
                    int index = random.nextInt(4) + 1; //zufall 1 bis 4
    
                    String dateiName = basisPfad + "card-place-" + index + ".wav";
                    File musikPfad = new File(dateiName);
        
                    if (musikPfad.exists()){
                        AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioInput);
                        clip.start();
    
                    }else {
                        System.out.println("Karte Ziehen Musik nicht gefunden");
                    }  
    
                } catch (Exception m) {
                    System.out.println("Fehler beim Abspielen der Karte Ziehen Musik: " + m.getMessage());
                }

                Karte karte = stapel.remove(stapel.size() - 1); 
            
                if (hatGeteilt == true) {
                    teilenButton.setEnabled(false);
                    verdoppelnButton.setEnabled(false);
                    
                    if (aktuelleHand == 1) {
                        spielerTeilenHand1.add(karte);
                    } else if (aktuelleHand == 2) {
                        spielerTeilenHand2.add(karte);
                    }

                    if (spielerTeilenHand1.size() == 2 && spielerTeilenHand2.size() == 2) {
                        karteZiehenButton.setEnabled(false);
                        teilenButton.setEnabled(false);
                        verdoppelnButton.setEnabled(false);
                    }
        
                    // Wechsle die Hand nach dem Ziehen einer Karte
                    wechsleHand();
                } else {
                    // Fuege die Karte zur urspruenglichen Hand hinzu
                    spielerHand.add(karte);
                }
        
                spielerPunkte += karte.getValue();
                spielerAssZaehler += karte.isAss() ? 1 : 0;

                //falls mit 2 Ass startet
                if (spielerPunkte == 22 && spielerHand.size() == 2) {
                    spielerPunkte -= 10;
                }
        
                if (reduceSpielerAss() > 21) {
                    karteZiehenButton.setEnabled(false);
                }
                spielPanel.repaint();
            }
        });

        passenButton.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musikSpieler.spielPassenMusik();

                karteZiehenButton.setEnabled(false);
                passenButton.setEnabled(false);
                teilenButton.setEnabled(false);
                verdoppelnButton.setEnabled(false);
              
                while (dealerPunkte < 17) {
                    Karte karte = stapel.remove(stapel.size()-1);
                    dealerPunkte += karte.getValue();
                    dealerAssZaehler += karte.isAss()? 1 : 0;
                    dealerHand.add(karte);
                }
                spielPanel.repaint();
             }
        });

        teilenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musikSpieler.spielTeilenMusik();

                if (spielerHand.size() == 2) {
                    Karte karte1 = spielerHand.get(0);
                    Karte karte2 = spielerHand.get(1);
                    teilenButton.setEnabled(false);
                    verdoppelnButton.setEnabled(false);
            
                    if (karte1.wert.equals(karte2.wert) || karte1.typ.equals(karte2.typ)) {
                        spielerTeilenHand1 = new ArrayList<>();
                        spielerTeilenHand2 = new ArrayList<>();
            
                        spielerTeilenHand1.add(karte1);
                        spielerTeilenHand2.add(karte2);
            
                        //leert die urspruengliche hand
                        spielerHand.clear();
                        hatGeteilt = true;
            
                        System.out.println("Teilen erfolgreich! Du hast jetzt zwei Haende:");
                        System.out.println("Hand 1: " + spielerTeilenHand1);
                        System.out.println("Hand 2: " + spielerTeilenHand2);
                        spielPanel.repaint();
                    }
                }
                spielPanel.repaint();
            }
        });

        verdoppelnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musikSpieler.spielVerdoppelnMusik();

                verdoppelnKonto = konto;
                verdoppelnCount = true;
                konto -= konto;
                verdoppelnButton.setEnabled(false);
                
                spielPanel.repaint();
            }
        });

        neustartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musikSpieler.spielNeustartMusik();

                if(spielerTeilenHand1 != null) {
                    spielerTeilenHand1.clear();
                }else {
                    spielerTeilenHand1 = new ArrayList<>();
                }
        
                if(spielerTeilenHand2 != null) {
                    spielerTeilenHand2.clear();
                }else {
                    spielerTeilenHand2 = new ArrayList<>();
                }
                
                spielerHand.clear();
                spielerTeilenHand1.clear();
                spielerTeilenHand2.clear();
                dealerHand.clear();
                spielerPunkte = 0;
                spielerAssZaehler = 0;
                dealerPunkte = 0;
                dealerAssZaehler = 0;
                hatGeteilt = false;
                aktuelleHand = 1;
            
                karteZiehenButton.setEnabled(true);
                passenButton.setEnabled(true);
                teilenButton.setEnabled(true);
                verdoppelnButton.setEnabled(true);
                
                if (!spielerGewinnt == true && konto <= 0) {
                    konto = 10;
                }          
                
                startGame();
                spielPanel.repaint();
            }
        });
        spielPanel.repaint();
    }

    public void startGame() {
        deckBauen();
        stapelMischen();

        //initialisieren dealer
        dealerHand = new ArrayList<Karte>();
        dealerPunkte = 0;
        dealerAssZaehler = 0;

        //erste karte des dealers (versteckt)
        umgedrehteKarte = stapel.remove(stapel.size()- 1);
        dealerPunkte += umgedrehteKarte.getValue();
        dealerAssZaehler += umgedrehteKarte.isAss() ? 1 : 0;

        //zweite karte des dealers (sichtbar)
        Karte karte = stapel.remove(stapel.size()-1);
        dealerPunkte += karte.getValue();
        dealerAssZaehler += karte.isAss() ? 1 : 0;
        dealerHand.add(karte);

        System.out.println("Dealer:");
        System.out.println(umgedrehteKarte);
        System.out.println(dealerHand);
        System.out.println(dealerPunkte);
        System.out.println(dealerAssZaehler);
        System.out.println(" ");

        //initialisiere die Haende des Spielers
        spielerHand = new ArrayList<>();
        spielerPunkte = 0;
        spielerAssZaehler = 0;
        
        for (int i = 0; i < 2; i++) {
           karte = stapel.remove(stapel.size()-1);
           spielerPunkte += karte.getValue();
           spielerAssZaehler += karte.isAss() ? 1 : 0;
           spielerHand.add(karte);
        }

        //falls mit 2 Ass startet
        if (spielerAssZaehler == 2) {
            spielerPunkte -= 10;
        }

        System.out.println("Spieler:");
        System.out.println(spielerHand);
        System.out.println(spielerPunkte);
        System.out.println(spielerAssZaehler);
        System.out.println(" ");

        spielPanel.repaint();
    }

    public void deckBauen() {
        stapel = new ArrayList<Karte>();
        String[] typen = {"Kreuz", "Pik", "Herz", "Karo"};
        String[] werte = {"Ass", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Bube", "Dame", "Koenig"};

        for (int t = 0; t < typen.length; t++) { 
            for (int w = 0; w < werte.length; w++) { 
                Karte karte = new Karte(werte[w], typen[t]);
                stapel.add(karte);
            }
        }
        // System.out.println("Sortierte Karten:");
        // System.out.println(stapel);
        // System.err.println(" ");
    }

    //mischt den stapel
    public void stapelMischen() {
        for (int i = 0; i < stapel.size(); i++) {
            int j = random.nextInt(stapel.size());
            Karte aktuelleKarte = stapel.get(i);
            Karte randomKarte = stapel.get(j);

            //beide karten werden getauscht
            stapel.set(i, randomKarte);
            stapel.set(j, aktuelleKarte);
        }
        // System.err.println("Nach dem Mischen der Karten:");
        // System.out.println(stapel);
    }

    public int reduceSpielerAss() {
        while (spielerPunkte > 21 && spielerAssZaehler > 0) {
            spielerPunkte -= 10;
            spielerAssZaehler -= 1;
        }
        return spielerPunkte;
    }
    
    public int reduceDealerAss() {
        while (dealerPunkte > 21 && dealerAssZaehler > 0) {
            dealerPunkte -= 10;
            dealerAssZaehler -= 1;
        }
        return dealerPunkte;
    }

    public void wechsleHand() {
        if (aktuelleHand == 2) {
            aktuelleHand = 1; // Wechsle zur zweiten Hand
        } else if (aktuelleHand == 1) {
            aktuelleHand = 2; // Wechsle zur ersten Hand
        }
        System.out.println("Aktive Hand: " + aktuelleHand);
    }

    public void updateKonto(int betrag) {
        konto = Math.max(0, konto + betrag);  //verhindert Konto negativ wird
        kontoLabel.setText("Konto: " + konto);

        if (konto == 0) {
            karteZiehenButton.setEnabled(false);
            passenButton.setEnabled(false);
            teilenButton.setEnabled(false);
            verdoppelnButton.setEnabled(false);
        }
    }    
}

class MusikSpieler {
    private void setzeLautstaerke(Clip clip, float lautstaerkeInDB) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(lautstaerkeInDB);
        } else {
            System.out.println("Lautstärkeregulierung wird nicht unterstützt.");
        }
    }

    private Clip clip;

    public void spielHintergrundMusik(){
        try {
            File musikPfad = new File("src/music/hintergrund/CasinoJazz.wav");
    
            if (musikPfad.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);

                float gewuenschteLautstaerke = -25.0f; //beispiel: -10 dB (leiser)
                setzeLautstaerke(clip, gewuenschteLautstaerke);
            }else {
                System.out.println("Hintergrundmusik nicht gefunden");
            }  

        } catch (Exception e) {
            System.out.println("Fehler beim Abspielen der Hintergrundmusik: " + e.getMessage());
        }
    }

    public void spielKarteZiehenMusik() {
            
    }
        
    public void spielPassenMusik() {
        try {
            String basisPfad = "src/music/soundeffekt/passen/";
            Random random = new Random();
            int index = random.nextInt(5) + 1; //zufall 1 bis 4

            String dateiName = basisPfad + "chips-stack-" + index + ".wav";
            File musikPfad = new File(dateiName);
    
            if (musikPfad.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

            }else {
                System.out.println("Passen-Musik nicht gefunden");
            }  

        } catch (Exception e) {
            System.out.println("Fehler beim Abspielen der Passen-Musik: " + e.getMessage());
        }
    }

    public void spielTeilenMusik() {
        try {
            String basisPfad = "src/music/soundeffekt/teilen/";
            Random random = new Random();
            int index = random.nextInt(8) + 1; //zufall 1 bis 4

            String dateiName = basisPfad + "card-slide-" + index + ".wav";
            File musikPfad = new File(dateiName);

            if (musikPfad.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

            }else {
                System.out.println("Teilen-Musik nicht gefunden");
            }  

        } catch (Exception e) {
            System.out.println("Fehler beim Abspielen der Teilen-Musik: " + e.getMessage());
        }
    }

    public void spielVerdoppelnMusik() {
        try {
            String basisPfad = "src/music/soundeffekt/verdoppeln/";
            Random random = new Random();
            int index = random.nextInt(5) + 1; //zufall 1 bis 4

            String dateiName = basisPfad + "chips-handle-" + index + ".wav";
            File musikPfad = new File(dateiName);

            if (musikPfad.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

            }else {
                System.out.println("Verdoppeln-Musik nicht gefunden");
            }  

        } catch (Exception e) {
            System.out.println("Fehler beim Abspielen der Verdoppeln-Musik: " + e.getMessage());
        }
    }
    

    public void spielNeustartMusik() {
        try {
            String basisPfad = "src/music/soundeffekt/neustart/";
            Random random = new Random();
            int index = random.nextInt(4) + 1; //zufall 1 bis 4

            String dateiName = basisPfad + "card-shove-" + index + ".wav";
            File musikPfad = new File(dateiName);

            if (musikPfad.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musikPfad);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

            }else {
                System.out.println("Neustart-Musik nicht gefunden");
            }  

        } catch (Exception e) {
            System.out.println("Fehler beim Abspielen der Neustart-Musik: " + e.getMessage());
        }
    }

    public void stopMusik() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
 
