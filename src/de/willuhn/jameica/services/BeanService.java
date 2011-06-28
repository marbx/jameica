/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/BeanService.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/28 12:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Injector;
import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Session;

/**
 * Ein Service, ueber den Beans instanziiert werden.
 * Er unterstuetzt die Annoations {@link Lifecycle}, {@link Resource}, {@link PostConstruct} und {@link PreDestroy}.
 * 
 * Ist die Bean mit der Annotation {@link Lifecycle} versehen, gilt:
 * 
 *   - {@link Type#CONTEXT}: Die Bean wird nur einmal instanziiert und existiert
 *                           ueber die gesamte Lebensdauer des BeanService - also
 *                           ueber die gesamte Laufzeit der Jameica-Instanz
 *   - {@link Type#REQUEST}: Die Bean besitzt keinen Lifecycle - mit jedem Aufruf
 *                           wird eine neue Instanz erzeugt.
 *   - {@link Type#SESSION}: Die Bean besitzt eine Lebensdauer von 30 Minuten.
 *   
 * Ist eine Member-Variable oder Methode mit der Annotation {@link Resource} versehen,
 * wird sie ueber den BeanService bei der Instanziierung der Bean aufgeloest.
 * 
 * Enthaelt die Bean die Annotation {@link PostConstruct}, wird die zugehoerige
 * Methode bei der Instanziierung aufgerufen.
 * 
 * Die Annotation {@link PreDestroy} wird nur bei Beans mit CONTEXT-Lifecycle
 * beruecksichtigt. Der Aufruf der mit dieser Annotation versehenen Funktion erfolgt
 * beim Beenden von Jameica.
 */
public class BeanService implements Bootable
{
  private Map<Class,Object> contextScope = new HashMap<Class,Object>();
  private Stack contextOrder             = new Stack();
  private Session sessionScope           = new Session();

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    // Nicht noetig - wir machen alles on-demand.
  }
  
  /**
   * Liefert eine Instanz der angegebenen Bean.
   * @param <T> Typ der Bean.
   * @param type Typ der Bean.
   * @return die Instanz der Bean.
   * Wenn die Bean mit der {@link Lifecycle} Annotation versehen ist, wird
   * diese beruecksichtigt.
   * @throws ApplicationException
   */
  public <T> T get(Class<T> type) throws ApplicationException
  {
    if (type == null)
      return null;
    
    String name = type.getSimpleName();
    Logger.debug("searching for bean " + name);
    
    T bean = null;
    
    // 1. Checken, ob wir sie im Context-Scope haben
    bean = (T) contextScope.get(type);
    if (bean != null)
    {
      Logger.debug("  found in context scope");
      return bean;
    }
    
    // 2. Checken, ob wir sie im Session-Scope haben
    bean = (T) sessionScope.get(type);
    if (bean != null)
    {
      Logger.debug("  found in session scope");
      return bean;
    }

    try
    {
      // 3. Bean erzeugen
      Logger.debug("  creating new");
      bean = type.newInstance();

      // Lifecycle ermitteln
      Lifecycle lc = (Lifecycle) type.getAnnotation(Lifecycle.class);
      Lifecycle.Type lct = lc != null ? lc.value() : null;

      if (lct == null)
      {
        Logger.debug("  no lifecycle -> request scope");
      }
      else if (lct == Type.REQUEST)
      {
        Logger.debug("  request scope");
      }
      else if (lct == Type.CONTEXT)
      {
        Logger.debug("  context scope");
        contextScope.put(type,bean);
        contextOrder.add(bean);
      }
      else if (lct == Type.SESSION)
      {
        Logger.debug("  session scope");
        sessionScope.put(type,bean);
      }
      else
      {
        Logger.debug("  unknown scope");
      }
      
      // Abhaengigkeiten aufloesen
      // Das duerfen wir erst machen, NACHDEM wir sie registriert haben
      // Andernfalls koennte man durch zirkulaere Abhaengigkeit eine Endlosschleife ausloesen
      resolve(bean);
      
      // Fertig
      return bean;
    }
    catch (Exception e)
    {
      Logger.error("unable to create instance of " + type,e);
      throw new ApplicationException(Application.getI18n().tr("{0} kann nicht erstellt werden: {1}",type.getSimpleName(),e.getMessage()));
    }
  }
  
  /**
   * Laedt die Abhaengigkeiten der Bean.
   * @param bean die Bean.
   */
  private void resolve(Object bean) throws Exception
  {
    final String name = bean.getClass().getSimpleName();
    
    // Resource-Annotations anwenden
    Inject.inject(bean,new Injector()
    {
      /**
       * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
       */
      public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        Resource r = (Resource) annotation;
        Class c = r.type(); // Das ist die Abhaengigkeit, die geladen werden soll
        if (c == null)
          return; // dann halt nicht.

        Logger.debug("  inject " + c.getSimpleName() + " into " + name);

        Object dep = get(c); // die loesen wir ebenfalls auf
        
        field.setAccessible(true);
        
        if (field instanceof Method)
        {
          ((Method)field).invoke(bean,dep);
        }
        else if (field instanceof Field)
        {
          ((Field)field).set(bean,dep);
        }
      }
    },Resource.class);
    
    // PostConstruct anwenden
    Inject.inject(bean,new Injector()
    {
      /**
       * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
       */
      public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        Method m = (Method) field;
        Logger.debug("  " + name + "." + m.getName());
        m.setAccessible(true);
        m.invoke(bean,(Object[]) null);
      }
    },PostConstruct.class);
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      Logger.debug("invoking predestroy for context beans");
      // PostDestroy bei den Context-Beans aufrufen
      // Erfolgt in umgekehrter Lade-Reihenfolge
      while (!this.contextOrder.isEmpty())
      {
        Object bean = this.contextOrder.pop();
        try
        {
          Inject.inject(bean,new Injector()
          {
            /**
             * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
             */
            public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
            {
              Method m = (Method) field;
              Logger.debug("  " + bean.getClass().getSimpleName() + "." + m.getName());
              m.setAccessible(true);
              m.invoke(bean,(Object[]) null);
            }
          },PreDestroy.class);
        }
        catch (Exception e)
        {
          Logger.error("unable to predestroy " + bean.getClass().getSimpleName(),e);
        }
      }
    }
    finally
    {
      contextOrder.clear();
      contextScope.clear();
      sessionScope.clear();
    }
  }

}



/**********************************************************************
 * $Log: BeanService.java,v $
 * Revision 1.2  2011/06/28 12:28:07  willuhn
 * @N Neuer BeanService als Dependency-Injection-Tool - yeah cool ;)
 *
 * Revision 1.1  2011-06-28 09:57:39  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 **********************************************************************/