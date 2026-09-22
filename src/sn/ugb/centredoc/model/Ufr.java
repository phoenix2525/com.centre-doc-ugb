package sn.ugb.centredoc.model;

import java.util.Objects;

/**
 * Entité représentant une UFR (Unité de Formation et de Recherche) de l'UGB.
 */
public class Ufr {
    private int idUfr;
    private String code;
    private String nom;

    public Ufr() {
    }

    public Ufr(int idUfr, String code, String nom) {
        this.idUfr = idUfr;
        this.code = code;
        this.nom = nom;
    }

    public Ufr(String code, String nom) {
        this(0, code, nom);
    }

    public int getIdUfr() {
        return idUfr;
    }

    public void setIdUfr(int idUfr) {
        this.idUfr = idUfr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ufr ufr = (Ufr) o;
        return idUfr == ufr.idUfr || Objects.equals(code, ufr.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUfr, code);
    }

    @Override
    public String toString() {
        return code + " - " + nom;
    }
}
