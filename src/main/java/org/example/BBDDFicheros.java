package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BBDDFicheros {
    private final String nombreFichero;
    private final Map<String, Integer> campos; // (Clave): Nombre del campo y (Valor): su longitud en bytes
    private final String primaryKey;

    private int longRegistro; //Es la suma de cada una de las longitudes de sus campos
    private long numRegistro;
    private long numRegistroMarcadoBorrado;

    public BBDDFicheros(String nombreFichero, Map<String, Integer> campos, String primaryKey) throws IOException {
        this.nombreFichero = nombreFichero;
        this.campos = campos;
        this.primaryKey = primaryKey;
        this.numRegistro = 0;
        this.numRegistroMarcadoBorrado = 0;
        this.longRegistro = 0;

        //Calculo la longitud del fichero sumando la longitud en bytes de cada uno de ellos
        for (Map.Entry<String, Integer> campo : campos.entrySet()) {
            this.longRegistro += campo.getValue();
        }
        File file = new File(nombreFichero);
        if (file.exists()) {
            this.numRegistro = file.length() / longRegistro;
        } else {
            file.createNewFile();
        }
    }

    public String getNombreFichero() {
        return nombreFichero;
    }

    public Map<String, Integer> getCampos() {
        return campos;
    }

    public int getLongRegistro() {
        return longRegistro;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public long getNumRegistroMarcadoBorrado() {
        return numRegistroMarcadoBorrado;
    }

    public long getNumRegistro() {
        return numRegistro;
    }

    public Map<String, String> recuperar(String valorClave) {
        int pos = 0;
        boolean encontrado = false;
        Map<String, String> result = null;
        try (FileInputStream fis = new FileInputStream(nombreFichero)) {
            while (pos < this.numRegistro && !encontrado) {
                byte[] buffer = new byte[this.longRegistro];
                if (fis.read(buffer, 0, this.longRegistro) < this.longRegistro) {
                    return null;
                }
                int offsetCampo = 0;
                String unValorClave = null;
                for (Map.Entry<String, Integer> campo : campos.entrySet()) {
                    String unCampo = campo.getKey();
                    int longCampo = campo.getValue();
                    if (unCampo.equals(this.primaryKey)) {
                        unValorClave = new String(buffer, offsetCampo, longCampo, StandardCharsets.UTF_8);
                        break;
                    }
                    offsetCampo += longCampo;
                }
                if (valorClave.equals(unValorClave)) {//Para cada registro, comparamos si unValorClave recuperado del
                    encontrado = true;
                    offsetCampo = 0;
                    result = new HashMap<String, String>();
                    for (Map.Entry<String, Integer> campo : campos.entrySet()) {
                        String unCampo = campo.getKey();
                        int longCampo = campo.getValue();
                        if (unCampo.equals(this.primaryKey)) {
                            result.put(campo.getKey(), unValorClave);
                        }
                    }
                }
            }
        } catch (IOException io) {
            System.out.println("Error E/S: " + io.getMessage());
        }
        return result;
    }
}