package com.example.PFE.Model;

public class ReclamationStats {
    private long NEW_TODAY;
    private long UNREAD;
    private long RESOLUE;
    private long EN_ATTENTE;
    private long EN_COURS;
    private long REJETEE;
    private long TOTAL; // Ajoutez cette propriété

    // Getters and Setters
    public long getNEW_TODAY() {
        return NEW_TODAY;
    }

    public void setNEW_TODAY(long NEW_TODAY) {
        this.NEW_TODAY = NEW_TODAY;
    }

    public long getUNREAD() {
        return UNREAD;
    }

    public void setUNREAD(long UNREAD) {
        this.UNREAD = UNREAD;
    }

    public long getRESOLUE() {
        return RESOLUE;
    }

    public void setRESOLUE(long RESOLUE) {
        this.RESOLUE = RESOLUE;
    }

    public long getEN_ATTENTE() {
        return EN_ATTENTE;
    }

    public void setEN_ATTENTE(long EN_ATTENTE) {
        this.EN_ATTENTE = EN_ATTENTE;
    }

    public long getEN_COURS() {
        return EN_COURS;
    }

    public void setEN_COURS(long EN_COURS) {
        this.EN_COURS = EN_COURS;
    }

    public long getREJETEE() {
        return REJETEE;
    }

    public void setREJETEE(long REJETEE) {
        this.REJETEE = REJETEE;
    }

    public long getTOTAL() {
        return TOTAL;
    }

    public void setTOTAL(long TOTAL) {
        this.TOTAL = TOTAL;
    }
}