package be.vdab.gebruikersbeheer.derden.service;


import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface CsvExportService {

    void sendCsv(HttpServletResponse response, Ikp ikp, String fileName) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;
}
