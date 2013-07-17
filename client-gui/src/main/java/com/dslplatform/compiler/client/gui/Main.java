package com.dslplatform.compiler.client.gui;

import java.lang.reflect.Field;

import com.dslplatform.compiler.client.api.ApiCall;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.logging.Logger.Level;
import com.dslplatform.compiler.client.api.logging.LoggerSystemOut;
import com.dslplatform.compiler.client.gui.windows.login.LoginDialog;
import com.dslplatform.compiler.client.gui.windows.login.LoginDialogResult;

public class Main {

    public static void main(final String[] args) {
        Setup.setLookAndFeel(true);

        final Logger logger = new LoggerSystemOut(Level.NONE);
        final ApiCall apiCall = new ApiCall(logger);

        LoginDialogResult result = LoginDialog.show(logger, apiCall, "a", "b", false);

        Print.AllFields(result);

        result = LoginDialog.show(logger, apiCall, "a", "b", false);

        Print.AllFields(result);
        //System.exit(0);
    }






















    private static class Print {

      public static void AllFields(Object o) {
        AllFields(o, null, 1);
      }

      public static void AllFields(Object o, String name) {
        AllFields(o, name, 1);
      }

      private static void AllFields(Object o, String name, int position) {
        if (o!=null) {
          int maxLength = 0;

          for (Field f : o.getClass().getDeclaredFields()) {
            int len = f.getName().length();
            if ( maxLength < len) maxLength = len;
          }

          if (name == null) {
            System.out.println((o.getClass().getSimpleName().toUpperCase()));
          } else {
            int startpos = position - 2 < 1 ? 1 : position - 2;
            System.out.println(String.format("%" + startpos + "s %s", "+", name.toUpperCase()));
          }

          try {

            for (Field f : o.getClass().getDeclaredFields()) {
              f.setAccessible(true);//Very important, this allows the setting to work.
              Object fieldObj = f.get(o);
              String value;
              if (fieldObj != null) {
                value = fieldObj.toString();
                if (value.startsWith(f.getType().getCanonicalName())) continue; // object
                if (f.getType().isArray())                            continue; // array
                if (Iterable.class.isAssignableFrom(f.getType()))     continue; // list
              } else {
                value = "NULL";
              }
              System.out.println(String.format("%-" + position + "s %-" + maxLength +"s : %s", " ", f.getName(), value));
            }

            for (Field f : o.getClass().getDeclaredFields()) {
              f.setAccessible(true);//Very important, this allows the setting to work.
              Object fieldObj = f.get(o);
              String value;
              if (fieldObj != null) {
                value = fieldObj.toString();

                if (f.getType().isArray()) { // array
                  Object[] objectArray =(Object[])fieldObj;
                  System.out.println(String.format("%-" + position + "s %-" + maxLength +"s", " ", "" + f.getName().toUpperCase()));
                  for (int index = 0; index < objectArray.length; index++) {
                    //AllFields(objectArray[index], String.format("[%s] %s", index, f.getType().getSimpleName()), position + 4);
                    AllFields(objectArray[index], "", position + 4);
                  }
                  System.out.println(String.format("%-" + position + "s %-" + maxLength +"s", " ", "-----------------------"));
                  continue;
                }

                if (Iterable.class.isAssignableFrom(f.getType())) {
                  Iterable<?> iterableFieldObject = (Iterable<?>) fieldObj;
                  //int index = 0;
                  System.out.println(String.format("%-" + position + "s %-" + maxLength +"s", " ", "" + f.getName().toUpperCase()));
                  for (Object iObj : iterableFieldObject) {
                    //AllFields(iObj, String.format("[%s] %s", index++, iObj.getClass().getSimpleName()), position + 4);
                    AllFields(iObj, "", position + 4);
                  }
                  System.out.println(String.format("%-" + position + "s %-" + maxLength +"s", " ", "-----------------------"));
                  continue;
                }

                if (value.startsWith(fieldObj.getClass().getCanonicalName())) {
                  AllFields(fieldObj, f.getName(), position + 2);
                }
              }
            }
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
}
