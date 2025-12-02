import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;


import keyboardinput.Keyboard;


/**
 * La classe MainTest rappresenta il Client dell'applicazione di Data Mining
 * (probabilmente basata sull'algoritmo QTMiner).
 * È responsabile della connessione con il server (MultiServer) tramite socket,
 * della gestione del menu delle operazioni e della gestione del protocollo
 * di comunicazione per lo scambio di oggetti serializzati.
 */
public class MainTest {

    /**
     * Stream di output utilizzato per inviare oggetti (richieste, parametri) al server.
     */
	private ObjectOutputStream out;

    /**
     * Stream di input utilizzato per ricevere oggetti (risposte, risultati) dal server.
     */
	private ObjectInputStream in ; // stream con richieste del client

    /**
     * Costruttore della classe MainTest.
     * Inizializza la connessione con il server all'indirizzo IP e porta specificati
     * e configura gli stream ObjectOutputStream e ObjectInputStream per la comunicazione.
     * @param ip L'indirizzo IP del server a cui connettersi.
     * @param port La porta del server a cui connettersi.
     * @throws IOException Se si verifica un errore I/O durante la creazione del socket o degli stream.
     */
	public MainTest(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip); //ip
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port); //Port
		System.out.println(socket);
		
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());	; // stream con richieste del client
	}

    /**
     * Visualizza un menu all'utente e legge la scelta dell'operazione da eseguire.
     * Le opzioni sono: (1) Carica cluster da file o (2) Carica dati dal database.
     * @return La scelta dell'utente (1 o 2).
     */
	private int menu(){
		int answer;
		
		do{
			System.out.println("(1) Load clusters from file");
			System.out.println("(2) Load data from db");
			System.out.print("(1/2):");
			answer=Keyboard.readInt();
		}
		while(answer<=0 || answer>2);
		return answer;
		
	}

    /**
     * Implementa l'operazione di clustering leggendo una tabella e un raggio R da input.
     * Invia il codice operativo (3), il nome della tabella e il raggio al server.
     * Il client attende la ricezione del risultato del clustering sotto forma di stringa.
     * @return La stringa che rappresenta il risultato del clustering (K-Means/QT).
     * @throws SocketException Se si verifica un errore durante la comunicazione socket.
     * @throws ServerException Se il server risponde con un messaggio di errore ('KO').
     * @throws IOException Se si verifica un errore I/O.
     * @throws ClassNotFoundException Se la classe dell'oggetto ricevuto non è trovata.
     */
	private String learningFromFile() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(3);
		
		System.out.print("File Name:");
		String FileName=Keyboard.readString();
		out.writeObject(FileName);

		String result = (String)in.readObject();
		if(result.equals("OK"))
			return (String)in.readObject();
		else throw new ServerException(result);
		
	}

    /**
     * Implementa l'operazione di caricamento di una tabella dal database nel server.
     * Invia il codice operativo (0) e il nome della tabella al server.
     * Il client attende una conferma ('OK') o un messaggio di errore.
     * @throws SocketException Se si verifica un errore durante la comunicazione socket.
     * @throws ServerException Se il server risponde con un messaggio di errore ('KO').
     * @throws IOException Se si verifica un errore I/O.
     * @throws ClassNotFoundException Se la classe dell'oggetto ricevuto non è trovata.
     */
    private void storeTableFromDb() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(0);
		System.out.print("Table name:");
		String tabName=Keyboard.readString();
		out.writeObject(tabName);
		String result = (String)in.readObject();
		if(!result.equals("OK"))
			throw new ServerException(result);
		
	}

    /**
     * Implementa l'operazione di esecuzione del clustering sui dati precedentemente caricati
     * dal database nel server. Invia il codice operativo (1) e il raggio R al server.
     * Attende come risposta l'OK, il numero di cluster trovati e il set di cluster finale.
     * @return La stringa che rappresenta il set di cluster risultante.
     * @throws SocketException Se si verifica un errore durante la comunicazione socket.
     * @throws ServerException Se il server risponde con un messaggio di errore ('KO').
     * @throws IOException Se si verifica un errore I/O.
     * @throws ClassNotFoundException Se la classe dell'oggetto ricevuto non è trovata.
     */
    private String learningFromDbTable() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(1);
		double r=1.0;
		do{
			System.out.print("Radius:");
			r=Keyboard.readDouble();
		} while(r<=0);
		out.writeObject(r);
		String result = (String)in.readObject();
		if(result.equals("OK")){
			System.out.println("Number of Clusters:"+in.readObject());
			return (String)in.readObject();
		}
		else throw new ServerException(result);
		
		
	}

    /**
     * Implementa l'operazione di salvataggio dell'ultimo set di cluster calcolato in un file
     * sul lato server. Invia il codice operativo (2) al server.
     * @throws SocketException Se si verifica un errore durante la comunicazione socket.
     * @throws ServerException Se il server risponde con un messaggio di errore ('KO').
     * @throws IOException Se si verifica un errore I/O.
     * @throws ClassNotFoundException Se la classe dell'oggetto ricevuto non è trovata.
     */
    private void storeClusterInFile() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(2);

        System.out.print("File Name:");
        out.writeObject(Keyboard.readString());

		String result = (String)in.readObject();
		if(!result.equals("OK"))
			 throw new ServerException(result);
		
	}

    /**
     * Metodo principale (entry point) dell'applicazione client.
     * Gestisce gli argomenti della riga di comando (IP e Porta), inizializza la connessione
     * e gestisce il ciclo principale delle operazioni del menu, comprese le relative eccezioni.
     * @param args Argomenti della riga di comando: args[0] è l'IP del server, args[1] è la Porta.
     */
    public static void main(String[] args) {
		String ip=args[0];
		int port = Integer.parseInt(args[1]);
		MainTest main=null;
		try{
			main=new MainTest(ip,port);
		}
		catch (IOException e){
			System.out.println(e);
			return;
		}
		
		
		do{
			int menuAnswer=main.menu();
			switch(menuAnswer)
			{
				case 1:
					try {
						String kmeans=main.learningFromFile();
						System.out.println(kmeans);
					}
					catch (SocketException e) {
						System.out.println(e);
						return;
					}
					catch (FileNotFoundException e) {
						System.out.println(e);
						return ;
					} catch (IOException e) {
						System.out.println(e);
						return;
					} catch (ClassNotFoundException e) {
						System.out.println(e);
						return;
					}
					catch (ServerException e) {
						System.out.println(e.getMessage());
					}
					break;
				case 2: // learning from db
				
					while(true){
						try{
							main.storeTableFromDb();
							break; //esce fuori dal while
						}
						
						catch (SocketException e) {
							System.out.println(e);
							return;
						}
						catch (FileNotFoundException e) {
							System.out.println(e);
							return;
							
						} catch (IOException e) {
							System.out.println(e);
							return;
						} catch (ClassNotFoundException e) {
							System.out.println(e);
							return;
						}
						catch (ServerException e) {
							System.out.println(e.getMessage());
						}
					} //end while [viene fuori dal while con un db (in alternativa il programma termina)
						
					char answer='y';//itera per learning al variare di k
					do{
						try
						{
							String clusterSet=main.learningFromDbTable();
							System.out.println(clusterSet);
							
							main.storeClusterInFile();
									
						}
						catch (SocketException e) {
							System.out.println(e);
							return;
						}
						catch (FileNotFoundException e) {
							System.out.println(e);
							return;
						} 
						catch (ClassNotFoundException e) {
							System.out.println(e);
							return;
						}catch (IOException e) {
							System.out.println(e);
							return;
						}
						catch (ServerException e) {
							System.out.println(e.getMessage());
						}
						System.out.print("Would you repeat?(y/n)");
						answer=Keyboard.readChar();
					}
					while(Character.toLowerCase(answer)=='y');
					break; //fine case 2
					default:
					System.out.println("Invalid option!");
			}
			
			System.out.print("would you choose a new operation from menu?(y/n)");
			if(Keyboard.readChar()!='y')
				break;
			}
		while(true);
		}
}



