package emi.sip;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.*;
import javax.swing.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.net.*;
import javax.sip.Dialog;
import javax.sdp.*;
import javax.sip.ClientTransaction;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.header.CSeqHeader;



public class SipClient extends JFrame implements SipListener {

    // SIP-related objects
    private SipFactory sipFactory;
    private SipStack sipStack;
    private SipProvider sipProvider;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private ListeningPoint listeningPoint;
    private Properties properties;

    private String transport;

    // Local configuration
    private String ip;
    private int port = 6060;
    private String protocol = "tcp";
    private int tag = new Random().nextInt();
    private Address contactAddress;
    private ContactHeader contactHeader;

    private SdpFactory sdpFactory;
    private Dialog dialogClient;

    // UI components
    private javax.swing.JButton buttonBye;
    private javax.swing.JButton buttonInvite;
    private javax.swing.JButton buttonRegisterStatefull;
    private javax.swing.JButton buttonRegisterStateless;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;
    private javax.swing.JTextField textField;

    private JTextArea textAreaMsgRecu;
    private JTextArea textAreaMsgSent;

    /**
     * Constructor to initialize the SIP Client
     */
    public SipClient() {
        initComponents();
    }

    private void initComponents() {
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        buttonRegisterStateless = new javax.swing.JButton();
        buttonRegisterStatefull = new javax.swing.JButton();
        buttonInvite = new javax.swing.JButton();
        buttonBye = new javax.swing.JButton();
        textField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SIP Client");
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                onOpen();
            }
        });

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        buttonRegisterStateless.setText("Reg (SL)");
        buttonRegisterStateless.setEnabled(true);
        buttonRegisterStateless.addActionListener(evt -> onRegisterStateless(evt));

        buttonRegisterStatefull.setText("Reg (SF)");
        buttonRegisterStatefull.setEnabled(true);
        buttonRegisterStatefull.addActionListener(evt -> onRegisterStatefull(evt));


        // buttonInvite.setText("Invite");
        // buttonInvite.setEnabled(true);
        //buttonInvite.addActionListener(evt -> onInvite(evt));

        //buttonBye.setText("Bye");
        //buttonBye.setEnabled(true);
        buttonBye.addActionListener(evt -> onBye(evt));
        textField.setText("sip:alice@localhost:5060");


        // Layout settings
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonRegisterStateless, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonRegisterStatefull, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonInvite, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonBye, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 2, Short.MAX_VALUE))
                                        .addComponent(textField))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonRegisterStateless)
                                        .addComponent(buttonInvite)
                                        .addComponent(buttonBye)
                                        .addComponent(buttonRegisterStatefull))
                                .addContainerGap())
        );

        pack();
    }
    public void setUiComponents(JTextArea textAreaMsgRecu, JTextArea textAreaMsgSent) {
        this.textAreaMsgRecu = textAreaMsgRecu;
        this.textAreaMsgSent = textAreaMsgSent;
    }
    public void onOpen() {
        try {
            System.out.println("Initialisation de la pile SIP...");

            // Initialisation des valeurs
            ip = InetAddress.getLocalHost().getHostAddress();
            port = 6060; // Définir un port par défaut
            transport = "udp"; // Ou "tcp", selon votre besoin

            System.out.println("IP locale : " + ip);
            System.out.println("Port : " + port);
            System.out.println("Transport : " + transport);

            // Création de SipFactory
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            System.out.println("SipFactory initialisée.");

            // Configuration des propriétés
            properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "stack");
            properties.setProperty("javax.sip.IP_ADDRESS", ip);
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sip_debug_log.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "sip_server_log.txt");

            // Création de la pile SIP
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("Pile SIP créée.");

            // Création des factories SIP
            messageFactory = sipFactory.createMessageFactory();
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            System.out.println("Factories SIP créées.");

            // Création du ListeningPoint et du SipProvider
            listeningPoint = sipStack.createListeningPoint(ip, port, transport);
            sipProvider = sipStack.createSipProvider(listeningPoint);
            sipProvider.addSipListener(this);
            System.out.println("ListeningPoint et SipProvider configurés.");

            // Création de l'adresse de contact
            contactAddress = addressFactory.createAddress("sip:" + ip + ":" + port);
            contactHeader = headerFactory.createContactHeader(contactAddress);
            System.out.println("Adresse de contact créée : " + contactAddress);
            if (contactHeader == null) {
                throw new IllegalStateException("ContactHeader n'a pas pu être initialisé.");
            }
            System.out.println("ContactHeader initialisé : " + contactHeader);


            System.out.println("Pile SIP initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la pile SIP : " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
    private void onRegisterStateless(java.awt.event.ActionEvent evt) {
        try {
            Address addressTo = addressFactory.createAddress(textField.getText());
            javax.sip.address.URI requestURI = addressTo.getURI();

            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = headerFactory.createViaHeader(ip, port, protocol, null);
            viaHeaders.add(viaHeader);

            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER);
            FromHeader fromHeader = headerFactory.createFromHeader(contactAddress, String.valueOf(tag));
            ToHeader toHeader = headerFactory.createToHeader(addressTo, null);

            Request request = messageFactory.createRequest(
                    requestURI,
                    Request.REGISTER,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            request.addHeader(contactHeader);
            sipProvider.sendRequest(request);

            textArea.append("Request sent:\n" + request.toString() + "\n\n");
        } catch (Exception e) {
            textArea.append("Request failed: " + e.getMessage() + "\n");
        }
    }

    private void onRegisterStatefull(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRegisterStatefull
        try {
            // Obtenir l’adresse de destination à partir du champ texte
            Address addressTo = this.addressFactory.createAddress(this.textField.getText());
            URI requestURI = addressTo.getURI();

            // Créer les entêtes nécessaires pour la requête REGISTER
            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, this.protocol, null);
            viaHeaders.add(viaHeader);

            MaxForwardsHeader maxForwards = this.headerFactory.createMaxForwardsHeader(70);
            CallIdHeader callId = this.sipProvider.getNewCallId();
            CSeqHeader cSeq = this.headerFactory.createCSeqHeader(1L, Request.REGISTER);
            FromHeader from = this.headerFactory.createFromHeader(this.contactAddress, String.valueOf(this.tag));
            ToHeader to = this.headerFactory.createToHeader(addressTo, null);

            // Créer la requête REGISTER
            Request request = this.messageFactory.createRequest(
                    requestURI,
                    Request.REGISTER,
                    callId,
                    cSeq,
                    from,
                    to,
                    viaHeaders,
                    maxForwards
            );

            // Ajouter l'entête "Contact" à la requête
            request.addHeader(contactHeader);

            // Commentez cette ligne pour éviter un envoi en mode Stateless
            // sipProvider.sendRequest(request);

            // Créer une nouvelle transaction cliente SIP
            ClientTransaction transaction = this.sipProvider.getNewClientTransaction(request);

            // Envoyer la requête via la transaction stateful
            transaction.sendRequest();

            // Afficher le message dans le champ texte
            this.textArea.append("Stateful REGISTER request sent:\n" + request.toString() + "\n");
        } catch (Exception e) {
            // Afficher l'erreur en cas de problème
            this.textArea.append("Failed to send stateful REGISTER request: " + e.getMessage() + "\n");
        }
    }//GEN-LAST:event_onRegisterStatefull

    public void onInvite(String destinationSIP) {
        try {
            // Extraction de l'adresse IP depuis l'URI SIP
            String ipAddress = destinationSIP.split(":")[1].split(";")[0]; // "sip:10.72.154.17:6060" -> "10.72.154.17"

            // Validation des paramètres nécessaires
            if (this.ip == null || this.port == 0 || this.transport == null) {
                throw new IllegalStateException("Impossible d'envoyer l'invitation : IP, port ou transport non initialisé.");
            }

            System.out.println("Valeurs pour createViaHeader :");
            System.out.println("IP : " + this.ip);
            System.out.println("Port : " + this.port);
            System.out.println("Transport : " + this.transport);

            // Étape 1 : Création de l'adresse SIP
            Address addressTo = this.addressFactory.createAddress("sip:" + ipAddress + ":6060");  // Utilisation de l'adresse IP extraite
            System.out.println("Adresse SIP créée : " + addressTo);

            // Étape 2 : Création des en-têtes SIP
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, null);
            javax.sip.address.URI requestURI = addressTo.getURI();
            System.out.println("URI de la requête : " + requestURI);

            // Étape 3 : Préparation des en-têtes INVITE
            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, this.transport, null);
            viaHeaders.add(viaHeader);

            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, Request.INVITE);
            MaxForwardsHeader maxForwards = this.headerFactory.createMaxForwardsHeader(70);
            FromHeader fromHeader = this.headerFactory.createFromHeader(this.contactAddress, String.valueOf(this.tag));

            // Étape 4 : Création de la requête INVITE
            Request request = this.messageFactory.createRequest(
                    requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards
            );
            System.out.println("Requête INVITE créée : \n" + request);
            if (this.contactHeader == null) {
                throw new IllegalStateException("ContactHeader non initialisé dans onInvite.");
            }
            request.addHeader(this.contactHeader);
            System.out.println("En-tête Contact ajouté : " + this.contactHeader);

            // Étape 5 : Ajout du SDP
            String sdpData = createSDPData(50002, 22222); // Ports pour audio et vidéo
            ContentTypeHeader contentTypeHeader = this.headerFactory.createContentTypeHeader("application", "sdp");
            request.setContent(sdpData, contentTypeHeader);
            System.out.println("SDP ajouté à la requête.");

            // Étape 6 : Création de la transaction INVITE
            ClientTransaction inviteTid = this.sipProvider.getNewClientTransaction(request);

            // Étape 7 : Envoi de la requête INVITE
            inviteTid.sendRequest();
            System.out.println("Requête INVITE envoyée avec succès.");
            if (textAreaMsgSent != null) {
                textAreaMsgSent.append("Requête INVITE envoyée :\n" + request.toString() + "\n");
            }
        } catch (IllegalStateException e) {
            System.err.println("Erreur dans onInvite : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur inattendue dans onInvite : " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void onBye(java.awt.event.ActionEvent evt) {
        try {
            if (dialogClient == null) {
                textArea.append("No active dialog to send BYE.\n");
                return;
            }

            // Créer une requête BYE
            Request byeRequest = dialogClient.createRequest(Request.BYE);
            ClientTransaction byeTransaction = sipProvider.getNewClientTransaction(byeRequest);
            dialogClient.sendRequest(byeTransaction);
            textArea.append("BYE request sent.\n");
        } catch (Exception e) {
            textArea.append("Error sending BYE: " + e.getMessage() + "\n");
        }
    }

    public String createSDPData(int localBasePort, int remoteBasePort) {
        try {
            sdpFactory = SdpFactory.getInstance();
            SessionDescription sessDescr = sdpFactory.createSessionDescription();
            String myIPAddr = InetAddress.getLocalHost().getHostAddress();

            // "v=0"
            Version v = sdpFactory.createVersion(0);
            // "o=" (origin)
            Origin o = sdpFactory.createOrigin("1234", 0, 0, "IN", "IP4", myIPAddr);
            // "s=-" (session name)
            SessionName s = sdpFactory.createSessionName("-");
            // "c=" (connection)
            Connection c = sdpFactory.createConnection("IN", "IP4", myIPAddr);
            // "t=0 0" (timing)
            TimeDescription t = sdpFactory.createTimeDescription();
            Vector<TimeDescription> timeDescs = new Vector<>();
            timeDescs.add(t);

            // Description des médias (audio)
            String[] audioFormats = {"0", "4", "18"}; // Formats audio pour RTP
            MediaDescription am = sdpFactory.createMediaDescription("audio", localBasePort, 1, "RTP/AVP", audioFormats);

            // Description des médias (vidéo)
            String[] videoFormats = {"34"}; // Formats vidéo pour RTP
            MediaDescription vm = sdpFactory.createMediaDescription("video", remoteBasePort, 1, "RTP/AVP", videoFormats);

            // Ajouter les descriptions de médias
            Vector<MediaDescription> mediaDescs = new Vector<>();
            mediaDescs.add(am);
            mediaDescs.add(vm);

            // Configurer la session
            sessDescr.setVersion(v);
            sessDescr.setOrigin(o);
            sessDescr.setConnection(c);
            sessDescr.setSessionName(s);
            sessDescr.setTimeDescriptions(timeDescs);
            sessDescr.setMediaDescriptions(mediaDescs);

            return sessDescr.toString();
        } catch (SdpException | UnknownHostException exc) {
            System.out.println("Erreur lors de la génération du SDP.");
            exc.printStackTrace();
        }

        return "No SDP set";
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            String method = request.getMethod();

            System.out.println("Requête reçue : " + method);
            if (textAreaMsgRecu != null) {
                textAreaMsgRecu.append("Requête reçue :\n" + request.toString() + "\n");
            }

            if (method.equals(Request.INVITE)) {
                // Envoyer une réponse 180 Ringing
                Response ringingResponse = messageFactory.createResponse(Response.RINGING, request);

                // Vérifier si une transaction serveur existe, sinon en créer une
                ServerTransaction serverTransaction = requestEvent.getServerTransaction();
                if (serverTransaction == null) {
                    System.out.println("Création d'une nouvelle transaction serveur pour INVITE...");
                    serverTransaction = sipProvider.getNewServerTransaction(request);
                }

                serverTransaction.sendResponse(ringingResponse);
                System.out.println("180 Ringing envoyé pour la requête INVITE.");

                // Envoyer une réponse 200 OK pour accepter l'appel
                Response okResponse = messageFactory.createResponse(Response.OK, request);
                Address localContactAddress = addressFactory.createAddress("sip:" + ip + ":" + port);
                ContactHeader localContactHeader = headerFactory.createContactHeader(localContactAddress);
                okResponse.addHeader(localContactHeader);

                serverTransaction.sendResponse(okResponse);
                System.out.println("200 OK envoyé pour la requête INVITE.");

                // Stocker le dialogue pour de futures interactions
                this.dialogClient = serverTransaction.getDialog();
            } else if (method.equals(Request.BYE)) {
                // Gérer les requêtes BYE pour terminer la session
                Response okResponse = messageFactory.createResponse(Response.OK, request);

                ServerTransaction serverTransaction = requestEvent.getServerTransaction();
                if (serverTransaction == null) {
                    System.out.println("Création d'une nouvelle transaction serveur pour BYE...");
                    serverTransaction = sipProvider.getNewServerTransaction(request);
                }

                serverTransaction.sendResponse(okResponse);
                System.out.println("200 OK envoyé pour la requête BYE.");

                // Réinitialiser le dialogue
                this.dialogClient = null;
            } else {
                System.out.println("Requête non gérée reçue : " + request.toString());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de la requête : " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Override
    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            int statusCode = response.getStatusCode();

            if (statusCode == Response.OK) {
                // Gestion des réponses INVITE
                CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                if (cSeqHeader != null && cSeqHeader.getMethod().equals(Request.INVITE)) {
                    // Créer un ACK pour confirmer l'appel
                    Request ackRequest = dialogClient.createAck(cSeqHeader.getSeqNumber());
                    dialogClient.sendAck(ackRequest);
                    System.out.println("ACK envoyé pour INVITE.");

                    // Enregistrer le dialogue pour pouvoir envoyer BYE plus tard
                    this.dialogClient = dialogClient;
                }
            }
        } catch (Exception e) {
            textArea.append("Erreur lors de l'envoi de l'ACK : " + e.getMessage() + "\n");
        }
    }


    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        textArea.append("Timeout occurred.\n");
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        textArea.append("IO Exception occurred.\n");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        textArea.append("Transaction terminated.\n");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        textArea.append("Dialog terminated.\n");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new SipClient().setVisible(true));
    }
}