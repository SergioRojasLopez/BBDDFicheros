package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                        String valorCampo = new String(buffer, offsetCampo, longCampo, StandardCharsets.UTF_8);
                        result.put(unCampo, valorCampo);
                        offsetCampo += longCampo;

                    }
                }
            }
        } catch (IOException io) {
            System.out.println("Error E/S: " + io.getMessage());
        }
        return result;
    }

    public long insertar(HashMap<String, String> reg) throws IOException {
        String valorCampoClave = reg.get(this.primaryKey);
        if (recuperar(valorCampoClave) != null) {//Comprobamos si ya existe un registro con el mismo valor para el campo clave que el queremos insertar (No está permitido)
            System.err.println("No se puede insertar debido a que ya existe uno con esta clave primaria - " + valorCampoClave);
            return -1;
        }

        try (FileOutputStream fos = new FileOutputStream(nombreFichero, true)) {
            for (Map.Entry<String, Integer> campo : campos.entrySet()) {
                int longCampo = campo.getValue();
                String valorCampo = reg.get(campo.getKey());
                if (valorCampo == null) {
                    valorCampo = "";
                }

                String valorCampoForm = String.format("%1$-" + longCampo + "s", valorCampo); //devuelve el valor del 1er argumento en un String con longitud "longCampo" y alineado a la izquierda (gracias al uso de "-")
                fos.write(valorCampoForm.getBytes("UTF-8"), 0, longCampo);
            }
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.numRegistro++;
        return this.numRegistro - 1;
    }

    public boolean borrar(String valorClave) throws FileNotFoundException, IOException {
        // Muy parecido a la búsqueda y modificación, con la diferencia de que, una vez encontrado,
        // se marca como borrado
        int pos = 0;
        boolean encontrado = false;
        try (RandomAccessFile raf = new RandomAccessFile(this.nombreFichero, "rws")) {  // Se necesita leer y escribir, y además volver hacia atrás a una posición conocida
            while (pos < this.numRegistro && !encontrado) {
                byte buffer[] = new byte[this.longRegistro]; // Leer registro
                if (raf.read(buffer, 0, this.longRegistro) < this.longRegistro) {
                    return false;
                }
                int offsetCampo = 0;  // Obtener valor del campo clave
                String unValorClave = null;
                for (Map.Entry<String, Integer> campo : campos.entrySet()) {
                    String unCampo = campo.getKey();
                    int longCampo = campo.getValue();
                    if (unCampo.equals(this.primaryKey)) {
                        unValorClave = new String(buffer, offsetCampo, longCampo, StandardCharsets.UTF_8);
                        break;  // Ya tenemos el valor del campo clave
                    }
                    offsetCampo += longCampo;
                }
                if (valorClave.equals(unValorClave)) {
                    offsetCampo = 0;
                    encontrado = true;  // Ahora hay que poner todos los bytes a cero en registro en posición pos
                    raf.seek(pos * this.longRegistro);
                    java.util.Arrays.fill(buffer, (byte) 0);  // Por si acaso, no es necesario.
                    raf.write(buffer, 0, this.longRegistro);
                    this.numRegistroMarcadoBorrado++;
                }
                pos++;
            }
        }
        return encontrado;
    }

    /**
     * Compacta fichero, es decir, elimina registros marcados como borrados
     *
     * @return número de registros marcados como borrado, que se han eliminado.
     * -1 si ha habido algún error que ha impedido compactar el fichero.
     * @throws IOException
     */
    public int compactar() throws IOException {
        int numSuprimidos = 0;
        int numReg_ = 0;
        File fTemp = File.createTempFile(nombreFichero, "");
        try (FileInputStream fis = new FileInputStream(nombreFichero);
             FileOutputStream fos = new FileOutputStream(fTemp)) {
            byte[] buffer = new byte[this.longRegistro];
            for (int pos = 0; pos < this.numRegistro; pos++) {
                fis.read(buffer);
                boolean noCero = false;  // Ver si marcado para borrado: todo a cero
                for (int j = 0; j < this.longRegistro && !noCero; j++) {
                    if (buffer[j] != 0) {
                        noCero = true;
                    }
                }
                if (noCero) {
                    fos.write(buffer);
                    numReg_++;
                } else {
                    numSuprimidos++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Copia de seguridad de fichero original, con timestamp y número aleatorio.
        // Necesario para poder renombrar fichero temporal como original. Podría borrarse al final.
        java.util.Random r = new java.util.Random();
        String nombreCopiaSeg = nombreFichero + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "." + r.nextInt() + ".bak";
        File fOrig = new File(nombreFichero);
        if (!fOrig.renameTo(new File(nombreCopiaSeg))) {
            System.err.println("Error haciendo copia de seguridad de " + nombreFichero + " a " + nombreCopiaSeg);
            return -1;
        }
        String nomFichTemp = fTemp.getAbsolutePath(); // Se renombra fichero temporal como original
        if (!fTemp.renameTo(new File(nombreFichero))) {
            System.err.println("Error copiando de fichero temporal " + nomFichTemp + " a " + nombreFichero);
            return -1;
        }
        //File fCopiaSeg=new File(nombreCopiaSeg); fCopiaSeg.delete();  // descomentar para borrar copia de seguridad

        this.numRegistro = numReg_;
        this.numRegistroMarcadoBorrado = 0;
        return numSuprimidos;
    }

    public boolean modificar(String valorClave, String nombreCampo, String valorCampo) throws IOException {
        if (nombreCampo.equals(this.primaryKey)) {
            System.out.println("No se puede modificar el campo clave: " + nombreCampo);
            return false;
        }
        int pos = 0;
        boolean encontrado = false;
        RandomAccessFile raf = new RandomAccessFile(this.nombreFichero, "rws");
        while (pos < this.numRegistro && !encontrado) {
            byte buffer[] = new byte[this.longRegistro];
            if (raf.read(buffer, 0, this.longRegistro) < this.longRegistro) {
                return false;
            }
            String unValorClave = recuperarValorCampoClave(buffer);

            if (valorClave.equals(unValorClave)) {
                int offsetCampo = 0;
                encontrado = true;
                raf.seek(pos * longRegistro);
                for (Map.Entry<String, Integer> campo : campos.entrySet()) {
                    String unCampo = campo.getKey();
                    int longCampo = campo.getValue();
                    if (nombreCampo.equals(unCampo)) {
                        raf.skipBytes(offsetCampo);
                        String valorCampoForm = String.format("%1$-" + longCampo + "s", valorCampo);
                        raf.write(valorCampoForm.getBytes("UTF-8"), 0, longCampo);
                        break;
                    }
                    offsetCampo += longCampo;
                }
            }
            pos++;
        }
        return encontrado;

    }

    private String recuperarValorCampoClave(byte[] buffer) {
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
        return unValorClave;
    }


}