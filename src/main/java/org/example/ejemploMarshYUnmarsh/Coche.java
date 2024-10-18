package org.example.ejemploMarshYUnmarsh;

import javax.xml.bind.annotation.*;

@XmlRootElement
public class Coche {
    private String matricula;
    private String marca;
    private String modelo;

    public Coche() {
    }

    public Coche(String matricula, String marca, String modelo) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    @XmlElement
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    @XmlElement
    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    @XmlElement
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
}
