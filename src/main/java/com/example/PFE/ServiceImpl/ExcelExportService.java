package com.example.PFE.ServiceImpl;

import com.example.PFE.DTO.PlacementDTO;
import com.example.PFE.Model.Placement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    public ByteArrayInputStream placementsToExcel(List<Placement> placements) throws IOException {
        // Convert entities to DTOs
        List<PlacementDTO> placementDTOs = placements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Placements");

            // Create header row
            Row headerRow = sheet.createRow(0);

            String[] headers = {
                    "ID", "Code Agence", "Type Client", "Nature Placement",
                    "Montant", "Taux Proposé", "Durée (mois)", "Origine Fonds",
                    "Engagement Relation", "Taux Crédit", "Nantissement", "Statut",
                    "Message", "Client"
            };

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (PlacementDTO placement : placementDTOs) {
                Row row = sheet.createRow(rowNum++);

                setCellValue(row, 0, placement.getId());
                setCellValue(row, 1, placement.getCodeAgence());
                setCellValue(row, 2, placement.getTypeClient());
                setCellValue(row, 3, placement.getNaturePlacement());
                setCellValue(row, 4, placement.getMontant());
                setCellValue(row, 5, placement.getTauxPropose());
                setCellValue(row, 6, placement.getDuree());
                setCellValue(row, 7, placement.getOrigineFonds());
                setCellValue(row, 8, placement.getEngagementRelation());
                setCellValue(row, 9, placement.getTauxCredit());
                setCellValue(row, 10, placement.getNantissement());
                setCellValue(row, 11, placement.getStatut() != null ? placement.getStatut().toString() : null);
                setCellValue(row, 12, placement.getMessage());
                setCellValue(row, 13, placement.getClientName());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private PlacementDTO convertToDTO(Placement placement) {
        PlacementDTO dto = new PlacementDTO();
        dto.setId(placement.getId());
        dto.setCodeAgence(placement.getCodeAgence());
        dto.setTypeClient(placement.getTypeClient());
        dto.setPin(placement.getPin());
        dto.setNaturePlacement(placement.getNaturePlacement());
        dto.setMontant(placement.getMontant());
        dto.setTauxPropose(placement.getTauxPropose());
        dto.setDuree(placement.getDuree());
        dto.setOrigineFonds(placement.getOrigineFonds());
        dto.setEngagementRelation(placement.getEngagementRelation());
        dto.setTauxCredit(placement.getTauxCredit());
        dto.setNantissement(placement.getNantissement());
        dto.setStatut(placement.getStatut());
        dto.setMessage(placement.getMessage());

        if (placement.getClient() != null) {
            dto.setClientId(placement.getClient().getId());
            dto.setClientName(placement.getClient().getName());
        }

        return dto;
    }

    private void setCellValue(Row row, int column, Object value) {
        if (value == null) {
            row.createCell(column).setBlank();
            return;
        }

        Cell cell = row.createCell(column);

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}