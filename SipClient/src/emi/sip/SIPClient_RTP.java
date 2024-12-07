package emi.sip;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SIPClient_RTP extends JFrame {

    private JTextField textFieldLocalSIP;
    private JTextField textFieldDestinationSIP;
    private JTextArea textAreaMsgRecu;
    private JTextArea textAreaMsgSent;
    private JButton buttonAppeler;
    private JButton buttonRaccrocher;

    private SipClient sipClient;
    private TP_RTP rtpClient; // Intégrer le client RTP ici

    private int findAvailablePort() {
        int port = 5004; // Start from the default RTP port
        while (port <= 5100) { // Check ports in a range (5004-5100)
            try (java.net.DatagramSocket socket = new java.net.DatagramSocket(port)) {
                socket.close();
                return port; // Port is free, return it
            } catch (java.io.IOException ex) {
                port++; // Port is in use, try the next one
            }
        }
        throw new RuntimeException("No available ports found for RTP session.");
    }


    public SIPClient_RTP() {
        // Configuration de la fenêtre principale
        setTitle("SIP Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(null);

        // Initialisation des composants graphiques
        JLabel labelLocalSIP = new JLabel("Adresse SIP locale:");
        labelLocalSIP.setBounds(20, 20, 150, 25);
        add(labelLocalSIP);

        // Récupérer l'adresse IP locale et l'afficher avec un port d'écoute
        String localAddress = getLocalIPAddress();
        String localSIP = "sip:" + localAddress + ":6060"; // Port d'écoute 6060
        textFieldLocalSIP = new JTextField(localSIP);
        textFieldLocalSIP.setBounds(180, 20, 380, 25);
        textFieldLocalSIP.setEditable(false);
        add(textFieldLocalSIP);

        JLabel labelDestinationSIP = new JLabel("Adresse SIP destination:");
        labelDestinationSIP.setBounds(20, 60, 150, 25);
        add(labelDestinationSIP);

        textFieldDestinationSIP = new JTextField();
        textFieldDestinationSIP.setBounds(180, 60, 380, 25);
        add(textFieldDestinationSIP);

        JLabel labelMsgRecu = new JLabel("Messages SIP reçus:");
        labelMsgRecu.setBounds(20, 100, 150, 25);
        add(labelMsgRecu);

        textAreaMsgRecu = new JTextArea();
        JScrollPane scrollPaneRecu = new JScrollPane(textAreaMsgRecu);
        scrollPaneRecu.setBounds(20, 130, 260, 150);
        add(scrollPaneRecu);

        JLabel labelMsgSent = new JLabel("Messages SIP envoyés:");
        labelMsgSent.setBounds(310, 100, 150, 25);
        add(labelMsgSent);

        textAreaMsgSent = new JTextArea();
        JScrollPane scrollPaneSent = new JScrollPane(textAreaMsgSent);
        scrollPaneSent.setBounds(310, 130, 260, 150);
        add(scrollPaneSent);

        // Bouton "Appeler"
        buttonAppeler = new JButton("Appeler");
        buttonAppeler.setBounds(150, 300, 120, 30);
        add(buttonAppeler);

        // Bouton "Raccrocher"
        buttonRaccrocher = new JButton("Raccrocher");
        buttonRaccrocher.setBounds(330, 300, 120, 30);
        add(buttonRaccrocher);

        // Initialisation de l'objet SipClient
        sipClient = new SipClient();
        rtpClient = new TP_RTP(); // Créer l'instance de RTP

        // Relier les messages reçus et envoyés SIP avec les zones de texte
        sipClient.setUiComponents(textAreaMsgRecu, textAreaMsgSent);

        // Démarrer la pile SIP
        System.out.println("Initialisation de la pile SIP via onOpen...");
        sipClient.onOpen();

        // Gestion du bouton "Appeler"
        buttonAppeler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String destinationSIP = textFieldDestinationSIP.getText().trim();
                if (destinationSIP.isEmpty()) {
                    textAreaMsgSent.append("Erreur: Adresse SIP de destination vide.\n");
                    return;
                }
                try {
                    // Extract the IP address from the SIP URI (e.g., "sip:10.72.154.17:6060")
                    String destinationIP = destinationSIP.split(":")[1].split(";")[0];

                    // Start the SIP INVITE process
                    sipClient.onInvite(destinationSIP);

                    // Dynamically find a free local port for RTP
                    int localPort = findAvailablePort();
                    int remotePort = 5004; // Assume the remote port for RTP is 5004
                    int audioFormat = 5; // ULAW format

                    textAreaMsgSent.append("Initialisation de la session RTP...\n");
                    textAreaMsgSent.append("Port local RTP: " + localPort + "\n");

                    // Start the RTP session
                    rtpClient.demarrerSession(destinationIP, remotePort, localPort, audioFormat);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    textAreaMsgSent.append("Erreur lors de l'appel : " + ex.getMessage() + "\n");
                }
            }
        });



        // Gestion du bouton "Raccrocher"
        buttonRaccrocher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Terminer l'appel et fermer la session RTP
                sipClient.onBye(e);
                rtpClient.arreterSession(); // Arrêter la session RTP
            }
        });

        // Rendre la fenêtre visible
        setVisible(true);
    }

    // Méthode pour obtenir l'adresse IP locale de la machine
    private String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "localhost"; // Retourne localhost si l'adresse IP ne peut pas être déterminée
        }
    }

    public static void main(String[] args) {
        // Lancer l'interface utilisateur
        new SIPClient_RTP();
    }
}
