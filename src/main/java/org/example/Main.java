package org.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Map<String,Integer> campos = new HashMap<String,Integer>();
        campos.put("Matrícula", 7);
        campos.put("Marca", 32);
        campos.put("Modelo", 32);

        try {
            BBDDFicheros bbddFicheros = new BBDDFicheros("coches.dat",campos,"Matrícula");
            HashMap<String,String> coches = new HashMap<>();

            coches.put("Matrícula","1111AAA");
            coches.put("Marca","Seat");
            coches.put("Modelo","León");
            bbddFicheros.insertar(coches);

            coches.clear();

            coches.put("Matrícula","2222BBB");
            coches.put("Marca","Toyota");
            coches.put("Modelo","CH-R");
            bbddFicheros.insertar(coches);

            coches.clear();

            coches.put("Matrícula","3333CCC");
            coches.put("Marca","Kia");
            coches.put("Modelo","Sportage");
            bbddFicheros.insertar(coches);

            coches.clear();

            coches.put("Matrícula","4444CCC");
            coches.put("Marca","Ferrari");
            coches.put("Modelo","Enzo");
            bbddFicheros.insertar(coches);

            coches.clear();

            mostrarContenido("2222BBB",bbddFicheros);
            bbddFicheros.modificar("2222BBB","Marca","Fiat");
            mostrarContenido("2222BBB",bbddFicheros);
            bbddFicheros.borrar("3333CCC");
            bbddFicheros.compactar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private static  void mostrarContenido (String unValorClave,BBDDFicheros bbddFicheros){
        HashMap<String,String> coches = (HashMap<String, String>) bbddFicheros.recuperar(unValorClave);
        for (Map.Entry<String,String> coche : coches.entrySet()){
            System.out.println(coche.getKey() + ": " + coche.getValue());
        }
    }
}
