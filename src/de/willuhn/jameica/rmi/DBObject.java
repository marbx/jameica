/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/DBObject.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/11/21 02:10:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface fuer alle Business-Objekte.
 * Erweitert java.rmi.Remote. Somit sind diese RMI-faehig. 
 * @author willuhn
 */
public interface DBObject extends Remote
{

  /**
   * Damit kann man manuell eine Transaktion starten.
   * Normalerweise wir bei store() oder delete() sofort
   * bei Erfolg ein commit gemacht. Wenn man aber von
   * aussen das Transaktionsverhalten beeinflussen will,
   * kann man diese Methode aufruefen. Hat man dies
   * getan, werden store() und delete() erst dann in
   * der Datenbank ausgefuehrt, wenn man anschliessend
   * transactionCommit() aufruft.
   * @throws RemoteException
   */
  public void transactionBegin() throws RemoteException;
  
  /**
   * Beendet eine manuell gestartete Transaktion.
   * Wenn vorher kein transactionBegin aufgerufen wurde,
   * wird dieser Aufruf ignoriert.
   * @throws RemoteException
   */
  public void transactionCommit() throws RemoteException;

  /**
   * Rollt die angefangene Transaktion manuell zurueck.
   * @throws RemoteException
   */
  public void transactionRollback() throws RemoteException;

	/**
	 * Speichert das Objekt in der Datenbank. Wenn es eine ID besitzt, wird
	 * der existierende Datensatz aktualisiert, andernfalls hinzugefuegt.
	 * @throws RemoteException
	 */
	public void store() throws RemoteException;

	/**
	 * Loescht das Objekt aus der Datenbank.
	 * @throws RemoteException
	 */
	public void delete() throws RemoteException;

	/**
	 * Liefert die ID des Objektes oder null bei neuen Objekten.
	 * @return
	 * @throws RemoteException
	 */
	public String getID() throws RemoteException;
	
  /**
   * Liefert den Wert des angegebenen Feldes.
   * @param name Name des Feldes.
   * @return Wert des Feldes.
   * @throws RemoteException
   */
  public Object getField(String name) throws RemoteException;

	/**
	 * Gibt an, ob das Objekt neu ist und somit 
	 * ein Insert statt einem Update gemacht werden muss.
	 * @return
	 * @throws RemoteException
	 */
	public boolean isNewObject() throws RemoteException;


  /**
   * Liefert eine Liste mit allen Objekten dieser Tabelle.
   * @return
   * @throws RemoteException
   */
  public DBIterator getList() throws RemoteException;
  
  /**
   * Liefert den Namen des Primaer-Feldes dieses Objektes.
   * Hintergrund: Wenn man z.Bsp. in einer Select-Box nur einen Wert
   * anzeigen kann, dann wird dieser genommen.
   * @return
   * @throws RemoteException
   */
  public String getPrimaryField() throws RemoteException;

}

/*********************************************************************
 * $Log: DBObject.java,v $
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.2  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/10/29 20:56:49  willuhn
 * @N added transactionRollback
 *
 * Revision 1.5  2003/10/29 15:18:16  willuhn
 * @N added transactionBegin() and transactionCommit()
 *
 * Revision 1.4  2003/10/29 11:33:22  andre
 * @N isNewObject
 *
 * Revision 1.3  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 * Revision 1.2  2003/10/26 17:46:30  willuhn
 * @N DBObject
 *
 * Revision 1.1  2003/10/25 19:49:47  willuhn
 * *** empty log message ***
 *
 **********************************************************************/