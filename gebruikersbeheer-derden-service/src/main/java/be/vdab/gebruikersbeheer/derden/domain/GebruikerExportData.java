package be.vdab.gebruikersbeheer.derden.domain;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class GebruikerExportData {

    @CsvBindByPosition(position = 0)
    private String achternaam;
    @CsvBindByPosition(position = 1)
    private String voornaam;
    @CsvBindByPosition(position = 2)
    private String account;
    @CsvBindByPosition(position = 3)
    private String vestigingsnummer;
    @CsvBindByPosition(position = 4)
    private String kantoor;
    @CsvBindByPosition(position = 5)
    private String toegangsrechten;
    @CsvBindByPosition(position = 6)
    private String telefoon;
    @CsvBindByPosition(position = 7)
    private String mail;

    public GebruikerExportData(String achternaam,
                               String voornaam,
                               String account,
                               String vestigingsnummer,
                               String kantoor,
                               String toegangsrechten,
                               String telefoon,
                               String mail) {
        this.achternaam = achternaam;
        this.voornaam = voornaam;
        this.account = account;
        this.vestigingsnummer = vestigingsnummer;
        this.kantoor = kantoor;
        this.toegangsrechten = toegangsrechten;
        this.telefoon = telefoon;
        this.mail = mail;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public void setVoornaam(String voornaam) {
        this.voornaam = voornaam;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setVestigingsnummer(String vestigingsnummer) {
        this.vestigingsnummer = vestigingsnummer;
    }

    public void setKantoor(String kantoor) {
        this.kantoor = kantoor;
    }

    public void setToegangsrechten(String toegangsrechten) {
        this.toegangsrechten = toegangsrechten;
    }

    public void setTelefoon(String telefoon) {
        this.telefoon = telefoon;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public String getAccount() {
        return account;
    }

    public String getTelefoon() {
        return telefoon;
    }

    public String getToegangsrechten() {
        return toegangsrechten;
    }

    public String getVestigingsnummer() {
        return vestigingsnummer;
    }

    public String getVoornaam() {
        return voornaam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GebruikerExportData that = (GebruikerExportData) o;
        return Objects.equals(achternaam, that.achternaam) &&
                Objects.equals(voornaam, that.voornaam) &&
                Objects.equals(account, that.account) &&
                Objects.equals(vestigingsnummer, that.vestigingsnummer) &&
                Objects.equals(kantoor, that.kantoor) &&
                Objects.equals(toegangsrechten, that.toegangsrechten) &&
                Objects.equals(telefoon, that.telefoon) &&
                Objects.equals(mail, that.mail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(achternaam, voornaam, account, vestigingsnummer, kantoor, toegangsrechten, telefoon, mail);
    }
}
